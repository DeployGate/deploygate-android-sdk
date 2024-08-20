package com.deploygate.sdk;

public final class DeployGateSdkConfiguration {
    private DeployGateSdkConfiguration() {
    }

    public static final class Builder {
        public Builder() {
        }

        public Builder setCustomLogConfiguration(CustomLogConfiguration customLogConfiguration) {
            return this;
        }

        public Builder setAppOwnerName(String appOwnerName) {
            return this;
        }

        public Builder setDisabled(boolean disabled) {
            return this;
        }

        public Builder setEnabledOnNonDebuggableBuild(boolean enabledOnNonDebuggableBuild) {
            return this;
        }

        public Builder setCaptureEnabled(boolean captureEnabled) {
            return this;
        }

        public Builder setCrashReportingEnabled(boolean crashReportingEnabled) {
            return this;
        }

        public Builder setCallback(DeployGateCallback callback) {
            return this;
        }

        public Builder setInitializeCallback(DeployGateInitializeCallback initializeCallback) {
            return this;
        }

        public Builder setStatusChangeCallback(DeployGateStatusChangeCallback statusChangeCallback) {
            return this;
        }

        public Builder setUpdateAvailableCallback(DeployGateUpdateAvailableCallback updateAvailableCallback) {
            return this;
        }


        public DeployGateSdkConfiguration build() {
            return new DeployGateSdkConfiguration();
        }
    }
}
