package com.deploygate.sdk;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.sdk.truth.BundleSubject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowSystemClock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CustomLogTest {

    @Test
    public void CustomLog_toExtras_must_be_valid_format() {
        CustomLog log = new CustomLog("error", "yes");

        ShadowSystemClock.advanceBy(Duration.ofMillis(123));

        BundleSubject.assertThat(log.toExtras()).isEqualTo(createLogExtra("error", "yes", 123, TimeUnit.MILLISECONDS));
    }

    private static Bundle createLogExtra(
            String type,
            String body,
            long timeInBuffer,
            TimeUnit timeInBufferUnit
    ) {
        Bundle bundle = new Bundle();
        bundle.putString("logType", type);
        bundle.putString("log", body);
        bundle.putLong("bufferedTimeInMs", timeInBufferUnit.toMillis(timeInBuffer));
        return bundle;
    }
}
