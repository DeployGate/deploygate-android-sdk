package com.deploygate.sdk;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test class will make sure all *public* interfaces are defined as expected.
 */
@RunWith(AndroidJUnit4.class)
public class DeployGateSdkConfigurationInterfaceTest {

    @NonNull
    DeployGateSdkConfiguration.Builder builder;

    CustomLogConfiguration customLogConfiguration;
    String appOwnerName;
    DeployGateCallback callback;
    DeployGateInitializeCallback initializeCallback;
    DeployGateStatusChangeCallback statusChangeCallback;
    DeployGateUpdateAvailableCallback updateAvailableCallback;
    DeployGateCaptureCreateCallback captureCreateCallback;

    @Before
    public void setUp() {
        builder = new DeployGateSdkConfiguration.Builder();
        customLogConfiguration = new CustomLogConfiguration.Builder().build();
        appOwnerName = "owner";
        callback = new DeployGateCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {
                // no-op
            }

            @Override
            public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {
                // no-op
            }

            @Override
            public void onUpdateAvailable(int revision, String versionName, int versionCode) {
                // no-op
            }
        };
        initializeCallback = new DeployGateInitializeCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {
                // no-op
            }
        };
        statusChangeCallback = new DeployGateStatusChangeCallback() {
            @Override
            public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {
                // no-op
            }
        };
        updateAvailableCallback = new DeployGateUpdateAvailableCallback() {
            @Override
            public void onUpdateAvailable(int revision, String versionName, int versionCode) {
                // no-op
            }
        };
        captureCreateCallback = new DeployGateCaptureCreateCallback() {
            @Override
            public void onCaptureCreated(String captureUrl, long createdAtMillis) {
                // no-op
            }
        };
    }

    @Test
    public void builder__set_method() {
        // setCustomLogConfiguration
        builder.setCustomLogConfiguration(null);
        builder.setCustomLogConfiguration(customLogConfiguration);
        builder.setCustomLogConfiguration(new CustomLogConfiguration.Builder().build());

        // setAppOwnerName
        builder.setAppOwnerName(null);
        builder.setAppOwnerName(appOwnerName);
        builder.setAppOwnerName("owner");

        // setDisabled
        builder.setDisabled(true);
        builder.setDisabled(false);

        // setEnabledOnNonDebuggableBuild
        builder.setEnabledOnNonDebuggableBuild(true);
        builder.setEnabledOnNonDebuggableBuild(false);

        // setCaptureEnabled
        builder.setCaptureEnabled(true);
        builder.setCaptureEnabled(false);

        // setCrashReportingEnabled
        builder.setCrashReportingEnabled(true);
        builder.setCrashReportingEnabled(false);

        // setCallback
        builder.setCallback(null);
        builder.setCallback(callback);
        builder.setCallback(new DeployGateCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {
                // no-op
            }

            @Override
            public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {
                // no-op
            }

            @Override
            public void onUpdateAvailable(int revision, String versionName, int versionCode) {
                // no-op
            }
        });

        // setInitializeCallback
        builder.setInitializeCallback(null);
        builder.setInitializeCallback(initializeCallback);
        builder.setInitializeCallback(new DeployGateInitializeCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {
                // no-op
            }
        });

        // setStatusChangeCallback
        builder.setStatusChangeCallback(null);
        builder.setStatusChangeCallback(statusChangeCallback);
        builder.setStatusChangeCallback(new DeployGateStatusChangeCallback() {
            @Override
            public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {
                // no-op
            }
        });

        // setUpdateAvailableCallback
        builder.setUpdateAvailableCallback(null);
        builder.setUpdateAvailableCallback(updateAvailableCallback);
        builder.setUpdateAvailableCallback(new DeployGateUpdateAvailableCallback() {
            @Override
            public void onUpdateAvailable(int revision, String versionName, int versionCode) {
                // no-op
            }
        });

        // setCaptureCreateCallback
        builder.setCaptureCreateCallback(null);
        builder.setCaptureCreateCallback(captureCreateCallback);
        builder.setCaptureCreateCallback(new DeployGateCaptureCreateCallback() {
            @Override
            public void onCaptureCreated(String captureUrl, long createdAtMillis) {
                // no-op
            }
        });
    }

    @Test
    public void builder__build() {
        Truth.assertThat(builder.build()).isNotNull();
    }
}
