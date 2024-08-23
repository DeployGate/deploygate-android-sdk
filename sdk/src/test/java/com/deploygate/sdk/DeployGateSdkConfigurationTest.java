package com.deploygate.sdk;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeployGateSdkConfigurationTest {

    CustomLogConfiguration customLogConfiguration;
    DeployGateInitializeCallback initializeCallback;
    DeployGateStatusChangeCallback statusChangeCallback;
    DeployGateUpdateAvailableCallback updateAvailableCallback;
    DeployGateCaptureCreateCallback captureCreateCallback;
    DeployGateCallback deployGateCallback;

    @Before
    public void setUp() {
        customLogConfiguration = new CustomLogConfiguration.Builder().build();
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
        deployGateCallback = new DeployGateCallback() {
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
    }


    @Test
    public void builder() {
        DeployGateSdkConfiguration configuration = new DeployGateSdkConfiguration.Builder()
                .setAppOwnerName("owner")
                .setCaptureEnabled(true)
                .setCrashReportingEnabled(true)
                .setCustomLogConfiguration(customLogConfiguration)
                .setDisabled(false)
                .setEnabledOnNonDebuggableBuild(true)
                .setInitializeCallback(initializeCallback)
                .setStatusChangeCallback(statusChangeCallback)
                .setUpdateAvailableCallback(updateAvailableCallback)
                .setCaptureCreateCallback(captureCreateCallback)
                .build();

        Truth.assertThat(configuration.appOwnerName).isEqualTo("owner");
        Truth.assertThat(configuration.isCaptureEnabled).isTrue();
        Truth.assertThat(configuration.isCrashReportingEnabled).isTrue();
        Truth.assertThat(configuration.customLogConfiguration).isEqualTo(customLogConfiguration);
        Truth.assertThat(configuration.isDisabled).isFalse();
        Truth.assertThat(configuration.isEnabledOnNonDebuggableBuild).isTrue();
        Truth.assertThat(configuration.initializeCallback).isEqualTo(initializeCallback);
        Truth.assertThat(configuration.statusChangeCallback).isEqualTo(statusChangeCallback);
        Truth.assertThat(configuration.updateAvailableCallback).isEqualTo(updateAvailableCallback);
        Truth.assertThat(configuration.captureCreateCallback).isEqualTo(captureCreateCallback);
    }

    @Test
    public void builder_setCallback() {
        DeployGateSdkConfiguration configurationWithCallback = new DeployGateSdkConfiguration.Builder()
                .setCallback(deployGateCallback)
                .build();

        Truth.assertThat(configurationWithCallback.initializeCallback).isEqualTo(deployGateCallback);
        Truth.assertThat(configurationWithCallback.statusChangeCallback).isEqualTo(deployGateCallback);
        Truth.assertThat(configurationWithCallback.updateAvailableCallback).isEqualTo(deployGateCallback);
    }

    @Test
    public void builder_setXXXCallback() {
        DeployGateSdkConfiguration configurationWithFineGrainedCallbacks = new DeployGateSdkConfiguration.Builder()
                .setInitializeCallback(initializeCallback)
                .setStatusChangeCallback(statusChangeCallback)
                .setUpdateAvailableCallback(updateAvailableCallback)
                .setCaptureCreateCallback(captureCreateCallback)
                .build();

        Truth.assertThat(configurationWithFineGrainedCallbacks.initializeCallback).isEqualTo(initializeCallback);
        Truth.assertThat(configurationWithFineGrainedCallbacks.statusChangeCallback).isEqualTo(statusChangeCallback);
        Truth.assertThat(configurationWithFineGrainedCallbacks.updateAvailableCallback).isEqualTo(updateAvailableCallback);
        Truth.assertThat(configurationWithFineGrainedCallbacks.captureCreateCallback).isEqualTo(captureCreateCallback);
    }

    @Test
    public void builder_setCallbackAndOverride() {
        DeployGateSdkConfiguration configurationOverrideByFineGrainedCallbacks = new DeployGateSdkConfiguration.Builder()
                .setCallback(deployGateCallback)
                .setInitializeCallback(initializeCallback)
                .setStatusChangeCallback(statusChangeCallback)
                .setUpdateAvailableCallback(updateAvailableCallback)
                .build();

        // the last value set will be used
        Truth.assertThat(configurationOverrideByFineGrainedCallbacks.initializeCallback).isEqualTo(initializeCallback);
        Truth.assertThat(configurationOverrideByFineGrainedCallbacks.statusChangeCallback).isEqualTo(statusChangeCallback);
        Truth.assertThat(configurationOverrideByFineGrainedCallbacks.updateAvailableCallback).isEqualTo(updateAvailableCallback);

        DeployGateSdkConfiguration configurationOverrideByCallback = new DeployGateSdkConfiguration.Builder()
                .setInitializeCallback(initializeCallback)
                .setStatusChangeCallback(statusChangeCallback)
                .setUpdateAvailableCallback(updateAvailableCallback)
                .setCallback(deployGateCallback)
                .build();

        Truth.assertThat(configurationOverrideByCallback.initializeCallback).isEqualTo(deployGateCallback);
        Truth.assertThat(configurationOverrideByCallback.statusChangeCallback).isEqualTo(deployGateCallback);
        Truth.assertThat(configurationOverrideByCallback.updateAvailableCallback).isEqualTo(deployGateCallback);

        DeployGateSdkConfiguration configurationWithCallbackOverrideByNull = new DeployGateSdkConfiguration.Builder()
                .setCallback(deployGateCallback)
                .setInitializeCallback(null)
                .setStatusChangeCallback(null)
                .setUpdateAvailableCallback(null)
                .build();

        Truth.assertThat(configurationWithCallbackOverrideByNull.initializeCallback).isNull();
        Truth.assertThat(configurationWithCallbackOverrideByNull.statusChangeCallback).isNull();
        Truth.assertThat(configurationWithCallbackOverrideByNull.updateAvailableCallback).isNull();

        DeployGateSdkConfiguration configurationWithFineGrainedCallbacksOverrideByNull = new DeployGateSdkConfiguration.Builder()
                .setInitializeCallback(initializeCallback)
                .setStatusChangeCallback(statusChangeCallback)
                .setUpdateAvailableCallback(updateAvailableCallback)
                .setCallback(null)
                .build();

        Truth.assertThat(configurationWithFineGrainedCallbacksOverrideByNull.initializeCallback).isNull();
        Truth.assertThat(configurationWithFineGrainedCallbacksOverrideByNull.statusChangeCallback).isNull();
        Truth.assertThat(configurationWithFineGrainedCallbacksOverrideByNull.updateAvailableCallback).isNull();
    }
}
