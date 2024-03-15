package com.deploygate.sdk;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class HostAppTest {
    private static final int FULL_BIT = (1 << 5) - 1;

    @NonNull
    private Context context;

    @Before
    public void setUp() {
        context = getApplicationContext();
    }

    @Test
    public void default_properties() {
        HostApp app = new HostApp(
                context,
                new DeployGateSdkConfiguration.Builder().build()
        );

        Truth.assertThat(app.canUseLogcat).isTrue();
        Truth.assertThat(app.packageName).isEqualTo("com.deploygate.sdk.test");
        Truth.assertThat(app.sdkVersion).isEqualTo(4);
        Truth.assertThat(app.sdkArtifactVersion).isEqualTo("4.7.0");
        Truth.assertThat(app.activeFeatureFlags).isEqualTo(FULL_BIT);
        Truth.assertThat(app.canUseDeviceCapture()).isTrue();
    }

    @Test
    @Config(sdk = 16)
    public void canUseLogcat_is_true_if_sdk_is_equal_to_jb() {
        HostApp app = new HostApp(
                context,
                new DeployGateSdkConfiguration.Builder().build()
        );

        Truth.assertThat(app.canUseLogcat).isTrue();
    }

    // sdk 15 or lower is not available for robolectric...

    @Test
    public void disabled_DeployGateSdkConfiguration_initialize_host_app_as_disabled() {
        HostApp app = new HostApp(
                context,
                new DeployGateSdkConfiguration.Builder().setDisabled(true).build()
        );

        Truth.assertThat(app.canUseLogcat).isFalse();
        Truth.assertThat(app.packageName).isEqualTo("com.deploygate.sdk.test");
        Truth.assertThat(app.sdkVersion).isEqualTo(0);
        Truth.assertThat(app.sdkArtifactVersion).isNull();
        Truth.assertThat(app.activeFeatureFlags).isEqualTo(0);
        Truth.assertThat(app.canUseDeviceCapture()).isFalse();
    }

    @Test
    public void can_read_DeployGateSdkConfiguration_setCaptureEnabled() {
        HostApp app1 = new HostApp(
                context,
                new DeployGateSdkConfiguration.Builder().setCaptureEnabled(true).build()
        );

        Truth.assertThat(app1.canUseDeviceCapture()).isTrue();
        Truth.assertThat(app1.activeFeatureFlags).isEqualTo(FULL_BIT);

        HostApp app2 = new HostApp(
                context,
                new DeployGateSdkConfiguration.Builder().setCaptureEnabled(false).build()
        );

        Truth.assertThat(app2.canUseDeviceCapture()).isFalse();
        Truth.assertThat(app2.activeFeatureFlags).isEqualTo(FULL_BIT - BuildConfig.DEVICE_CAPTURE);
    }
}
