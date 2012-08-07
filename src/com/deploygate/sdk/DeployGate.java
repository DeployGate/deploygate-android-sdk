
package com.deploygate.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.IDeployGateSdkService;
import com.deploygate.service.IDeployGateSdkServiceCallback;

import android.Manifest.permission;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class DeployGate {

    private static final String TAG = "DeployGate";

    public static final String ACTION_DEPLOYGATE_STARTED = "com.deploygate.action.ServiceStarted";
    
    static final String ACTION_APPLICATION_START = "com.deploygate.action.ApplicationStart";
    static final String ACTION_APPLICATION_CRASHED = "com.deploygate.action.ApplicationCrashed";

    static final String DEPLOYGATE_PACKAGE = "com.deploygate";
    static final String EXTRA_EXCEPTION = "com.deploygate.exception";
    static final String EXTRA_PACKAGE_NAME = "com.deploygate.packageName";
    
    private static final String[] DEPLOYGATE_FINGERPRINTS = new String[] {
        "c1f285f69cc02a397135ed182aa79af53d5d20a1", // mba debug
        "234eff4a1600a7aa78bf68adfbb15786e886ae1a", // jenkins debug
    };

    private static DeployGate sInstance;
    
    private final Context mApplicationContext;
    protected IDeployGateSdkService mRemoteService;

    protected Thread mLogcatThread;
    protected final IDeployGateSdkServiceCallback mRemoteCallback = new IDeployGateSdkServiceCallback.Stub() {
        private LogCatTranportWorker mLogcatWorker;

        public void onEvent(String action, Bundle extras) throws RemoteException {
            if (DeployGateEvent.ACTION_INIT.equals(action)) {
                onInitialized(extras.getBoolean(DeployGateEvent.EXTRA_IS_MANAGED, false),
                        extras.getBoolean(DeployGateEvent.EXTRA_IS_AUTHORIZED, false),
                        extras.getString(DeployGateEvent.EXTRA_LOGIN_USERNAME),
                        extras.getBoolean(DeployGateEvent.EXTRA_IS_STOP_REQUESTED, false));
            }
            else if (DeployGateEvent.ACTION_UPDATE_AVAILABLE.equals(action)) {
                onUpdateArrived(extras.getInt(DeployGateEvent.EXTRA_SERIAL),
                        extras.getString(DeployGateEvent.EXTRA_VERSION_NAME),
                        extras.getInt(DeployGateEvent.EXTRA_VERSION_CODE));
            }
            else if (DeployGateEvent.ACTION_ENABLE_LOGCAT.equals(action)) {
                onEnableLogcat(true);
            }
            else if (DeployGateEvent.ACTION_DISABLE_LOGCAT.equals(action)) {
                onEnableLogcat(false);
            }
        };

        private void onEnableLogcat(boolean isEnabled) {
            if (mRemoteService == null)
                return;
            
            if (isEnabled) {
                if (mLogcatThread == null || !mLogcatThread.isAlive()) {
                    mLogcatWorker = new LogCatTranportWorker(mApplicationContext.getPackageName(),
                            mRemoteService);
                    mLogcatThread = new Thread(mLogcatWorker);
                    mLogcatThread.start();
                }
            } else {
                if (mLogcatThread != null && mLogcatThread.isAlive()) {
                    mLogcatWorker.stop();
                    mLogcatThread.interrupt();
                }
            }
        }

        private void onInitialized(final boolean isManaged, final boolean isAuthorized,
                final String loginUsername, final boolean isStopped) throws RemoteException {
            Log.v(TAG, "DeployGate service initialized");
            mAppIsManaged = isManaged;
            mAppIsAuthorized = isAuthorized;
            mAppIsStopRequested = isStopped;
            mLoginUsername = loginUsername;
            
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onInitialized(true);
                        mCallback.onStatusChanged(isManaged, isAuthorized, loginUsername, isStopped);
                    }
                }
            });
        }
        
        private void onUpdateArrived(final int serial, final String versionName,
                final int versionCode) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null)
                        mCallback.onUpdateAvailable(serial, versionName, versionCode);
                }
            });
        }
    };
    
    private final Handler mHandler;
    private final DeployGateCallback mCallback;
    
    private boolean mAppIsManaged;
    private boolean mAppIsAuthorized;
    private boolean mAppIsStopRequested;
    private String mLoginUsername;
    
    /**
     * Do not instantiate directly. Call {@link #install(Application)} on your
     * {@link Application#onCreate()} instead.
     */
    private DeployGate(Context applicationContext, DeployGateCallback callback) {
        mHandler = new Handler();
        mApplicationContext = applicationContext;
        mCallback = callback;
        
        prepareBroadcastReceiver();
        if (isDeployGateAvailable()) {
            Log.v(TAG, "DeployGate installation detected. Initializing.");
            bindToService(true);
        } else {
            Log.v(TAG, "DeployGate is not available on this device.");
            if (mCallback != null)
                mCallback.onInitialized(false);
        }
    }

    private boolean isDeployGateAvailable() {
        String sig = getDeployGatePackageSignature();
        if (sig == null)
            return false;
        for (String value : DEPLOYGATE_FINGERPRINTS)
            if (value.equals(sig))
                return true;
        return false;
    }

    private void prepareBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_DEPLOYGATE_STARTED);
        mApplicationContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null)
                    return;
                if (isDeployGateAvailable()) {
                    bindToService(false);
                }
            }
        }, filter);
    }

    private void bindToService(final boolean isBoot) {
        Intent service = new Intent(IDeployGateSdkService.class.getName());
        service.setPackage(DEPLOYGATE_PACKAGE);
        mApplicationContext.bindService(service, new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.v(TAG, "DeployGate service connected");
                mRemoteService = IDeployGateSdkService.Stub.asInterface(service);
                
                Bundle args = new Bundle();
                args.putBoolean(DeployGateEvent.EXTRA_IS_BOOT, isBoot);
                args.putBoolean(DeployGateEvent.EXTRA_CAN_LOGCAT, canLogCat());
                try {
                    mRemoteService.init(mRemoteCallback, mApplicationContext.getPackageName(), args);
                } catch (RemoteException e) {
                    Log.w(TAG, "DeployGate service failed to be initialized.");
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.v(TAG, "DeployGate service disconneced");
                mRemoteService = null;
            }
        }, Context.BIND_AUTO_CREATE);
    }

    protected boolean canLogCat() {
        return mApplicationContext.getPackageManager().checkPermission(permission.READ_LOGS,
                mApplicationContext.getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

    public String getDeployGatePackageSignature() {
        PackageInfo info;
        try {
            info = mApplicationContext.getPackageManager().getPackageInfo(
                    "com.deploygate", PackageManager.GET_SIGNATURES);
        } catch (NameNotFoundException e) {
            return null;
        }
        if (info == null || info.signatures.length == 0)
            return null;
        
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA1 is not supported on this platform?", e);
            return null;
        }
        
        byte[] digest = md.digest(info.signatures[0].toByteArray());
        StringBuilder result = new StringBuilder(40);
        for (int i = 0; i < digest.length; i++) {
            result.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
    
    /**
     * Install DeployGate on your application instance. Call this method inside
     * of your {@link Application#onCreate()}.
     * 
     * @param app Application instance, typically just pass <em>this<em>.
     */
    public static void install(Application app) {
        install(app, null);
    }

    /**
     * Install DeployGate on your application instance. Call this method inside
     * of your {@link Application#onCreate()}.
     * 
     * @param app Application instance, typically just pass <em>this<em>.
     * @param callback Callback interface to listen events.
     */
    public static void install(Application app, DeployGateCallback callback) {
        if (sInstance == null) {
            Thread.setDefaultUncaughtExceptionHandler(new DeployGateUncaughtExceptionHandler(
                    app.getApplicationContext(), Thread
                            .getDefaultUncaughtExceptionHandler()));
            sInstance = new DeployGate(app.getApplicationContext(), callback);
        }
    }

    public static boolean isManaged() {
        if (sInstance != null)
            return sInstance.mAppIsManaged;
        return false;
    }

    public static boolean isAuthorized() {
        if (sInstance != null)
            return sInstance.mAppIsAuthorized;
        return false;
    }

    public static String getLoginUsername() {
        if (sInstance != null)
            return sInstance.mLoginUsername;
        return null;
    }
    
    public static boolean isStopRequested() {
        if (sInstance != null)
            return sInstance.mAppIsStopRequested;
        return false;
    }
    
    private static class LogCatTranportWorker implements Runnable {
        
        private final String mPackageName;
        private final IDeployGateSdkService mService;
        private Process mProcess;

        public LogCatTranportWorker(String packageName, IDeployGateSdkService service) {
            mPackageName = packageName;
            mService = service;
        }
        
        @Override
        public void run() {
            mProcess = null;
            ArrayList<String> logcatBuf = null;
            try {
                ArrayList<String> commandLine = new ArrayList<String>();
                commandLine.add("logcat");
                logcatBuf = new ArrayList<String>();
                
                commandLine.add("-v");
                commandLine.add("threadtime");
                commandLine.add("*:V");

                mProcess = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));

                Log.v(TAG, "Start retrieving logcat");
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    logcatBuf.add(line + "\n");
                    if (!bufferedReader.ready()) {
                        if (send(logcatBuf)) 
                            logcatBuf.clear();
                        else
                            return;
                    }
                }
                // EOF, stop it
            } catch (IOException e) {
                Log.d(TAG, "Logcat stopped: " + e.getMessage());
            } finally {
                if (mProcess != null)
                    mProcess.destroy();
            }
        }
        
        public void stop() {
            if (mProcess != null)
                mProcess.destroy();
        }

        private boolean send(ArrayList<String> logcatBuf) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(DeployGateEvent.EXTRA_LOG, logcatBuf);
            try {
                mService.sendEvent(mPackageName, DeployGateEvent.ACTION_SEND_LOGCAT, bundle);
            } catch (RemoteException e) {
                return false;
            }
            return true;
        }
    }

    static DeployGate getInstance() {
        return sInstance;
    }

    public void sendCrashReport(Throwable ex) {
        if (mRemoteService == null)
            return;
        Bundle extras = new Bundle();
        extras.putSerializable(DeployGateEvent.EXTRA_EXCEPTION, ex); 
        try {
            mRemoteService.sendEvent(mApplicationContext.getPackageName(),
                    DeployGateEvent.ACTION_SEND_CRASH_REPORT, extras);
        } catch (RemoteException e) {
            Log.w(TAG, "failed to send crash report: " + e.getMessage());
        }
    }
}
