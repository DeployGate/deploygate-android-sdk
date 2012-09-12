
package com.deploygate.sdk;

import android.Manifest.permission;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.IDeployGateSdkService;
import com.deploygate.service.IDeployGateSdkServiceCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 * This is DeployGate SDK library. Import this library to the application
 * package and call {@link #install(Application)} on the onCreate() of
 * application class to enable crash reporting and application launch
 * notification.
 * <p>
 * In order to get working Remote LogCat feature, you also have to add
 * <code>&lt;uses-permission android:name="android.permission.READ_LOGS" /&gt;</code>
 * in AndroidManifest.xml of your application.
 * </p>
 * 
 * @author tnj
 */
public class DeployGate {

    private static final String TAG = "DeployGate";
    private static final int SDK_VERSION = 2;

    private static final String ACTION_DEPLOYGATE_STARTED = "com.deploygate.action.ServiceStarted";
    private static final String DEPLOYGATE_PACKAGE = "com.deploygate";

    private static final String[] DEPLOYGATE_FINGERPRINTS = new String[] {
            "2f97f647645cb762bf5fc1445599a954e6ad76e7", // deploygate release
            "c1f285f69cc02a397135ed182aa79af53d5d20a1", // mba debug
            "234eff4a1600a7aa78bf68adfbb15786e886ae1a", // jenkins debug
    };

    private static DeployGate sInstance;

    private final Context mApplicationContext;
    private final Handler mHandler;
    private final HashSet<DeployGateCallback> mCallbacks;
    private String mAuthor;
    private String mExpectedAuthor;

    private CountDownLatch mInitializedLatch;
    private boolean mIsDeployGateAvailable;

    private boolean mAppIsManaged;
    private boolean mAppIsAuthorized;
    private boolean mAppIsStopRequested;
    private String mLoginUsername;

    @SuppressWarnings("unused")
    private boolean mAppUpdateAvailable;
    @SuppressWarnings("unused")
    private int mAppUpdateRevision;
    @SuppressWarnings("unused")
    private String mAppUpdateVersionName;
    @SuppressWarnings("unused")
    private int mAppUpdateVersionCode;

    private IDeployGateSdkService mRemoteService;
    private Thread mLogcatThread;
    private LogCatTranportWorker mLogcatWorker;

    private final IDeployGateSdkServiceCallback mRemoteCallback = new IDeployGateSdkServiceCallback.Stub() {

        public void onEvent(String action, Bundle extras) throws RemoteException {
            if (DeployGateEvent.ACTION_INIT.equals(action)) {
                onInitialized(extras.getBoolean(DeployGateEvent.EXTRA_IS_MANAGED, false),
                        extras.getBoolean(DeployGateEvent.EXTRA_IS_AUTHORIZED, false),
                        extras.getString(DeployGateEvent.EXTRA_LOGIN_USERNAME),
                        extras.getBoolean(DeployGateEvent.EXTRA_IS_STOP_REQUESTED, false),
                        extras.getString(DeployGateEvent.EXTRA_AUTHOR));
            }
            else if (DeployGateEvent.ACTION_UPDATE_AVAILABLE.equals(action)) {
                onUpdateArrived(extras.getInt(DeployGateEvent.EXTRA_SERIAL),
                        extras.getString(DeployGateEvent.EXTRA_VERSION_NAME),
                        extras.getInt(DeployGateEvent.EXTRA_VERSION_CODE),
                        extras.getString(DeployGateEvent.EXTRA_SERIAL_MESSAGE));
            }
            else if (DeployGateEvent.ACTION_ONESHOT_LOGCAT.equals(action)) {
                onOneshotLogcat();
            }
            else if (DeployGateEvent.ACTION_ENABLE_LOGCAT.equals(action)) {
                onEnableLogcat(true);
            }
            else if (DeployGateEvent.ACTION_DISABLE_LOGCAT.equals(action)) {
                onEnableLogcat(false);
            }
        };

        private void onOneshotLogcat() {
            if (mLogcatThread == null || !mLogcatThread.isAlive()) {
                mLogcatWorker = new LogCatTranportWorker(
                        mApplicationContext.getPackageName(), mRemoteService, true);
                mLogcatThread = new Thread(mLogcatWorker);
                mLogcatThread.start();
            }
        }

        private void onEnableLogcat(boolean isEnabled) {
            if (mRemoteService == null)
                return;

            if (isEnabled) {
                if (mLogcatThread == null || !mLogcatThread.isAlive()) {
                    mLogcatWorker = new LogCatTranportWorker(
                            mApplicationContext.getPackageName(), mRemoteService, false);
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
                final String loginUsername, final boolean isStopped, final String author) throws RemoteException {
            Log.v(TAG, "DeployGate service initialized");
            mAppIsManaged = isManaged;
            mAppIsAuthorized = isAuthorized;
            mAppIsStopRequested = isStopped;
            mLoginUsername = loginUsername;
            mAuthor = author;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeployGateCallback callback : mCallbacks) {
                        callback.onInitialized(true);
                        callback.onStatusChanged(isManaged, isAuthorized, loginUsername, isStopped);
                    }
                }
            });

            mIsDeployGateAvailable = true;
            mInitializedLatch.countDown();
        }

        private void onUpdateArrived(final int serial, final String versionName,
                final int versionCode, final String message) throws RemoteException {
            mAppUpdateAvailable = true;
            mAppUpdateRevision = serial;
            mAppUpdateVersionName = versionName;
            mAppUpdateVersionCode = versionCode;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (DeployGateCallback callback : mCallbacks) {
                        callback.onUpdateAvailable(serial, versionName, versionCode);
                    }
                }
            });
        }
    };

    void callbackDeployGateUnavailable() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (DeployGateCallback callback : mCallbacks) {
                    callback.onInitialized(false);
                    callback.onStatusChanged(false, false, null, false);
                }
            }
        });
    }

    /**
     * Do not instantiate directly. Call {@link #install(Application)} on your
     * {@link Application#onCreate()} instead.
     */
    private DeployGate(Context applicationContext, String author, DeployGateCallback callback) {
        mHandler = new Handler();
        mApplicationContext = applicationContext;
        mCallbacks = new HashSet<DeployGateCallback>();
        mExpectedAuthor = author;

        prepareBroadcastReceiver();

        if (callback != null)
            mCallbacks.add(callback);

        mInitializedLatch = new CountDownLatch(1);
        if (!initService(true)) {
            if (callback != null)
                callback.onInitialized(false);
        }
    }

    private boolean initService(boolean isBoot) {
        if (isDeployGateAvailable()) {
            Log.v(TAG, "DeployGate installation detected. Initializing.");
            bindToService(isBoot);
            return true;
        } else {
            Log.v(TAG, "DeployGate is not available on this device.");
            mInitializedLatch.countDown();
            mIsDeployGateAvailable = false;
            callbackDeployGateUnavailable();
            return false;
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
                requestServiceInit(isBoot);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.v(TAG, "DeployGate service disconneced");
                mRemoteService = null;
            }
        }, Context.BIND_AUTO_CREATE);
    }

    private void requestServiceInit(final boolean isBoot) {
        Bundle args = new Bundle();
        args.putBoolean(DeployGateEvent.EXTRA_IS_BOOT, isBoot);
        args.putBoolean(DeployGateEvent.EXTRA_CAN_LOGCAT, canLogCat());
        args.putString(DeployGateEvent.EXTRA_EXPECTED_AUTHOR, mExpectedAuthor);
        args.putInt(DeployGateEvent.EXTRA_SDK_VERSION, SDK_VERSION);
        try {
            mRemoteService.init(mRemoteCallback, mApplicationContext.getPackageName(), args);
        } catch (RemoteException e) {
            Log.w(TAG, "DeployGate service failed to be initialized.");
        }
    }

    protected boolean canLogCat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return true;
        return mApplicationContext.getPackageManager().checkPermission(permission.READ_LOGS,
                mApplicationContext.getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

    private String getDeployGatePackageSignature() {
        PackageInfo info;
        try {
            info = mApplicationContext.getPackageManager().getPackageInfo(
                    DEPLOYGATE_PACKAGE, PackageManager.GET_SIGNATURES);
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
     * of your {@link Application#onCreate()} once.
     * <p>
     * On a release build, which has <tt>android:isDebuggable</tt> set false on
     * AndroidManifest.xml, this function will do nothing. If you want to enable
     * DeployGate on a release build, consider using
     * {@link #install(Application, String, DeployGateCallback, boolean)}
     * instead.
     * </p>
     * <p>
     * <b>Note:</b> To make {@link #isAuthorized()} more effective, you should
     * call {@link #install(Application, String)} instead and specify authorId
     * explicitly to ensure the authority of this app to prevent casual
     * redistribution via DeployGate.
     * </p>
     * 
     * @param app Application instance, typically just pass <em>this</em>.
     * @throws IllegalStateException if this called twice
     */
    public static void install(Application app) {
        install(app, (String) null);
    }

    /**
     * Install DeployGate on your application instance. Call this method inside
     * of your {@link Application#onCreate()} once.
     * <p>
     * On a release build, which has <tt>android:isDebuggable</tt> set false on
     * AndroidManifest.xml, this function will do nothing. If you want to enable
     * DeployGate on a release build, consider using
     * {@link #install(Application, String[], DeployGateCallback, boolean)}
     * instead.
     * </p>
     * 
     * @param app Application instance, typically just pass <em>this</em>.
     * @param author author username of this app.
     * @throws IllegalStateException if this called twice
     */
    public static void install(Application app, String author) {
        install(app, author, null);
    }

    /**
     * Install DeployGate on your application instance and register a callback
     * listener. Call this method inside of your {@link Application#onCreate()}
     * once.
     * <p>
     * On a release build, which has <tt>android:isDebuggable</tt> set false on
     * AndroidManifest.xml, this function will do nothing. If you want to enable
     * DeployGate on a release build, consider using
     * {@link #install(Application, String, DeployGateCallback, boolean)}
     * instead.
     * </p>
     * <p>
     * <b>Note:</b> To make {@link #isAuthorized()} more effective, you should
     * call {@link #install(Application, String)} instead and specify authorId
     * explicitly to ensure the authority of this app to prevent casual
     * redistribution via DeployGate.
     * </p>
     * 
     * @param app Application instance, typically just pass <em>this</em>.
     * @param callback Callback interface to listen events.
     * @throws IllegalStateException if this called twice
     */
    public static void install(Application app, DeployGateCallback callback) {
        install(app, null, callback);
    }

    /**
     * Install DeployGate on your application instance and register a callback
     * listener. Call this method inside of your {@link Application#onCreate()}
     * once.
     * <p>
     * On a release build, which has <tt>android:isDebuggable</tt> set false on
     * AndroidManifest.xml, this function will do nothing. If you want to enable
     * DeployGate on a release build, consider using
     * {@link #install(Application, String, DeployGateCallback, boolean)}
     * instead.
     * </p>
     * 
     * @param app Application instance, typically just pass <em>this</em>.
     * @param author author username of this app.
     * @param callback Callback interface to listen events.
     * @throws IllegalStateException if this called twice
     */
    public static void install(Application app, String author, DeployGateCallback callback) {
        install(app, callback, false);
    }

    /**
     * Install DeployGate on your application instance and register a callback
     * listener. Call this method inside of your {@link Application#onCreate()}
     * once.
     * <p>
     * <b>Note:</b> To make {@link #isAuthorized()} more effective, you should
     * call {@link #install(Application, String)} instead and specify authorId
     * explicitly to ensure the authority of this app to prevent casual
     * redistribution via DeployGate.
     * </p>
     * 
     * @param app Application instance, typically just pass <em>this</em>.
     * @param callback Callback interface to listen events. Can be null.
     * @param forceApplyOnReleaseBuild if you want to keep DeployGate alive on
     *            the release build, set this true.
     * @throws IllegalStateException if this called twice
     */
    public static void install(Application app, DeployGateCallback callback,
            boolean forceApplyOnReleaseBuild) {
        install(app, null, callback, forceApplyOnReleaseBuild);
    }

    /**
     * Install DeployGate on your application instance and register a callback
     * listener. Call this method inside of your {@link Application#onCreate()}
     * once.
     * 
     * @param app Application instance, typically just pass <em>this</em>.
     * @param author author username of this app. Can be null.
     * @param callback Callback interface to listen events. Can be null.
     * @param forceApplyOnReleaseBuild if you want to keep DeployGate alive on
     *            the release build, set this true.
     * @throws IllegalStateException if this called twice
     */
    public static void install(Application app, String author, DeployGateCallback callback,
            boolean forceApplyOnReleaseBuild) {
        if (sInstance != null)
            throw new IllegalStateException("install already called");

        if (!forceApplyOnReleaseBuild && !isDebuggable(app.getApplicationContext()))
            return;

        Thread.setDefaultUncaughtExceptionHandler(new DeployGateUncaughtExceptionHandler(Thread
                .getDefaultUncaughtExceptionHandler()));
        sInstance = new DeployGate(app.getApplicationContext(), author, callback);
    }

    /**
     * Request refreshing cached session values (e.g., isAuthorized, etc.) to
     * the DeployGate service. Nothing happens if this called before
     * {@link #install(Application)} or when refreshing is already in progress.
     * Note that after calling this, {@link #isInitialized()} will changed to
     * false immediately and any call to <tt>is*()</tt> will be blocked until
     * refreshing get finished.
     */
    public static void refresh() {
        if (sInstance != null)
            sInstance.refreshInternal();
    }

    private void refreshInternal() {
        if (mInitializedLatch.getCount() == 0) {
            mInitializedLatch = new CountDownLatch(1);
            if (mRemoteService == null) {
                initService(false);
            } else {
                requestServiceInit(false);
            }
        }
    }

    /**
     * Register a DeployGate event callback listener. Don't forget to call
     * {@link #unregisterCallback(DeployGateCallback)} when the callback is no
     * longer needed (e.g., on destroying an activity.) If the listener has
     * already in the callback list, just ignored.
     * 
     * @param listener callback listener
     * @param refreshImmediately if you want to receive current states, set this
     *            true.
     */
    public static void registerCallback(DeployGateCallback listener, boolean refreshImmediately) {
        if (sInstance == null)
            return;
        if (listener == null)
            return;

        sInstance.registerCallbackInternal(listener, refreshImmediately);
    }

    private void registerCallbackInternal(DeployGateCallback listener, boolean callbackImmediately) {
        mCallbacks.add(listener);
        if (callbackImmediately)
            refresh();
    }

    /**
     * Unregister a callback listener. If the listener was not registered, just
     * ignored.
     * 
     * @param listener callback listener to be removed
     */
    public static void unregisterCallback(DeployGateCallback listener) {
        if (sInstance == null)
            return;
        if (listener == null)
            return;

        sInstance.mCallbacks.remove(listener);
    }

    /**
     * Get whether SDK is completed its intialization process and ready after
     * {@link #install(Application)}. This call will never blocked.
     * 
     * @return true if SDK is ready. false otherwise. If no install() called
     *         ever, this always returns false.
     */
    public static boolean isInitialized() {
        if (sInstance != null) {
            return sInstance.mInitializedLatch.getCount() == 0;
        }
        return false;
    }

    /**
     * Get whether DeployGate client service is available on this device.
     * <p>
     * Note this function will block until SDK get ready after
     * {@link #install(Application)} called. So if you want to call this
     * function from the main thread, you should confirm that
     * {@link #isInitialized()} is true before calling this. (Or consider using
     * {@link DeployGateCallback#onInitialized(boolean)} callback.)
     * </p>
     * 
     * @return true if valid DeployGate client is available. false otherwise. If
     *         no install() called ever, this always returns false.
     */
    public static boolean isDeployGateAvaliable() {
        if (sInstance != null) {
            waitForInitialized();
            return sInstance.mIsDeployGateAvailable;
        }
        return false;
    }

    /**
     * Get whether this application and its package is known and managed under
     * the DeployGate.
     * <p>
     * Note this function will block until SDK get ready after
     * {@link #install(Application)} called. So if you want to call this
     * function from the main thread, you should confirm that
     * {@link #isInitialized()} is true before calling this. (Or consider using
     * {@link DeployGateCallback#onInitialized(boolean)} callback.)
     * </p>
     * 
     * @return true if DeployGate knows and manages this package. false
     *         otherwise. If no install() called ever, this always returns
     *         false.
     */
    public static boolean isManaged() {
        if (sInstance != null) {
            waitForInitialized();
            return sInstance.mAppIsManaged;
        }
        return false;
    }

    /**
     * Get whether current DeployGate user has this application in his/her
     * available list. You may want to check this value on initialization
     * process of the main activity if you want to limit user of this
     * application to only who you explicitly allowed.
     * <p>
     * Note this function will block until SDK get ready after
     * {@link #install(Application)} called. So if you want to call this
     * function from the main thread, you should confirm that
     * {@link #isInitialized()} is true before calling this. (Or consider using
     * {@link DeployGateCallback#onInitialized(boolean)} callback.)
     * </p>
     * 
     * @return true if current DeployGate user has available list which contains
     *         this application. false otherwise. If no install() called ever,
     *         this always returns false.
     */
    public static boolean isAuthorized() {
        if (sInstance != null) {
            waitForInitialized();
            return sInstance.mAppIsAuthorized;
        }
        return false;
    }

    /**
     * Get current DeployGate username. This function only available when
     * {@link #isAuthorized()} is true.
     * <p>
     * Note this function will block until SDK get ready after
     * {@link #install(Application)} called. So if you want to call this
     * function from the main thread, you should confirm that
     * {@link #isInitialized()} is true before calling this. (Or consider using
     * {@link DeployGateCallback#onInitialized(boolean)} callback.)
     * </p>
     * 
     * @return Current user of DeployGate. May be null.
     */
    public static String getLoginUsername() {
        if (sInstance != null) {
            waitForInitialized();
            return sInstance.mLoginUsername;
        }
        return null;
    }

    /**
     * Get current app's author (i.e. distributor) username on DeployGate. You
     * may use this value to check the app was distributed by yourself or
     * someone else.
     * <p>
     * <b>Tip:</b> If you want to prevent distributing your app by someone else
     * on the DeployGate, consider using {@link #install(Application, String)}.
     * </p>
     * <p>
     * Note this function will block until SDK get ready after
     * {@link #install(Application)} called. So if you want to call this
     * function from the main thread, you should confirm that
     * {@link #isInitialized()} is true before calling this. (Or consider using
     * {@link DeployGateCallback#onInitialized(boolean)} callback.)
     * </p>
     * 
     * @return Author username of current app. May be null.
     */
    public static String getAuthorUsername() {
        if (sInstance != null) {
            waitForInitialized();
            return sInstance.mAuthor;
        }
        return null;
    }

    /**
     * Record ERROR level event on DeployGate. Log message will immediately send
     * to the server so you may see it on your dashboard. Nothing happen when
     * DeployGate is not available, i.e. {@link #isAuthorized()} is not true.
     * 
     * @param message Message body to be send. May be truncated if it's too
     *            long.
     */
    public static void logError(String message) {
        if (sInstance != null) {
            sInstance.sendLog("error", message);
        }
    }

    /**
     * Record WARN level event on DeployGate. Log message will immediately send
     * to the server so you may see it on your dashboard. Nothing happen when
     * DeployGate is not available, i.e. {@link #isAuthorized()} is not true.
     * 
     * @param message Message body to be send. May be truncated if it's too
     *            long.
     */
    public static void logWarn(String message) {
        if (sInstance != null) {
            sInstance.sendLog("warn", message);
        }
    }

    /**
     * Record DEBUG level event on DeployGate. Log message will immediately send
     * to the server so you may see it on your dashboard. Nothing happen when
     * DeployGate is not available, i.e. {@link #isAuthorized()} is not true.
     * 
     * @param message Message body to be send. May be truncated if it's too
     *            long.
     */
    public static void logDebug(String message) {
        if (sInstance != null) {
            sInstance.sendLog("debug", message);
        }
    }

    /**
     * Record INFO level event on DeployGate. Log message will immediately send
     * to the server so you may see it on your dashboard. Nothing happen when
     * DeployGate is not available, i.e. {@link #isAuthorized()} is not true.
     * 
     * @param message Message body to be send. May be truncated if it's too
     *            long.
     */
    public static void logInfo(String message) {
        if (sInstance != null) {
            sInstance.sendLog("info", message);
        }
    }

    /**
     * Record VERBOSE level event on DeployGate. Log message will immediately
     * send to the server so you may see it on your dashboard. Nothing happen
     * when DeployGate is not available, i.e. {@link #isAuthorized()} is not
     * true.
     * 
     * @param message Message body to be send. May be truncated if it's too
     *            long.
     */
    public static void logVerbose(String message) {
        if (sInstance != null) {
            sInstance.sendLog("verbose", message);
        }
    }

    @SuppressWarnings("unused")
    private/* public */static boolean isStopRequested() {
        if (sInstance != null) {
            waitForInitialized();
            return sInstance.mAppIsStopRequested;
        }
        return false;
    }

    private static void waitForInitialized() {
        try {
            sInstance.mInitializedLatch.await();
        } catch (InterruptedException e) {
            Log.w(TAG, "Interrupted while waiting initialization");
        }
    }

    private static boolean isDebuggable(Context context) {
        PackageManager manager = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
            return true;
        }
        return false;
    }

    private static class LogCatTranportWorker implements Runnable {
        private final String mPackageName;
        private final IDeployGateSdkService mService;
        private Process mProcess;
        private boolean mIsOneShot;

        public LogCatTranportWorker(String packageName, IDeployGateSdkService service,
                boolean isOneshot) {
            mPackageName = packageName;
            mService = service;
            mIsOneShot = isOneshot;
        }

        @Override
        public void run() {
            mProcess = null;
            ArrayList<String> logcatBuf = null;
            BufferedReader bufferedReader = null;
            ;
            try {
                LinkedList<String> commandLine = new LinkedList<String>();
                commandLine.add("logcat");
                logcatBuf = new ArrayList<String>();

                int MAX_LINES = 500;
                if (mIsOneShot) {
                    commandLine.add("-d");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                        commandLine.add("-t");
                        commandLine.add(String.valueOf(MAX_LINES));
                    }
                }
                commandLine.add("-v");
                commandLine.add("threadtime");
                commandLine.add("*:V");

                mProcess = Runtime.getRuntime().exec(
                        commandLine.toArray(new String[commandLine.size()]));
                bufferedReader = new BufferedReader(new InputStreamReader(
                        mProcess.getInputStream()));

                Log.v(TAG, "Start retrieving logcat");
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    logcatBuf.add(line + "\n");
                    if (mIsOneShot) {
                        if (logcatBuf.size() > MAX_LINES)
                            logcatBuf.remove(0);
                    } else {
                        if (!bufferedReader.ready()) {
                            if (send(logcatBuf))
                                logcatBuf.clear();
                            else
                                return;
                        }
                    }
                }
                if (!logcatBuf.isEmpty())
                    send(logcatBuf);
                // EOF, stop it
            } catch (IOException e) {
                Log.d(TAG, "Logcat stopped: " + e.getMessage());
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        // ignored
                    }
                }
                stop();
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

    void sendCrashReport(Throwable ex) {
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

    void sendLog(String type, String body) {
        if (mRemoteService == null)
            return;
        Bundle extras = new Bundle();
        extras.putSerializable(DeployGateEvent.EXTRA_LOG, body);
        extras.putSerializable(DeployGateEvent.EXTRA_LOG_TYPE, type);
        try {
            mRemoteService.sendEvent(mApplicationContext.getPackageName(),
                    DeployGateEvent.ACTION_SEND_CUSTOM_LOG, extras);
        } catch (RemoteException e) {
            Log.w(TAG, "failed to send custom log: " + e.getMessage());
        }
    }
}
