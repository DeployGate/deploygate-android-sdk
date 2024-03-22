package com.deploygate.sdk;

/** @noinspection ALL*/
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

        public DeployGateSdkConfiguration build() {
            return new DeployGateSdkConfiguration();
        }
    }
}
