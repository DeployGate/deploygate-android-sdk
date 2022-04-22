package com.deploygate.sdk;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CompatibilityTest {

    @Test
    public void check_SUPPORT_UPDATE_MESSAGE_OF_BUILD() {
        Truth.assertThat(Compatibility.ClientVersion.SUPPORT_UPDATE_MESSAGE_OF_BUILD).isEqualTo(39);
    }

    @Test
    public void check_SUPPORT_SERIALIZED_EXCEPTION() {
        Truth.assertThat(Compatibility.ClientVersion.SUPPORT_SERIALIZED_EXCEPTION).isEqualTo(42);
    }

    @Test
    public void check_bitMask_uniqueness() {
        for (final Compatibility c : Compatibility.values()) {
            int tmp = c.bitMask;

            while (tmp != 1) {
                Truth.assertThat(tmp & 1).isEqualTo(0);
                tmp >>>= 1;
            }
        }
    }

    @Test
    public void check_bitMask_value() {
        Truth.assertThat(Compatibility.UPDATE_MESSAGE_OF_BUILD.bitMask).isEqualTo(1);
        Truth.assertThat(Compatibility.SERIALIZED_EXCEPTION.bitMask).isEqualTo(2);
        Truth.assertThat(Compatibility.LOGCAT_BUNDLE.bitMask).isEqualTo(4);
    }
}
