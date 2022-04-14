package com.deploygate.sdk;

import android.os.Bundle;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.sdk.truth.BundleSubject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.shadows.ShadowSystemClock;

import java.time.Duration;

@RunWith(AndroidJUnit4.class)
public class CustomLogTest {

    @Test
    public void CustomLog_toExtras_must_be_valid_format() {
        try (MockedStatic<UniqueId> uidMock = Mockito.mockStatic(UniqueId.class)) {
            uidMock.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    UniqueId.generate();
                }
            }).thenReturn("unique_id");

            long bufferedAt = SystemClock.elapsedRealtime();

            CustomLog log = new CustomLog("error", "yes");

            ShadowSystemClock.advanceBy(Duration.ofMillis(123));

            BundleSubject.assertThat(log.toExtras()).isEqualTo(createLogExtra("error", "yes", bufferedAt));
        }
    }

    private static Bundle createLogExtra(
            String type,
            String body,
            long bufferedAt
    ) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", "unique_id");
        bundle.putString("logType", type);
        bundle.putString("log", body);
        bundle.putLong("bufferedAt", bufferedAt);
        return bundle;
    }
}
