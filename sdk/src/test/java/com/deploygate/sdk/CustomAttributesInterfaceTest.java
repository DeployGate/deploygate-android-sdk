package com.deploygate.sdk;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
    attributes.putString("key", "value");
  }

  @Test
  public void putInt() {
    attributes.putInt("key", 1);
  }

  @Test
  public void putLong() {
    attributes.putLong("key", 1L);
  }

  @Test
  public void putFloat() {
    attributes.putFloat("key", 1.0f);
  }

  @Test
  public void putDouble() {
    attributes.putDouble("key", 1.0);
  }

  public void putBoolean() {
    attributes.putBoolean("key", true);
  }

  public void remove() {
    attributes.remove("key");
  }

  public void removeAll() {
    attributes.removeAll();
  }

  public int size() {
    return attributes.size();
  }

  public boolean isEmpty() {
    return attributes.isEmpty();
  }
}