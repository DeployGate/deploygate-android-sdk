package com.deploygate.sdk;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class ClientIdTest {

    @Test
    public void check_generate_presence() {
        Truth.assertThat(ClientId.generate()).isNotEmpty();
    }

    @Test
    public void check_is_valid() {
        String validId = ClientId.generate();

        Truth.assertThat(ClientId.isValid(validId)).isTrue();

        Truth.assertThat(ClientId.isValid(null)).isFalse();
        Truth.assertThat(ClientId.isValid("")).isFalse();
        Truth.assertThat(ClientId.isValid("s" + validId)).isFalse();
        Truth.assertThat(ClientId.isValid(validId + "x")).isFalse();
        Truth.assertThat(ClientId.isValid(UUID.randomUUID().toString())).isFalse();
    }
}
