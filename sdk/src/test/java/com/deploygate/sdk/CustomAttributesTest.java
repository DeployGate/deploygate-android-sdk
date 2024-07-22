package com.deploygate.sdk;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CustomAttributesTest {

  @NonNull
  CustomAttributes attributes;

  @Before
  public void setUp() {
    attributes = new CustomAttributes();
  }

  @Test
  public void put__key_pattern() {
    Truth.assertThat(attributes.putString("valid", "value")).isTrue();
    Truth.assertThat(attributes.putString("valid_underscore", "value")).isTrue();
    Truth.assertThat(attributes.putString("valid_1_number", "value")).isTrue();
    Truth.assertThat(attributes.putString("min", "value")).isTrue();
    Truth.assertThat(attributes.putString("valid_key_with_length_under_32", "value")).isTrue();

    Truth.assertThat(attributes.putString("ng", "value")).isFalse();
    Truth.assertThat(attributes.putString("true", "value")).isFalse();
    Truth.assertThat(attributes.putString("false", "value")).isFalse();
    Truth.assertThat(attributes.putString("null", "value")).isFalse();
    Truth.assertThat(attributes.putString("invalid-hyphen", "value")).isFalse();
    Truth.assertThat(attributes.putString("invalid#sharp", "value")).isFalse();
    Truth.assertThat(attributes.putString("invalid$dollar", "value")).isFalse();
    Truth.assertThat(attributes.putString("invalid.dot", "value")).isFalse();
    Truth.assertThat(attributes.putString("invalid!bang", "value")).isFalse();
    Truth.assertThat(attributes.putString("invalid*glob", "value")).isFalse();
    Truth.assertThat(attributes.putString("invalidUpperCase", "value")).isFalse();
    Truth.assertThat(attributes.putString("12345", "value")).isFalse();
    Truth.assertThat(attributes.putString("1_invalid_begin_number", "value")).isFalse();
    Truth.assertThat(attributes.putString("invalid_key_with_length_over_32_characters", "value")).isFalse();
  }

  @Test
  public void put__value_pattern() {
    Truth.assertThat(attributes.putString("valid_string", "value")).isTrue();
    Truth.assertThat(attributes.putInt("valid_int", 1)).isTrue();
    Truth.assertThat(attributes.putLong("valid_long", 1L)).isTrue();
    Truth.assertThat(attributes.putFloat("valid_float", 1.1f)).isTrue();
    Truth.assertThat(attributes.putDouble("valid_double", 1.1)).isTrue();
    Truth.assertThat(attributes.putBoolean("valid_boolean", true)).isTrue();

    Truth.assertThat(attributes.putString("invalid_too_long_string", "this is too long string value. we cannot accept value if size over 64.")).isFalse();
  }

  @Test
  public void put__max_size() {
    Truth.assertThat(attributes.putString("key1", "value")).isTrue();
    Truth.assertThat(attributes.putString("key2", "value")).isTrue();
    Truth.assertThat(attributes.putString("key3", "value")).isTrue();
    Truth.assertThat(attributes.putString("key4", "value")).isTrue();
    Truth.assertThat(attributes.putString("key5", "value")).isTrue();
    Truth.assertThat(attributes.putString("key6", "value")).isTrue();
    Truth.assertThat(attributes.putString("key7", "value")).isTrue();
    Truth.assertThat(attributes.putString("key8", "value")).isTrue();

    // allow to overwrite
    Truth.assertThat(attributes.putString("key1", "value2")).isTrue();
    // not allow to put value with new key because of max size
    Truth.assertThat(attributes.putString("key9", "value")).isFalse();

    attributes.remove("key8");

    // allow to put value with new key after remove exists key
    Truth.assertThat(attributes.putString("key9", "value")).isTrue();
    // allow to overwrite
    Truth.assertThat(attributes.putString("key1", "value3")).isTrue();
    // not allow to put value with new key because of max size
    Truth.assertThat(attributes.putString("key10", "value")).isFalse();

    attributes.removeAll();

    // allow to put value less than max size
    Truth.assertThat(attributes.putString("key1", "value")).isTrue();
    Truth.assertThat(attributes.putString("key2", "value")).isTrue();
    Truth.assertThat(attributes.putString("key3", "value")).isTrue();
    Truth.assertThat(attributes.putString("key4", "value")).isTrue();
    Truth.assertThat(attributes.putString("key5", "value")).isTrue();
    Truth.assertThat(attributes.putString("key6", "value")).isTrue();
    Truth.assertThat(attributes.putString("key7", "value")).isTrue();
    Truth.assertThat(attributes.putString("key8", "value")).isTrue();
    Truth.assertThat(attributes.putString("key9", "value")).isFalse();
  }
}
