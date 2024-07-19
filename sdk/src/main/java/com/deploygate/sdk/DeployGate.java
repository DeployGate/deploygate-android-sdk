package com.deploygate.sdk;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.sdk.internal.VisibilityLifecycleCallbacks;
import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.IDeployGateSdkService;
import com.deploygate.service.IDeployGateSdkServiceCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    private static final String ACTION_DEPLOYGATE_STARTED = "com.deploygate.action.ServiceStarted";
    private static final String DEPLOYGATE_PACKAGE = "com.deploygate";
    private static final Object sPendingEventLock = new Object();

    private static DeployGate sInstance;

    private final Context mApplicationContext;
    private final DeployGateClient mDeployGateClient;
    private final HostApp mHostApp;
    private final Handler mHandler;
    private final ILogcatInstructionSerializer mLogcatInstructionSerializer;
    private final CustomLogInstructionSerializer mCustomLogInstructionSerializer;
    private final HashSet<DeployGateCallback> mCallbacks;
    private final HashMap<String, Bundle> mPendingEvents;
    private final String mExpectedAuthor;
    private String mAuthor;

    private CountDownLatch mInitializedLatch;
    private boolean mIsDeployGateAvailable;

    private boolean mAppIsManaged;
    private boolean mAppIsAuthorized;
    private boolean mAppIsStopRequested;
    private String mLoginUsername;
    private String mDistributionUserName;
    private int mCurrentRevision;
    private String mDistributionId;
    private String mDistributionTitle;

    private boolean mAppUpdateAvailable;
    private int mAppUpdateRevision;
    private String mAppUpdateVersionName;
    private int mAppUpdateVersionCode;
    private String mAppUpdateMessage;

    private IDeployGateSdkService mRemoteService;

    private final IDeployGateSdkServiceCallback mRemoteCallback = new IDeployGateSdkServiceCallback.Stub() {

        public void onEvent(
                String action,
                Bundle extras
        ) throws RemoteException {
            if (TextUtils.isEmpty(action)) {
                return;
            }

            // ensure non-null
            extras = extras != null ? extras : new Bundle();

            if (DeployGateEvent.ACTION_INIT.equals(action)) {
                onInitialized(extras.getBoolean(DeployGateEvent.EXTRA_IS_MANAGED, false), extras.getBoolean(DeployGateEvent.EXTRA_IS_AUTHORIZED, false), extras.getString(DeployGateEvent.EXTRA_LOGIN_USERNAME), extras.getString(DeployGateEvent.EXTRA_DISTRIBUTION_USER_NAME), extras.getBoolean(DeployGateEvent.EXTRA_IS_STOP_REQUESTED, false), extras.getString(DeployGateEvent.EXTRA_AUTHOR), extras.getInt(DeployGateEvent.EXTRA_CURRENT_REVISION, 0), extras.getString(DeployGateEvent.EXTRA_CURRENT_DISTRIBUTION_ID), extras.getString(DeployGateEvent.EXTRA_CURRENT_DISTRIBUTION_TITLE));
            } else if (DeployGateEvent.ACTION_UPDATE_AVAILABLE.equals(action)) {
                onUpdateArrived(extras.getInt(DeployGateEvent.EXTRA_SERIAL), extras.getString(DeployGateEvent.EXTRA_VERSION_NAME), extras.getInt(DeployGateEvent.EXTRA_VERSION_CODE), extras.getString(DeployGateEvent.EXTRA_SERIAL_MESSAGE));
            } else if (DeployGateEvent.ACTION_ONESHOT_LOGCAT.equals(action)) {
                String captureId = null;

                if (mHostApp.canUseDeviceCapture() && mDeployGateClient.isSupported(Compatibility.DEVICE_CAPTURE)) {
                    // still nullable
                    captureId = extras.getString(DeployGateEvent.EXTRA_CAPTURE_ID);
                }

                onOneshotLogcat(captureId);
            } else if (DeployGateEvent.ACTION_ENABLE_LOGCAT.equals(action)) {
                if (mDeployGateClient.isSupported(Compatibility.STREAMED_LOGCAT)) {
                    String sessionKey = extras.getString(DeployGateEvent.EXTRA_LOGCAT_STREAM_SESSION_KEY);

                    if (TextUtils.isEmpty(sessionKey)) {
                        Logger.w("%s is missing in the extra", DeployGateEvent.EXTRA_LOGCAT_STREAM_SESSION_KEY);
                        return;
                    }

                    onEnableStreamedLogcat(sessionKey);
                } else {
                    Logger.w("streamed logcat is not supported");
                }
            } else if (DeployGateEvent.ACTION_DISABLE_LOGCAT.equals(action)) {
                if (mDeployGateClient.isSupported(Compatibility.STREAMED_LOGCAT)) {
                    onDisableStreamedLogcat();
                } else {
                    Logger.w("streamed logcat is not supported");
                }
            } else if (DeployGateEvent.ACTION_COLLECT_DEVICE_STATES.equals(action)) {
                Uri targetUri = Uri.parse(extras.getString(DeployGateEvent.EXTRA_TARGET_URI_FOR_REPORT_DEVICE_STATES));
                Logger.d("collect-device-status event received: %s", targetUri);

                Logger.d("dump extras: %s", extras.toString());

                ContentResolver cr = mApplicationContext.getContentResolver();
                ContentValues cv = new ContentValues();
                HashMap<String, Object> buildEnv = new HashMap<>();
                buildEnv.put("bool", true);
                buildEnv.put("int", 1);
                buildEnv.put("long", 1L);
                buildEnv.put("float", 1.1f);
                buildEnv.put("double", 1.11111111);
                buildEnv.put("string", "1");
                HashMap<String, Object> runtimeExtras = new HashMap<>();
                runtimeExtras.put("bool", false);
                runtimeExtras.put("int", 2);
                runtimeExtras.put("long", 2L);
                runtimeExtras.put("float", 2.2f);
                runtimeExtras.put("double", 2.22222222);
                runtimeExtras.put("string", "2");
                cv.put(DeployGateEvent.KEY_BUILD_ENVIRONMENT, new JSONObject(buildEnv).toString());
                cv.put(DeployGateEvent.KEY_RUNTIME_EXTRAS, new JSONObject(runtimeExtras).toString());
                cv.put(DeployGateEvent.KEY_PACKAGE_NAME, mApplicationContext.getPackageName());
                cv.put(DeployGateEvent.KEY_EVENT_AT, System.currentTimeMillis());
                cr.insert(targetUri, cv);
            } else {
                Logger.w("%s is not supported by this sdk version", action);
            }
        }

        private void onInitialized(
                final boolean isManaged,
                final boolean isAuthorized,
                final String loginUsername,
                final String distributionUserName,
                final boolean isStopped,
                final String author,
                int currentRevision,
                String distributionId,
                String distributionTitle
        ) throws RemoteException {
            Log.v(TAG, "DeployGate service initialized");
            mAppIsManaged = isManaged;
            mAppIsAuthorized = isAuthorized;
            mAppIsStopRequested = isStopped;
            mLoginUsername = loginUsername;
            mDistributionUserName = distributionUserName;
            mAuthor = author;
            mCurrentRevision = currentRevision;
            mDistributionId = distributionId;
            mDistributionTitle = distributionTitle;

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

            mCustomLogInstructionSerializer.setDisabled(false);
            mCustomLogInstructionSerializer.connect(mRemoteService);
            mLogcatInstructionSerializer.setEnabled(true);
            mLogcatInstructionSerializer.connect(mRemoteService);

            mInitializedLatch.countDown();

            // to release a lock as soon as possible.
            flushPendingEvents();
        }

        private void onUpdateArrived(
                final int serial,
                final String versionName,
                final int versionCode,
                final String message
        ) throws RemoteException {
            mAppUpdateAvailable = true;
            mAppUpdateRevision = serial;
            mAppUpdateVersionName = versionName;
            mAppUpdateVersionCode = versionCode;
            mAppUpdateMessage = message;

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

    @SuppressWarnings("FieldCanBeLocal")
    private final VisibilityLifecycleCallbacks.OnVisibilityChangeListener mOnVisibilityChangeListener = new VisibilityLifecycleCallbacks.OnVisibilityChangeListener() {

        @Override
        public void onForeground(
                long elapsedRealtime,
                TimeUnit timeUnit
        ) {
            Bundle extras = new Bundle();
            extras.putLong(DeployGateEvent.EXTRA_VISIBILITY_EVENT_ELAPSED_REAL_TIME_IN_NANOS, timeUnit.toNanos(elapsedRealtime));
            extras.putInt(DeployGateEvent.EXTRA_VISIBILITY_EVENT_TYPE, DeployGateEvent.VisibilityType.FOREGROUND);
            invokeAction(DeployGateEvent.ACTION_VISIBILITY_EVENT, extras, true);
        }

        @Override
        public void onBackground(
                long elapsedRealtime,
                TimeUnit timeUnit
        ) {
            Bundle extras = new Bundle();
            extras.putLong(DeployGateEvent.EXTRA_VISIBILITY_EVENT_ELAPSED_REAL_TIME_IN_NANOS, timeUnit.toNanos(elapsedRealtime));
            extras.putInt(DeployGateEvent.EXTRA_VISIBILITY_EVENT_TYPE, DeployGateEvent.VisibilityType.BACKGROUND);
            invokeAction(DeployGateEvent.ACTION_VISIBILITY_EVENT, extras, true);
        }
    };

    private void requestOneshotLogcat() {
        onOneshotLogcat(null);
    }

    private void onOneshotLogcat(String captureId) {
        mLogcatInstructionSerializer.requestOneshotLogcat(captureId);
    }

    private void onEnableStreamedLogcat(String streamSessionKey) {
        if (TextUtils.isEmpty(streamSessionKey)) {
            Logger.w("sdk-origin streamed logcat requests are forbidden");
            return;
        }

        mLogcatInstructionSerializer.requestStreamedLogcat(streamSessionKey);
    }

    private void onDisableStreamedLogcat() {
        mLogcatInstructionSerializer.stopStream();
    }

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
    private DeployGate(
            Context applicationContext,
            HostApp hostApp,
            DeployGateSdkConfiguration sdkConfiguration
    ) {
        // Don't hold sdkConfiguration to avoid memory leak

        mApplicationContext = applicationContext;
        mDeployGateClient = new DeployGateClient(applicationContext, DEPLOYGATE_PACKAGE);
        mHostApp = hostApp;
        mHandler = new Handler();
        mLogcatInstructionSerializer = mHostApp.canUseLogcat ? new LogcatInstructionSerializer(mHostApp.packageName) : ILogcatInstructionSerializer.NULL_INSTANCE;
        mCustomLogInstructionSerializer = new CustomLogInstructionSerializer(mHostApp.packageName, sdkConfiguration.customLogConfiguration);
        mCallbacks = new HashSet<>();
        mPendingEvents = new HashMap<>();
        mExpectedAuthor = sdkConfiguration.appOwnerName;

        prepareBroadcastReceiver();

        if (sdkConfiguration.callback != null) {
            mCallbacks.add(sdkConfiguration.callback);
        }

        mInitializedLatch = new CountDownLatch(1);
        ((Application) applicationContext).registerActivityLifecycleCallbacks(new VisibilityLifecycleCallbacks(mOnVisibilityChangeListener));
        initService(true);
    }

    private boolean initService(boolean isBoot) {
        if (mDeployGateClient.isInstalled) {
            Log.v(TAG, "DeployGate installation detected. Initializing.");
            bindToService(isBoot);
            return true;
        } else {
            Log.v(TAG, "DeployGate is not available on this device.");
            mCustomLogInstructionSerializer.setDisabled(true);
            mLogcatInstructionSerializer.setEnabled(false);
            mInitializedLatch.countDown();
            mIsDeployGateAvailable = false;
            callbackDeployGateUnavailable();
            return false;
        }
    }

    private void prepareBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_DEPLOYGATE_STARTED);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(
                    Context context,
                    Intent intent
            ) {
                if (intent == null) {
                    return;
                }
                if (mDeployGateClient.isInstalled) {
                    bindToService(false);
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mApplicationContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            mApplicationContext.registerReceiver(receiver, filter);
        }
    }

    private void bindToService(final boolean isBoot) {
        Intent service = new Intent(IDeployGateSdkService.class.getName());
        service.setPackage(DEPLOYGATE_PACKAGE);
        mApplicationContext.bindService(service, new ServiceConnection() {
            public void onServiceConnected(
                    ComponentName name,
                    IBinder service
            ) {
                Log.v(TAG, "DeployGate service connected");
                mRemoteService = IDeployGateSdkService.Stub.asInterface(service);
                requestServiceInit(isBoot);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.v(TAG, "DeployGate service disconneced");
                mRemoteService = null;
                mCustomLogInstructionSerializer.disconnect();
                mLogcatInstructionSerializer.disconnect();
            }
        }, Context.BIND_AUTO_CREATE);
    }

    private void requestServiceInit(final boolean isBoot) {
        Bundle args = new Bundle();
        args.putBoolean(DeployGateEvent.EXTRA_IS_BOOT, isBoot);
        args.putBoolean(DeployGateEvent.EXTRA_CAN_LOGCAT, mHostApp.canUseLogcat);
        args.putString(DeployGateEvent.EXTRA_EXPECTED_AUTHOR, mExpectedAuthor);
        args.putInt(DeployGateEvent.EXTRA_SDK_VERSION, mHostApp.sdkVersion);
        args.putString(DeployGateEvent.EXTRA_SDK_ARTIFACT_VERSION, mHostApp.sdkArtifactVersion);
        args.putInt(DeployGateEvent.EXTRA_ACTIVE_FEATURE_FLAGS, mHostApp.activeFeatureFlags);
        try {
            mRemoteService.init(mRemoteCallback, mHostApp.packageName, args);
        } catch (RemoteException e) {
            Log.w(TAG, "DeployGate service failed to be initialized.");
        }
    }

    /**
     * Send an event to the client application
     *
     * @param action
     *         to be sent
     * @param extras
     *         to be sent
     * @param allowPending
     *         Allow queueing events to send them after a service connection is established (since 4.6.0)
     */
    private void invokeAction(
            String action,
            Bundle extras,
            boolean allowPending
    ) {
        extras = extras != null ? extras : new Bundle();

        if (mRemoteService == null) {
            if (allowPending) {
                synchronized (sPendingEventLock) {
                    mPendingEvents.put(action, extras);
                }
            }

            return;
        }
        try {
            mRemoteService.sendEvent(mHostApp.packageName, action, extras);
        } catch (RemoteException e) {
            Log.w(TAG, "failed to invoke " + action + " action: " + e.getMessage());
        }
    }

    private void flushPendingEvents() {
        // cannot re-enqueue events for now
        synchronized (sPendingEventLock) {
            Iterator<Map.Entry<String, Bundle>> iterator = mPendingEvents.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, Bundle> entry = iterator.next();
                invokeAction(entry.getKey(), entry.getValue(), false);
                iterator.remove();
            }
        }
    }

    /**
     * Clear the initiated DeployGate instance.
     * <p>
     * This method is only for testing so breaking changes would may happen.
     */
    static void clear() {
        sInstance = null;
    }


    /**
     * Install DeployGate on your application instance. Call this method inside
     * of your {@link Application#onCreate()}, in ContentProvider or AndroidX Startup Initializer only once.
     *
     * @param context Your application's Context. SDK will use {@code context.getApplicationContext} and don't hold this object reference.
     * @param sdkConfiguration sdk configuration {@link DeployGateSdkConfiguration}
     *
     * @throws IllegalStateException
     *         if this called twice
     * @since 4.7.0
     */

    public static void install(Context context, DeployGateSdkConfiguration sdkConfiguration) {
        if (sInstance != null) {
            Log.w(TAG, "DeployGate.install was already called. Ignoring.");
            return;
        }

        Context application = context.getApplicationContext();

        HostApp hostApp = new HostApp(
                application,
                sdkConfiguration
        );

        if (!hostApp.isSdkEnabled) {
            return;
        }

        if (sdkConfiguration.isCrashReportingEnabled) {
            Thread.setDefaultUncaughtExceptionHandler(new DeployGateUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()));
        } else {
            Logger.d("DeployGateSDK crash reporting hasn't started");
        }

        sInstance = new DeployGate(
                application,
                hostApp,
                sdkConfiguration
        );
    }

    /**
     * Install DeployGate on your application instance. Call this method inside
     * of your {@link Application#onCreate()} once.
     * <p>
     * On a release build, which has <code>android:isDebuggable</code> set false on
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
     * @param app
     *         Application instance, typically just pass <em>this</em>.
     *
     * @throws IllegalStateException
     *         if this called twice
     * @since r1
     * @deprecated since 4.7.0. Use {@link DeployGate#install(Context, DeployGateSdkConfiguration)} instead.
     */
    public static void install(Application app) {
        install(app, (String) null);
    }

    /**
     * Install DeployGate on your application instance. Call this method inside
     * of your {@link Application#onCreate()} once.
     * <p>
     * On a release build, which has <code>android:isDebuggable</code> set false on
     * AndroidManifest.xml, this function will do nothing. If you want to enable
     * DeployGate on a release build, consider using
     * {@link #install(Application, String, DeployGateCallback, boolean)}
     * instead.
     * </p>
     *
     * @param app
     *         Application instance, typically just pass <em>this</em>.
     * @param author
     *         author username of this app.
     *
     * @throws IllegalStateException
     *         if this called twice
     * @since r2
     * @deprecated since 4.7.0. Use {@link DeployGate#install(Context, DeployGateSdkConfiguration)} instead.
     */
    public static void install(
            Application app,
            String author
    ) {
        install(app, author, null);
    }

    /**
     * Install DeployGate on your application instance and register a callback
     * listener. Call this method inside of your {@link Application#onCreate()}
     * once.
     * <p>
     * On a release build, which has <code>android:isDebuggable</code> set false on
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
     * @param app
     *         Application instance, typically just pass <em>this</em>.
     * @param callback
     *         Callback interface to listen events.
     *
     * @throws IllegalStateException
     *         if this called twice
     * @since r1
     * @deprecated since 4.7.0. Use {@link DeployGate#install(Context, DeployGateSdkConfiguration)} instead.
     */
    public static void install(
            Application app,
            DeployGateCallback callback
    ) {
        install(app, null, callback);
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
     * @param app
     *         Application instance, typically just pass <em>this</em>.
     * @param forceApplyOnReleaseBuild
     *         if you want to keep DeployGate alive on
     *         the release build, set this true.
     *
     * @throws IllegalStateException
     *         if this called twice
     * @since r4.2
     * @deprecated since 4.7.0. Use {@link DeployGate#install(Context, DeployGateSdkConfiguration)} instead.
     */
    public static void install(
            Application app,
            boolean forceApplyOnReleaseBuild
    ) {
        install(app, null, null, forceApplyOnReleaseBuild);
    }

    /**
     * Install DeployGate on your application instance and register a callback
     * listener. Call this method inside of your {@link Application#onCreate()}
     * once.
     * <p>
     * On a release build, which has <code>android:isDebuggable</code> set false on
     * AndroidManifest.xml, this function will do nothing. If you want to enable
     * DeployGate on a release build, consider using
     * {@link #install(Application, String, DeployGateCallback, boolean)}
     * instead.
     * </p>
     *
     * @param app
     *         Application instance, typically just pass <em>this</em>.
     * @param author
     *         author username of this app.
     * @param callback
     *         Callback interface to listen events.
     *
     * @throws IllegalStateException
     *         if this called twice
     * @since r2
     * @deprecated since 4.7.0. Use {@link DeployGate#install(Context, DeployGateSdkConfiguration)} instead.
     */
    public static void install(
            Application app,
            String author,
            DeployGateCallback callback
    ) {
        install(app, author, callback, false);
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
     * @param app
     *         Application instance, typically just pass <em>this</em>.
     * @param callback
     *         Callback interface to listen events. Can be null.
     * @param forceApplyOnReleaseBuild
     *         if you want to keep DeployGate alive on
     *         the release build, set this true.
     *
     * @throws IllegalStateException
     *         if this called twice
     * @since r1
     * @deprecated since 4.7.0. Use {@link DeployGate#install(Context, DeployGateSdkConfiguration)} instead.
     */
    public static void install(
            Application app,
            DeployGateCallback callback,
            boolean forceApplyOnReleaseBuild
    ) {
        install(app, null, callback, forceApplyOnReleaseBuild);
    }

    /**
     * Install DeployGate on your application instance and register a callback
     * listener. Call this method inside of your {@link Application#onCreate()}
     * once.
     *
     * @param app
     *         Application instance, typically just pass <em>this</em>.
     * @param author
     *         author username of this app. Can be null.
     * @param callback
     *         Callback interface to listen events. Can be null.
     * @param forceApplyOnReleaseBuild
     *         if you want to keep DeployGate alive on
     *         the release build, set this true.
     *
     * @since r2
     * @deprecated since 4.7.0. Use {@link DeployGate#install(Context, DeployGateSdkConfiguration)} instead.
     */
    public static void install(
            Application app,
            String author,
            DeployGateCallback callback,
            boolean forceApplyOnReleaseBuild
    ) {
        install(app, author, callback, forceApplyOnReleaseBuild, new CustomLogConfiguration.Builder().build());
    }

    /**
     * Install DeployGate on your application instance and register a callback
     * listener. Call this method inside of your {@link Application#onCreate()}
     * once.
     *
     * @param app
     *         Application instance, typically just pass <em>this</em>.
     * @param author
     *         author username of this app. Can be null.
     * @param callback
     *         Callback interface to listen events. Can be null.
     * @param forceApplyOnReleaseBuild
     *         if you want to keep DeployGate alive on
     *         the release build, set this true.
     * @param customLogConfiguration
     *         set a configuration for custom logging
     *
     * @since 4.4.0
     * @deprecated since 4.7.0. Use {@link DeployGate#install(Context, DeployGateSdkConfiguration)} instead.
     */
    public static void install(
            Application app,
            String author,
            DeployGateCallback callback,
            boolean forceApplyOnReleaseBuild,
            CustomLogConfiguration customLogConfiguration
    ) {
        install(
                app,
                new DeployGateSdkConfiguration.Builder()
                        .setAppOwnerName(author)
                        .setCustomLogConfiguration(customLogConfiguration)
                        .setEnabledOnNonDebuggableBuild(forceApplyOnReleaseBuild)
                        .setCallback(callback)
                        .build()
        );
    }

    /**
     * Request refreshing cached session values (e.g., isAuthorized, etc.) to
     * the DeployGate service. Nothing happens if this called before
     * {@link #install(Application)} or when refreshing is already in progress.
     * Note that after calling this, {@link #isInitialized()} will changed to
     * false immediately and any call to <code>is*()</code> will be blocked until
     * refreshing get finished.
     *
     * @since r1
     */
    public static void refresh() {
        if (sInstance != null) {
            sInstance.refreshInternal();
        }
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
     * @param listener
     *         callback listener
     * @param refreshImmediately
     *         if you want to receive current states, set this
     *         true.
     *
     * @since r1
     */
    public static void registerCallback(
            DeployGateCallback listener,
            boolean refreshImmediately
    ) {
        if (sInstance == null) {
            return;
        }
        if (listener == null) {
            return;
        }

        sInstance.registerCallbackInternal(listener, refreshImmediately);
    }

    private void registerCallbackInternal(
            DeployGateCallback listener,
            boolean callbackImmediately
    ) {
        mCallbacks.add(listener);
        if (callbackImmediately) {
            refresh();
        }
    }

    /**
     * Unregister a callback listener. If the listener was not registered, just
     * ignored.
     *
     * @param listener
     *         callback listener to be removed
     *
     * @since r1
     */
    public static void unregisterCallback(DeployGateCallback listener) {
        if (sInstance == null) {
            return;
        }
        if (listener == null) {
            return;
        }

        sInstance.mCallbacks.remove(listener);
    }

    /**
     * Get whether SDK is completed its intialization process and ready after
     * {@link #install(Application)}. This call will never blocked.
     *
     * @return true if SDK is ready. false otherwise. If no install() called
     * ever, this always returns false.
     *
     * @since r1
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
     * no install() called ever, this always returns false.
     *
     * @since r1
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
     * otherwise. If no install() called ever, this always returns
     * false.
     *
     * @since r1
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
     * this application. false otherwise. If no install() called ever,
     * this always returns false.
     *
     * @since r1
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
     *
     * @since r1
     */
    public static String getLoginUsername() {
        if (sInstance != null) {
            waitForInitialized();
            return sInstance.mLoginUsername;
        }
        return null;
    }

    /**
     * Get current app's author (e.g., User or Organzation) name on DeployGate. You
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
     * @return Owner User or Organization of current app. May be null.
     *
     * @since r2
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
     * @param message
     *         Message body to be send. May be truncated if it's too
     *         long.
     *
     * @since r1
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
     * @param message
     *         Message body to be send. May be truncated if it's too
     *         long.
     *
     * @since r1
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
     * @param message
     *         Message body to be send. May be truncated if it's too
     *         long.
     *
     * @since r1
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
     * @param message
     *         Message body to be send. May be truncated if it's too
     *         long.
     *
     * @since r1
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
     * @param message
     *         Message body to be send. May be truncated if it's too
     *         long.
     *
     * @since r1
     */
    public static void logVerbose(String message) {
        if (sInstance != null) {
            sInstance.sendLog("verbose", message);
        }
    }

    @SuppressWarnings("unused")
    private/* public */ static boolean isStopRequested() {
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

    static DeployGate getInstance() {
        return sInstance;
    }

    void sendCrashReport(/* non-null */ Throwable ex) {
        if (mRemoteService == null) {
            return;
        }

        Bundle extras = new Bundle();
        try {
            if (mDeployGateClient.isSupported(Compatibility.SERIALIZED_EXCEPTION)) {
                Throwable rootCause = getRootCause(ex);
                String msg = rootCause.getMessage();
                extras.putString(DeployGateEvent.EXTRA_EXCEPTION_ROOT_CAUSE_CLASSNAME, rootCause.getClass().getName());
                extras.putString(DeployGateEvent.EXTRA_EXCEPTION_ROOT_CAUSE_MESSAGE, msg != null ? msg : "");
                extras.putString(DeployGateEvent.EXTRA_EXCEPTION_STACKTRACES, Log.getStackTraceString(ex));
            } else {
                extras.putSerializable(DeployGateEvent.EXTRA_EXCEPTION, ex);
            }

            mRemoteService.sendEvent(mHostApp.packageName, DeployGateEvent.ACTION_SEND_CRASH_REPORT, extras);
        } catch (RemoteException e) {
            Log.w(TAG, "failed to send crash report: " + e.getMessage());
        }
    }

    /* non-null */
    private static Throwable getRootCause(/* non-null */ Throwable ex) {
        Throwable cause = ex;
        LinkedList<Throwable> throwables = new LinkedList<>();

        while (cause != null && !throwables.contains(cause)) {
            throwables.add(cause);
            cause = cause.getCause();
        }

        return throwables.getLast();
    }

    void sendLog(
            String type,
            String body
    ) {
        mCustomLogInstructionSerializer.requestSendingLog(new CustomLog(type, body));
    }

    static boolean isFeatureSupported(Compatibility compatibility) {
        if (sInstance == null) {
            return false;
        }

        return sInstance.mDeployGateClient.isSupported(compatibility);
    }

    /**
     * Capture current LogCat and send it. This call will work asynchronously.
     * Capturing LogCat requires <code>android.permission.READ_LOGS</code> is
     * declared on your app AndroidManifest.xml, or your app is running on
     * Android 4.1 or higher. If LogCat is not available, this function simply
     * does nothing.
     *
     * @since r3
     */
    public static void requestLogCat() {
        if (sInstance != null) {
            sInstance.requestOneshotLogcat();
        }
    }


    /**
     * Returns the revision of the app on DeployGate.
     * The revision number is automatically incremented integer value every time you upload a build to DeployGate,
     * so you can identify the build explicitly.
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function always returns 0.
     *
     * @return revision number of the app, or 0 if DeployGate is older than v1.7.0 (39)
     *
     * @since r4
     */
    public static int getCurrentRevision() {
        if (sInstance != null) {
            return sInstance.mCurrentRevision;
        }
        return 0;
    }

    /**
     * Returns the URL of the distribution if the app was installed through Distribution Page.
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function always returns null.
     *
     * @return URL of the distribution page, null if the app was not installed through Distribution Page or DeployGate is older than v1.7.0 (39)
     *
     * @since r4
     */
    public static String getDistributionUrl() {
        if (sInstance == null) {
            return null;
        }

        if (TextUtils.isEmpty(sInstance.mDistributionId)) {
            return null;
        }

        return "https://deploygate.com/distributions/" + sInstance.mDistributionId;
    }

    /**
     * Returns the ID (40 digits hex string appears in URL) of the distribution if the app was installed through Distribution Page.
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function always returns null.
     *
     * @return ID of the distribution page, null if the app was not installed through Distribution Page or DeployGate is older than v1.7.0 (39)
     *
     * @since r4
     */
    public static String getDistributionId() {
        if (sInstance == null) {
            return null;
        }

        if (TextUtils.isEmpty(sInstance.mDistributionId)) {
            return null;
        }

        return sInstance.mDistributionId;
    }

    /**
     * Returns the title of the distribution if the app was installed through Distribution Page.
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function always returns null.
     *
     * @return Title of the distribution page, null if the app was not installed through Distribution Page or DeployGate is older than v1.7.0 (39)
     *
     * @since r4
     */
    public static String getDistributionTitle() {
        if (sInstance == null) {
            return null;
        }

        if (TextUtils.isEmpty(sInstance.mDistributionTitle)) {
            return null;
        }

        return sInstance.mDistributionTitle;
    }

    /**
     * Use {@link DeployGate#getDeployGateLongVersionCode}
     *
     * @return Version code of DeployGate in integer. Please refer to {@link DeployGate#getDeployGateLongVersionCode()}
     */
    @Deprecated
    public static int getDeployGateVersionCode() {
        if (sInstance == null) {
            return 0;
        }

        return (int) sInstance.mDeployGateClient.versionCode;
    }

    /**
     * Returns android:versionCode of DeployGate app.
     *
     * @return Version code of DeployGate, or 0 if DeployGate is older than v1.7.0 (39)
     *
     * @since 4.4.0
     */
    public static long getDeployGateLongVersionCode() {
        if (sInstance == null) {
            return 0;
        }

        return sInstance.mDeployGateClient.versionCode;
    }

    /**
     * Check whether there's update of the app.
     * You can get detailed information via {@link #getUpdateRevision()}, {@link #getUpdateVersionCode()}, {@link #getUpdateVersionName()}, and {@link #getUpdateMessage()}.
     *
     * @return true if there's an update, false otherwise.
     *
     * @since r4.2
     */
    public static boolean hasUpdate() {
        if (sInstance == null) {
            return false;
        }

        return sInstance.mAppUpdateAvailable;
    }

    /**
     * Returns the revision number of the update. The value is only valid when {@link #hasUpdate()} is true.
     *
     * @return Revision number of the update.
     *
     * @since r4.2
     */
    public static int getUpdateRevision() {
        if (sInstance == null) {
            return 0;
        }

        return sInstance.mAppUpdateRevision;
    }

    /**
     * Returns the android:versionCode of the update. The value is only valid when {@link #hasUpdate()} is true.
     *
     * @return Revision number of the update.
     *
     * @since r4.2
     */
    public static int getUpdateVersionCode() {
        if (sInstance == null) {
            return 0;
        }

        return sInstance.mAppUpdateVersionCode;
    }

    /**
     * Returns the android:versionName of the update. The value is only valid when {@link #hasUpdate()} is true.
     *
     * @return Revision number of the update.
     *
     * @since r4.2
     */
    public static String getUpdateVersionName() {
        if (sInstance == null) {
            return null;
        }

        return sInstance.mAppUpdateVersionName;
    }

    /**
     * Returns the message attached to the build.
     * If the app was installed via Distribution Page (i.e. {@link #getDistributionUrl() is not null,}
     * this method returns Release Note that was entered when you published a new version on the page.
     * Otherwise this method returns the message that was entered when you upload a new build to DeployGate.
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function always returns null.
     *
     * @return The message attached to the build
     *
     * @since r4
     */
    public static String getUpdateMessage() {
        if (sInstance == null || !isFeatureSupported(Compatibility.UPDATE_MESSAGE_OF_BUILD)) {
            return null;
        }

        return sInstance.mAppUpdateMessage;
    }

    /**
     * Start the installation of the latest version of the app if available. No effects if there's no update.
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function produces no effect.
     *
     * @since r4
     */
    public static void installUpdate() {
        if (sInstance == null) {
            return;
        }

        sInstance.invokeAction(DeployGateEvent.ACTION_INSTALL_UPDATE, null, false);
    }

    /**
     * Open comments screen for the distribution of the app installed.
     * No effect if the app was installed via Distribution Page (i.e. {@link #getDistributionUrl() is null.}
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function produces no effect.
     *
     * @since r4
     */
    public static void openComments() {
        if (sInstance == null || sInstance.mDistributionId == null) {
            return;
        }

        sInstance.invokeAction(DeployGateEvent.ACTION_OPEN_COMMENTS, null, false);
    }

    /**
     * Open comment composer screen for the distribution of the app installed.
     * No effect if the app was installed via Distribution Page (i.e. {@link #getDistributionUrl() is null.}
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function produces no effect.
     *
     * @since r4
     */
    public static void composeComment() {
        composeComment(null);
    }

    /**
     * Open comment composer screen for the distribution of the app installed with pre filled string.
     * No effect if the app was installed via Distribution Page (i.e. {@link #getDistributionUrl() is null.}
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function produces no effect.
     *
     * @param defaultComment
     *         Default comment set to the editor
     *
     * @since r4
     */
    public static void composeComment(String defaultComment) {
        if (sInstance == null || sInstance.mDistributionId == null) {
            return;
        }

        Bundle extras = new Bundle();
        extras.putString(DeployGateEvent.EXTRA_COMMENT, defaultComment);
        sInstance.invokeAction(DeployGateEvent.ACTION_COMPOSE_COMMENT, extras, false);
    }

    /**
     * Get current user's name on Distribution Page. Default name is randomly generated string (like "[abcd1234]").
     * <p>
     * Requires DeployGate v1.7.0 or higher installed, otherwise this function returns null.
     *
     * @return User's display name on DeployGate. May be null.
     *
     * @since r4
     */
    public static String getDistributionUserName() {
        if (sInstance == null) {
            return null;
        }

        return sInstance.mDistributionUserName;
    }
}
