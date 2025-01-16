package com.deploygate.sdk;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    String tooLongString = "this is too long string value. we cannot accept value if size over 64.";
    Truth.assertThat(attributes.putString("invalid_too_long_string", tooLongString)).isFalse();
  }

  @Test
  public void size() {
    Truth.assertThat(attributes.size()).isEqualTo(0);

    attributes.putString("key1", "value1");
    Truth.assertThat(attributes.size()).isEqualTo(1);

    attributes.putString("key1", "overwrite1");
    Truth.assertThat(attributes.size()).isEqualTo(1);

    for (int i = 2; i <= 64; i++) {
      attributes.putString("key" + i, "value" + i);
    }
    Truth.assertThat(attributes.size()).isEqualTo(64);

    attributes.putString("key65", "value65");
    Truth.assertThat(attributes.size()).isEqualTo(64);

    attributes.remove("key1");
    Truth.assertThat(attributes.size()).isEqualTo(63);

    attributes.removeAll();
    Truth.assertThat(attributes.size()).isEqualTo(0);
  }

  @Test
  public void toJSONString() {
    Truth.assertThat(attributes.getJSONString()).isEqualTo("{}");

    attributes.putString("string", "value");
    attributes.putInt("int", Integer.MAX_VALUE);
    attributes.putLong("long", Long.MAX_VALUE);
    attributes.putFloat("float", Float.MAX_VALUE);
    attributes.putDouble("double", Double.MAX_VALUE);
    attributes.putBoolean("boolean", true);

    String expectedJSON = "{" +
        "\"string\":\"value\"," +
        "\"int\":2147483647," +
        "\"long\":9223372036854775807," +
        "\"float\":3.4028235E38," +
        "\"double\":1.7976931348623157E308," +
        "\"boolean\":true" +
        "}";
    Truth.assertThat(attributes.getJSONString()).isEqualTo(expectedJSON);

    attributes.removeAll();
    attributes.putString("string2", "value2");
    attributes.putInt("int2", Integer.MIN_VALUE);
    attributes.putLong("long2", Long.MIN_VALUE);
    attributes.putFloat("float2", Float.MIN_VALUE);
    attributes.putDouble("double2", Double.MIN_VALUE);
    attributes.putBoolean("boolean2", false);

    String expectedJSON2 = "{" +
        "\"string2\":\"value2\"," +
        "\"int2\":-2147483648," +
        "\"long2\":-9223372036854775808," +
        "\"float2\":1.4E-45," +
        "\"double2\":4.9E-324," +
        "\"boolean2\":false" +
        "}";
    Truth.assertThat(attributes.getJSONString()).isEqualTo(expectedJSON2);
  }

  @Test
  public void not_exceed_max_size() {
    for (int i = 1; i <= 64; i++) {
      Truth.assertThat(attributes.putString("key" + i, "value" + i)).isTrue();
    }

    // allow to overwrite
    Truth.assertThat(attributes.putString("key1", "overwrite1_1")).isTrue();
    // not allow to put value with new key because of max size
    Truth.assertThat(attributes.putString("key65", "value65")).isFalse();

    attributes.remove("key64");

    // allow to put value with new key after remove exists key
    Truth.assertThat(attributes.putString("key65", "value65")).isTrue();
    // allow to overwrite
    Truth.assertThat(attributes.putString("key1", "overwrite1_2")).isTrue();
    // not allow to put value with new key because of max size
    Truth.assertThat(attributes.putString("key66", "value66")).isFalse();

    attributes.removeAll();

    // allow to put value less than max size
    for (int i = 1; i <= 64; i++) {
      Truth.assertThat(attributes.putString("key" + i, "value" + i)).isTrue();
    }
    Truth.assertThat(attributes.putString("key65", "value65")).isFalse();
  }

  @Test()
  public void not_exceed_max_size_multi_thread() {
    // prepare attributes with max size
    for (int i = 1; i <= 64; i++) {
      attributes.putString("key" + i, "value" + i);
    }

    // try to put value with multi thread
    ExecutorService executors = Executors.newCachedThreadPool();
    for (int i = 1; i <= 100; i++) {
      final int index = i;
      executors.submit(new Runnable() {
        @Override
        public void run() {
          attributes.putString("key" + index, "value" + index);
        }
      });
    }

    Truth.assertThat(attributes.size()).isEqualTo(64);
    StringBuilder expectedJSONBuilder = new StringBuilder("{");
    for (int i = 1; i < 64; i++) {
      expectedJSONBuilder.append(String.format("\"key%d\":\"value%d\",", i, i));
    }
    expectedJSONBuilder.append("\"key64\":\"value64\"}");

    Truth.assertThat(attributes.getJSONString()).isEqualTo(expectedJSONBuilder.toString());
  }
}
