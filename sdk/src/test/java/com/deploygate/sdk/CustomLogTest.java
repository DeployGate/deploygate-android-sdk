package com.deploygate.sdk;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.sdk.truth.BundleSubject;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CustomLogTest {

    @Test
    public void CustomLog_toExtras_must_be_valid_format() {
        CustomLog log = new CustomLog("error", "yes");

        BundleSubject.assertThat(log.toExtras()).isEqualTo(createLogExtra("error", "yes"));
    }

    private static Bundle createLogExtra(
            String type,
            String body
    ) {
        Bundle bundle = new Bundle();
        bundle.putString("logType", type);
        bundle.putString("log", body);
        return bundle;
    }
}
