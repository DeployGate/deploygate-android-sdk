package com.deploygate.sdk;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test class will make sure all *public* interfaces are defined as expected
 */
@RunWith(AndroidJUnit4.class)
public class CustomAttributesInterfaceTest {

  @NonNull
  CustomAttributes attributes;

  @Before
  public void setUp() {
    attributes = new CustomAttributes();
  }

  @Test
  public void putString() {
    Truth.assertThat(attributes.putString("key", "value")).isInstanceOf(Boolean.class);
  }

  @Test
  public void putInt() {
    Truth.assertThat(attributes.putInt("key", 1)).isInstanceOf(Boolean.class);
  }

  @Test
  public void putLong() {
    Truth.assertThat(attributes.putLong("key", 1L)).isInstanceOf(Boolean.class);
  }

  @Test
  public void putFloat() {
    Truth.assertThat(attributes.putFloat("key", 1.0f)).isInstanceOf(Boolean.class);
  }

  @Test
  public void putDouble() {
    Truth.assertThat(attributes.putDouble("key", 1.0)).isInstanceOf(Boolean.class);
  }

  @Test
  public void putBoolean() {
    Truth.assertThat(attributes.putBoolean("key", true)).isInstanceOf(Boolean.class);
  }

  @Test
  public void remove() {
    attributes.remove("key");
  }

  @Test
  public void removeAll() {
    attributes.removeAll();
  }

  @Test
  public void size() {
    Truth.assertThat(attributes.size()).isInstanceOf(Integer.class);
  }

  @Test
  public void isEmpty() {
    Truth.assertThat(attributes.isEmpty()).isInstanceOf(Boolean.class);
  }
}