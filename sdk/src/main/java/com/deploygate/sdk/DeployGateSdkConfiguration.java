package com.deploygate.sdk;

import com.deploygate.sdk.internal.annotations.Experimental;

public final class DeployGateSdkConfiguration {
    final CustomLogConfiguration customLogConfiguration;

    final boolean isDisabled;
    final boolean isEnabledOnNonDebuggableBuild;

    final String appOwnerName;

    final boolean isCrashReportingEnabled;

    final DeployGateInitializeCallback initializeCallback;
    final DeployGateStatusChangeCallback statusChangeCallback;
    final DeployGateUpdateAvailableCallback updateAvailableCallback;

    final boolean isCaptureEnabled;

    private DeployGateSdkConfiguration(
            Builder builder
    ) {
        this(
                builder.isDisabled,
                builder.customLogConfiguration,
                builder.isEnabledOnNonDebuggableBuild,
                builder.appOwnerName,
                builder.isCrashReportingEnabled,
                builder.isCaptureEnabled,
                builder.initializeCallback,
                builder.statusChangeCallback,
                builder.updateAvailableCallback
        );
    }

    private DeployGateSdkConfiguration(
            boolean isDisabled,
            CustomLogConfiguration customLogConfiguration,
            boolean isEnabledOnNonDebuggableBuild,
            String appOwnerName,
            boolean isCrashReportingEnabled,
            boolean isCaptureEnabled,
            DeployGateInitializeCallback initializeCallback,
            DeployGateStatusChangeCallback statusChangeCallback,
            DeployGateUpdateAvailableCallback updateAvailableCallback
    ) {
        this.customLogConfiguration = customLogConfiguration;
        this.isDisabled = isDisabled;
        this.isEnabledOnNonDebuggableBuild = isEnabledOnNonDebuggableBuild;
        this.appOwnerName = appOwnerName;
        this.isCrashReportingEnabled = isCrashReportingEnabled;
        this.isCaptureEnabled = isCaptureEnabled;
        this.initializeCallback = initializeCallback;
        this.statusChangeCallback = statusChangeCallback;
        this.updateAvailableCallback = updateAvailableCallback;
    }

    public static final class Builder {
        private CustomLogConfiguration customLogConfiguration = new CustomLogConfiguration.Builder().build();

        private boolean isDisabled = false;
        private boolean isEnabledOnNonDebuggableBuild = false;

        private String appOwnerName = null;

        private boolean isCrashReportingEnabled = true;

        private boolean isCaptureEnabled = true;

        private DeployGateInitializeCallback initializeCallback = null;
        private DeployGateStatusChangeCallback statusChangeCallback = null;
        private DeployGateUpdateAvailableCallback updateAvailableCallback = null;

        public Builder() {
        }

        /**
         * Set a custom log configuration
         *
         * @param customLogConfiguration
         *   a configuration object for custom logs like {@link DeployGate#logDebug(String)}
         * @see CustomLogConfiguration
         * @return self
         */
        @Experimental
        public Builder setCustomLogConfiguration(CustomLogConfiguration customLogConfiguration) {
            this.customLogConfiguration = customLogConfiguration;
            return this;
        }

        /**
         * Ensure the authority of this app to prevent casual redistribution via DeployGate.
         *
         * @param appOwnerName
         *    A name of this app's owner on DeployGate.
         * @return self
         */
        public Builder setAppOwnerName(String appOwnerName) {
            this.appOwnerName = appOwnerName;
            return this;
        }

        /**
         * Disable all SDK features.
         *
         * @param disabled
         *   Specify true if you would like to disable SDK completely. Defaults to false.
         * @return self
         */
        public Builder setDisabled(boolean disabled) {
            isDisabled = disabled;
            return this;
        }

        /**
         * Enable SDK even on non-debuggable builds.
         *
         * @param enabledOnNonDebuggableBuild
         *   Specify true if you would like to enable SDK on non-debuggable builds. Defaults to false.
         * @return self
         */
        public Builder setEnabledOnNonDebuggableBuild(boolean enabledOnNonDebuggableBuild) {
            isEnabledOnNonDebuggableBuild = enabledOnNonDebuggableBuild;
            return this;
        }

        /**
         * Enable DeployGate Capture feature.
         *
         * @param captureEnabled
         *   Specify true if you would like to use DeployGate Capture feature if available. Otherwise, false. Defaults to true.
         * @return self
         */
        @Experimental
        public Builder setCaptureEnabled(boolean captureEnabled) {
            isCaptureEnabled = captureEnabled;
            return this;
        }

        /**
         * Enable DeployGate Crash reporting feature.
         *
         * @param crashReportingEnabled
         *   Specify true if you would like to use DeployGate Crash reporting feature. Otherwise, false. Defaults to true.
         * @return self
         */
        public Builder setCrashReportingEnabled(boolean crashReportingEnabled) {
            isCrashReportingEnabled = crashReportingEnabled;
            return this;
        }

        /**
         * Set a callback of the communication events between DeployGate client app and this app.
         *
         * @param callback
         *   Set an instance of callback. The reference won't be released. Please use {@link DeployGate#registerCallback(DeployGateCallback, boolean)} for memory sensitive works.
         * @return self
         * @deprecated since 4.9.0. Use each extended callback interfaces instead.
         * @see #setInitializeCallback(DeployGateInitializeCallback)
         * @see #setStatusChangeCallback(DeployGateStatusChangeCallback)
         * @see #setUpdateAvailableCallback(DeployGateUpdateAvailableCallback)
         */
        @Deprecated
        public Builder setCallback(final DeployGateCallback callback) {
            this.setInitializeCallback(new DeployGateInitializeCallback() {
                @Override
                public void onInitialized(boolean isServiceAvailable) {
                    callback.onInitialized(isServiceAvailable);
                }
            });
            this.setStatusChangeCallback(new DeployGateStatusChangeCallback() {
                @Override
                public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {
                    callback.onStatusChanged(isManaged, isAuthorized, loginUsername, isStopped);
                }
            });
            this.setUpdateAvailableCallback(new DeployGateUpdateAvailableCallback() {
                @Override
                public void onUpdateAvailable(int revision, String versionName, int versionCode) {
                    callback.onUpdateAvailable(revision, versionName, versionCode);
                }
            });
            return this;
        }

        public Builder setInitializeCallback(DeployGateInitializeCallback initializeCallback) {
            this.initializeCallback = initializeCallback;
            return this;
        }

        public Builder setStatusChangeCallback(DeployGateStatusChangeCallback statusChangeCallback) {
            this.statusChangeCallback = statusChangeCallback;
            return this;
        }

        public Builder setUpdateAvailableCallback(DeployGateUpdateAvailableCallback updateAvailableCallback) {
            this.updateAvailableCallback = updateAvailableCallback;
            return this;
        }

        /**
         * @return a new sdk configuration.
         */
        public DeployGateSdkConfiguration build() {
            if (isDisabled) {
                // Create a new builder instance to release all references just in case.
                return new DeployGateSdkConfiguration(new Builder().setDisabled(true));
            } else {
                return new DeployGateSdkConfiguration(this);
            }
        }
    }
}
