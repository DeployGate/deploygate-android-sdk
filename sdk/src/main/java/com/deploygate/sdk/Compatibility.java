package com.deploygate.sdk;

final class Compatibility {
    /**
     * All values must be *inclusive*. {@link Integer#MAX_VALUE} means the version is not fixed yet.
     */
    static final class ClientVersion {
        static final int SUPPORT_UPDATE_MESSAGE_OF_BUILD = 39;
        static final int SUPPORT_SERIALIZED_EXCEPTION = 42;
        static final int SUPPORT_LOGCAT_BUNDLE = Integer.MAX_VALUE;
    }

    static boolean isUpdateMessageOfBuildSupported() {
        return DeployGate.getDeployGateVersionCode() >= ClientVersion.SUPPORT_UPDATE_MESSAGE_OF_BUILD;
    }

    /**
     * older clients crash due to ClassNotFound if SDK sends custom exceptions.
     */
    static boolean isSerializedExceptionSupported() {
        return DeployGate.getDeployGateVersionCode() >= ClientVersion.SUPPORT_SERIALIZED_EXCEPTION;
    }

    /**
     * older clients emits only the single set of lines.
     */
    static boolean isLogcatBundleSupported() {
        return DeployGate.getDeployGateVersionCode() >= ClientVersion.SUPPORT_LOGCAT_BUNDLE;
    }
}
