package com.deploygate.service;

public interface DeployGateEvent {
    // old format is too ambiguous so we are going to make it clear and safer.
    //
    // all values should be <namespace>.<content> since writing this comment :)
    //
    // namespace:
    //   ACTION => a
    //   EXTRA => e
    //   ATTRIBUTE_KEY => ak
    //
    // content:
    //   should be hyphen-separated string and be lower cases

    public static final String ACTION_INIT = "init";
    public static final String ACTION_UPDATE_AVAILABLE = "update";
    public static final String ACTION_ENABLE_LOGCAT = "enableLogcat";
    public static final String ACTION_DISABLE_LOGCAT = "disableLogcat";
    public static final String ACTION_ONESHOT_LOGCAT = "oneshotLogcat";

    public static final String ACTION_SEND_LOGCAT = "sendLogcat";
    public static final String ACTION_SEND_CRASH_REPORT = "reportCrash";
    public static final String ACTION_SEND_CUSTOM_LOG = "customLog";
    public static final String ACTION_INSTALL_UPDATE = "installUpdate";
    public static final String ACTION_OPEN_APP_DETAIL = "openAppDetail";
    public static final String ACTION_OPEN_COMMENTS = "openComments";
    public static final String ACTION_COMPOSE_COMMENT = "composeComment";
    public static final String ACTION_VISIBILITY_EVENT = "a.visibility-event";

    /**
     * @since 4.8.0
     */
    public static final String ACTION_COLLECT_DEVICE_STATES = "a.collect-device-states";

    /**
     * @since 4.9.0
     */
    public static final String ACTION_CAPTURE_CREATED = "a.capture-created";

    public static final String EXTRA_AUTHOR = "author";
    public static final String EXTRA_EXPECTED_AUTHOR = "expectedAuthor";

    /**
     * A SDK's model version queried by this key from INIT event.
     */
    public static final String EXTRA_SDK_VERSION = "sdkVersion";

    /**
     * A SDK's artifact version queried by this key from INIT event.
     *
     * @since 4.7.0
     */
    public static final String EXTRA_SDK_ARTIFACT_VERSION = "e.sdk-artifact-version";

    /**
     * Only active feature flags on this host app.
     *
     * @since 4.7.0
     */
    public static final String EXTRA_ACTIVE_FEATURE_FLAGS = "e.active-feature-flags";
    public static final String EXTRA_IS_MANAGED = "isManaged";
    public static final String EXTRA_IS_AUTHORIZED = "isAuthorized";
    public static final String EXTRA_LOGIN_USERNAME = "loginUsername";
    public static final String EXTRA_IS_STOP_REQUESTED = "isStopRequested";
    public static final String EXTRA_SERIAL = "serial";
    public static final String EXTRA_VERSION_NAME = "versionName";
    public static final String EXTRA_VERSION_CODE = "versionCode";
    public static final String EXTRA_SERIAL_MESSAGE = "serialMessage";
    public static final String EXTRA_CAN_LOGCAT = "canLogCat";
    public static final String EXTRA_IS_BOOT = "isBoot";
    public static final String EXTRA_LOG = "log";
    public static final String EXTRA_LOG_TYPE = "logType";

    /**
     * the unique id generated on the client side to identify data
     */
    public static final String EXTRA_CID = "e.cid";

    /**
     * a group id that collects instructions
     */
    public static final String EXTRA_INSTRUCTION_GROUP_ID = "e.gid";

    /**
     * session key of a logcat-stream
     */
    public static final String EXTRA_LOGCAT_STREAM_SESSION_KEY = "e.logcat-stream-session-key";

    /**
     * a marker of the bundle positioning
     */
    public static final String EXTRA_BUNDLE_POSITION = "e.bundle-position";

    /**
     * buffered-at in sdk-side, depends on device-clock.
     */
    public static final String EXTRA_BUFFERED_AT_IN_MILLI_SECONDS = "e.bufferedAt";

    /**
     * this key shouldn't be used. ref: com.deploygate.sdk.Compatibility#SERIALIZED_EXCEPTION
     */
    @Deprecated
    public static final String EXTRA_EXCEPTION = "exception";
    public static final String EXTRA_EXCEPTION_ROOT_CAUSE_CLASSNAME = "exceptionRootCauseClassName";
    public static final String EXTRA_EXCEPTION_ROOT_CAUSE_MESSAGE = "exceptionRootCauseMessage";
    public static final String EXTRA_EXCEPTION_STACKTRACES = "exceptionStackTraces";
    public static final String EXTRA_CURRENT_REVISION = "currentRevision";
    public static final String EXTRA_CURRENT_DISTRIBUTION_ID = "currentDistributionId";
    public static final String EXTRA_CURRENT_DISTRIBUTION_TITLE = "currentDistributionTitle";
    /**
     * this key shouldn't be used
     */
    @Deprecated
    public static final String EXTRA_DEPLOYGATE_VERSION_CODE = "deploygateVersionCode";
    public static final String EXTRA_COMMENT = "comment";
    public static final String EXTRA_DISTRIBUTION_USER_NAME = "distributionUserName";

    /**
     * the id of the capture.
     */
    public static final String EXTRA_CAPTURE_ID = "e.capture-id";

    /**
     * the url of the capture.
     */
    public static final String EXTRA_CAPTURE_URL = "e.capture-url";

    /**
     * the created time of the capture.
     */
    public static final String EXTRA_CAPTURE_CREATED_AT = "e.capture-created-at";

    /**
     * A event type for the app goes to foreground/background.
     */
    public static final String EXTRA_VISIBILITY_EVENT_TYPE = "e.visibility-event-type";

    /**
     * The elapsed real time since boot at the time when the app goes to foreground/background.
     * this value must be nano times.
     */
    public static final String EXTRA_VISIBILITY_EVENT_ELAPSED_REAL_TIME_IN_NANOS = "e.visibility-event-elapsed-real-time";

    /**
     * @since 4.8.0
     */
    public static final String EXTRA_TARGET_URI_FOR_REPORT_DEVICE_STATES = "e.target-uri-for-report-device-states";

    /**
     * @since 4.8.0
     */
    public static final String ATTRIBUTE_KEY_BUILD_ENVIRONMENT = "ak.build-environment";

    /**
     * @since 4.8.0
     */
    public static final String ATTRIBUTE_KEY_RUNTIME_EXTRAS = "ak.runtime-extras";

    /**
     * @since 4.8.0
     */
    public static final String ATTRIBUTE_KEY_SDK_DEVICE_STATES = "ak.sdk-device-states";

    /**
     * @since 4.8.0
     */
    public static final String ATTRIBUTE_KEY_EVENT_AT = "ak.event-at";

    interface VisibilityType {
        int BACKGROUND = 0;
        int FOREGROUND = 1;
    }

    /**
     * Not a public value. This is only for internal.
     */
    static final Long DEFAULT_EXTRA_CAPTURE_CREATE_AT = -1L;
}
