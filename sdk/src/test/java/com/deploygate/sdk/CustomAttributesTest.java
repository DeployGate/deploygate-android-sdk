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
  public void put__accept_when_valid_key() {
    Truth.assertThat(attributes.putString("valid", "valid_value1")).isTrue();
    Truth.assertThat(attributes.putString("valid_underscore", "valid_value2")).isTrue();
    Truth.assertThat(attributes.putString("valid_1_number", "valid_value3")).isTrue();
    Truth.assertThat(attributes.putString("min", "valid_value4")).isTrue();
    Truth.assertThat(attributes.putString("valid_key_with_length_under_32", "valid_value5")).isTrue();

    Truth.assertThat(attributes.putString("ng", "invalid_value1")).isFalse();
    Truth.assertThat(attributes.putString("true", "invalid_value2")).isFalse();
    Truth.assertThat(attributes.putString("false", "invalid_value3")).isFalse();
    Truth.assertThat(attributes.putString("null", "invalid_value4")).isFalse();
    Truth.assertThat(attributes.putString("invalid-hyphen", "invalid_value5")).isFalse();
    Truth.assertThat(attributes.putString("invalid#sharp", "invalid_value6")).isFalse();
    Truth.assertThat(attributes.putString("invalid$dollar", "invalid_value7")).isFalse();
    Truth.assertThat(attributes.putString("invalid.dot", "invalid_value8")).isFalse();
    Truth.assertThat(attributes.putString("invalid!bang", "invalid_value9")).isFalse();
    Truth.assertThat(attributes.putString("invalid*glob", "invalid_value10")).isFalse();
    Truth.assertThat(attributes.putString("invalidUpperCase", "invalid_value11")).isFalse();
    Truth.assertThat(attributes.putString("12345", "invalid_value12")).isFalse();
    Truth.assertThat(attributes.putString("1_invalid_begin_number", "invalid_value13")).isFalse();
    Truth.assertThat(attributes.putString("invalid_key_with_length_over_32_characters", "invalid_value14")).isFalse();
  }

  @Test
  public void put__accept_when_valid_value() {
    Truth.assertThat(attributes.putString("valid_string", "value")).isTrue();
    Truth.assertThat(attributes.putInt("valid_int", 1)).isTrue();
    Truth.assertThat(attributes.putLong("valid_long", 1L)).isTrue();
    Truth.assertThat(attributes.putFloat("valid_float", 1.1f)).isTrue();
    Truth.assertThat(attributes.putDouble("valid_double", 1.1)).isTrue();
    Truth.assertThat(attributes.putBoolean("valid_boolean", true)).isTrue();

    Truth.assertThat(attributes.putString("invalid_too_long_string", "this is too long string value. we cannot accept value if size over 64.")).isFalse();
  }

  @Test
  public void not_exceed_max_size() {
    Truth.assertThat(attributes.putString("key1", "value1")).isTrue();
    Truth.assertThat(attributes.putString("key2", "value2")).isTrue();
    Truth.assertThat(attributes.putString("key3", "value3")).isTrue();
    Truth.assertThat(attributes.putString("key4", "value4")).isTrue();
    Truth.assertThat(attributes.putString("key5", "value5")).isTrue();
    Truth.assertThat(attributes.putString("key6", "value6")).isTrue();
    Truth.assertThat(attributes.putString("key7", "value7")).isTrue();
    Truth.assertThat(attributes.putString("key8", "value8")).isTrue();

    // allow to overwrite
    Truth.assertThat(attributes.putString("key1", "overwrite1_1")).isTrue();
    // not allow to put value with new key because of max size
    Truth.assertThat(attributes.putString("key9", "value9")).isFalse();

    attributes.remove("key8");

    // allow to put value with new key after remove exists key
    Truth.assertThat(attributes.putString("key9", "value9")).isTrue();
    // allow to overwrite
    Truth.assertThat(attributes.putString("key1", "overwrite1_2")).isTrue();
    // not allow to put value with new key because of max size
    Truth.assertThat(attributes.putString("key10", "value10")).isFalse();

    attributes.removeAll();

    // allow to put value less than max size
    Truth.assertThat(attributes.putString("key1", "another_value1")).isTrue();
    Truth.assertThat(attributes.putString("key2", "another_value2")).isTrue();
    Truth.assertThat(attributes.putString("key3", "another_value3")).isTrue();
    Truth.assertThat(attributes.putString("key4", "another_value4")).isTrue();
    Truth.assertThat(attributes.putString("key5", "another_value5")).isTrue();
    Truth.assertThat(attributes.putString("key6", "another_value6")).isTrue();
    Truth.assertThat(attributes.putString("key7", "another_value7")).isTrue();
    Truth.assertThat(attributes.putString("key8", "another_value8")).isTrue();
    Truth.assertThat(attributes.putString("key9", "another_value9")).isFalse();
  }

  @Test
  public void size() {
    Truth.assertThat(attributes.size()).isEqualTo(0);

    attributes.putString("key1", "value1");
    Truth.assertThat(attributes.size()).isEqualTo(1);

    attributes.putString("key1", "overwrite1");
    Truth.assertThat(attributes.size()).isEqualTo(1);

    attributes.putString("key2", "value2");
    attributes.putString("key3", "value3");
    attributes.putString("key4", "value4");
    attributes.putString("key5", "value5");
    attributes.putString("key6", "value6");
    attributes.putString("key7", "value7");
    attributes.putString("key8", "value8");
    Truth.assertThat(attributes.size()).isEqualTo(8);

    attributes.putString("key9", "value9");
    Truth.assertThat(attributes.size()).isEqualTo(8);

    attributes.remove("key1");
    Truth.assertThat(attributes.size()).isEqualTo(7);

    attributes.removeAll();
    Truth.assertThat(attributes.size()).isEqualTo(0);
  }

  @Test
  public void isEmpty() {
    Truth.assertThat(attributes.isEmpty()).isTrue();

    attributes.putString("key1", "value1");
    Truth.assertThat(attributes.isEmpty()).isFalse();

    attributes.putString("key1", "overwrite1");
    Truth.assertThat(attributes.isEmpty()).isFalse();

    attributes.putString("key2", "value2");
    attributes.putString("key3", "value3");
    Truth.assertThat(attributes.isEmpty()).isFalse();

    attributes.remove("key1");
    Truth.assertThat(attributes.isEmpty()).isFalse();

    attributes.removeAll();
    Truth.assertThat(attributes.isEmpty()).isTrue();

    attributes.putString("key4", "value4");
    Truth.assertThat(attributes.isEmpty()).isFalse();
  }
}
