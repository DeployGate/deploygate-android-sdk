package com.deploygate.sdk;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeployGateTest {

    @NonNull
    Application app;

    @NonNull
    DeployGateSdkConfiguration configuration;

    DeployGateCallback callback;
    DeployGateInitializeCallback initializeCallback;
    DeployGateStatusChangeCallback statusChangeCallback;
    DeployGateUpdateAvailableCallback updateAvailableCallback;
    DeployGateCaptureCreateCallback captureCreateCallback;

    @Before
    public void setUp() {
        app = getApplicationContext();
        configuration = new DeployGateSdkConfiguration.Builder().build();
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

        DeployGate.install(app, configuration);
    }

    @After
    public void tearDown() {
        DeployGate.clear();
    }

    @Test
    public void register_fine_grained_callbacks() {
        DeployGateInitializeCallback anotherInitializeCallback = new DeployGateInitializeCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {
                // no-op
            }
        };
        DeployGateStatusChangeCallback anotherStatusChangeCallback = new DeployGateStatusChangeCallback() {
            @Override
            public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {
                // no-op
            }
        };
        DeployGateUpdateAvailableCallback anotherUpdateAvailableCallback = new DeployGateUpdateAvailableCallback() {
            @Override
            public void onUpdateAvailable(int revision, String versionName, int versionCode) {
                // no-op
            }
        };
        DeployGateCaptureCreateCallback anotherCaptureCreateCallback = new DeployGateCaptureCreateCallback() {
            @Override
            public void onCaptureCreated(String captureUrl, long createdAtMillis) {
                // no-op
            }
        };

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(0);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(0);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(0);
        Truth.assertThat(DeployGate.getCaptureCreateCallbacks().size()).isEqualTo(0);

        DeployGate.registerInitializeCallback(initializeCallback);
        DeployGate.registerStatusChangeCallback(statusChangeCallback);
        DeployGate.registerUpdateAvailableCallback(updateAvailableCallback);
        DeployGate.registerCaptureCreateCallback(captureCreateCallback);

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getInitializeCallbacks().contains(initializeCallback)).isTrue();
        Truth.assertThat(DeployGate.getInitializeCallbacks().contains(anotherInitializeCallback)).isFalse();
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().contains(statusChangeCallback)).isTrue();
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().contains(anotherStatusChangeCallback)).isFalse();
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().contains(updateAvailableCallback)).isTrue();
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().contains(anotherUpdateAvailableCallback)).isFalse();
        Truth.assertThat(DeployGate.getCaptureCreateCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getCaptureCreateCallbacks().contains(captureCreateCallback)).isTrue();
        Truth.assertThat(DeployGate.getCaptureCreateCallbacks().contains(anotherCaptureCreateCallback)).isFalse();

        // Each callback already registered before, so DeployGate maybe ignore this operation.
        DeployGate.registerInitializeCallback(initializeCallback);
        DeployGate.registerStatusChangeCallback(statusChangeCallback);
        DeployGate.registerUpdateAvailableCallback(updateAvailableCallback);
        DeployGate.registerCaptureCreateCallback(captureCreateCallback);

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getCaptureCreateCallbacks().size()).isEqualTo(1);

        // register another callbacks
        DeployGate.registerInitializeCallback(anotherInitializeCallback);
        DeployGate.registerStatusChangeCallback(anotherStatusChangeCallback);
        DeployGate.registerUpdateAvailableCallback(anotherUpdateAvailableCallback);
        DeployGate.registerCaptureCreateCallback(anotherCaptureCreateCallback);

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(2);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(2);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(2);
        Truth.assertThat(DeployGate.getCaptureCreateCallbacks().size()).isEqualTo(2);

        DeployGate.unregisterInitializeCallback(initializeCallback);
        DeployGate.unregisterStatusChangeCallback(statusChangeCallback);
        DeployGate.unregisterUpdateAvailableCallback(updateAvailableCallback);
        DeployGate.unregisterCaptureCreateCallback(captureCreateCallback);

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getCaptureCreateCallbacks().size()).isEqualTo(1);

        // Each callback already unregistered before, so DeployGate maybe ignore this operation.
        DeployGate.unregisterInitializeCallback(initializeCallback);
        DeployGate.unregisterStatusChangeCallback(statusChangeCallback);
        DeployGate.unregisterUpdateAvailableCallback(updateAvailableCallback);
        DeployGate.unregisterCaptureCreateCallback(captureCreateCallback);

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getCaptureCreateCallbacks().size()).isEqualTo(1);
    }

    @Test
    public void register_callback() {
        DeployGateCallback anotherCallback = new DeployGateCallback() {
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

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(0);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(0);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(0);

        DeployGate.registerCallback(callback, false);

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getInitializeCallbacks().contains(callback)).isTrue();
        Truth.assertThat(DeployGate.getInitializeCallbacks().contains(anotherCallback)).isFalse();
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().contains(callback)).isTrue();
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().contains(anotherCallback)).isFalse();
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().contains(callback)).isTrue();
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().contains(anotherCallback)).isFalse();

        // callback already registered before, so DeployGate maybe ignore this operation.
        DeployGate.registerCallback(callback, false);

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(1);

        // register another callback
        DeployGate.registerCallback(anotherCallback, false);

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(2);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(2);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(2);

        DeployGate.unregisterCallback(callback);

        Truth.assertThat(DeployGate.getInitializeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getStatusChangeCallbacks().size()).isEqualTo(1);
        Truth.assertThat(DeployGate.getUpdateAvailableCallbacks().size()).isEqualTo(1);
    }
}
