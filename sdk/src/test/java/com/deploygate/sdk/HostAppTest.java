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
    @NonNull
    private Context context;

    @Before
    public void setUp() {
        context = getApplicationContext();
    }

    @Test
    public void default_properties() {
        HostApp app = new HostApp(context);

        Truth.assertThat(app.debuggable).isTrue();
        Truth.assertThat(app.canUseLogcat).isTrue();
        Truth.assertThat(app.packageName).isEqualTo("com.deploygate.sdk.test");
        Truth.assertThat(app.sdkVersion).isEqualTo(4);
    }

    @Test
    @Config(sdk = 16)
    public void canUseLogcat_is_true_if_sdk_is_equal_to_jb() {
        HostApp app = new HostApp(context);

        Truth.assertThat(app.canUseLogcat).isTrue();
    }

    // sdk 15 or lower is not available for robolectric...
}
