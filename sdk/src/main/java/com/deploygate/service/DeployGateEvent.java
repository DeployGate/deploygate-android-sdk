package com.deploygate.service;

public interface DeployGateEvent {
    // old format is too ambiguous so we are going to make it clear and safer.
    //
    // all values should be <namespace>.<content> since writing this comment :)
    //
    // namespace:
    //   ACTION => a
    //   EXTRA => e
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
    public static final String ACTION_DETECT_SCREENSHOT = "a.detect-screenshot";
    public static final String ACTION_OPEN_CAPTURE = "a.open-capture";

    public static final String EXTRA_AUTHOR = "author";
    public static final String EXTRA_EXPECTED_AUTHOR = "expectedAuthor";
    public static final String EXTRA_SDK_VERSION = "sdkVersion";
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
    public static final String EXTRA_SCREENSHOT_URI = "e.screenshot-uri";
    public static final String EXTRA_CAPTURE_ID = "e.capture-id";

    /**
     * the timestamp of the capture.
     * this value must be compatible EpochMillis.
     */
    public static final String EXTRA_CAPTURE_EVENT_AT = "e.capture-event-at";
}
