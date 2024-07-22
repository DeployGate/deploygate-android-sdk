package com.deploygate.sdk;

import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class CustomAttributes {

  private static final String TAG = "CustomAttributes";

  private static final int MAX_ATTRIBUTES_SIZE = 8;
  private static final Pattern VALID_KEY_PATTERN = Pattern.compile("^[a-z][_a-z0-9]{2,31}$");
  private static final int MIN_KEY_LENGTH = 3;
  private static final int MAX_KEY_LENGTH = 32;
  private static final int MAX_VALUE_LENGTH = 64;

  private final ConcurrentHashMap<String, Object> attributes;

  public CustomAttributes() {
    attributes = new ConcurrentHashMap<>();
  }

  public boolean putString(String key, String value) {
    return putInternal(key, value);
  }

  public boolean putInt(String key, int value) {
    return putInternal(key, value);
  }

  public boolean putLong(String key, long value) {
    return putInternal(key, value);
  }

  public boolean putFloat(String key, float value) {
    return putInternal(key, value);
  }

  public boolean putDouble(String key, double value) {
    return putInternal(key, value);
  }

  public boolean putBoolean(String key, boolean value) {
    return putInternal(key, value);
  }

  public void remove(String key) {
    attributes.remove(key);
  }

  public void removeAll() {
    attributes.clear();
  }

  public int size() {
    return attributes.size();
  }

  public boolean isEmpty() {
    return attributes.isEmpty();
  }

  public String toJsonString() {
    return new JSONObject(attributes).toString();
  }

  private boolean putInternal(String key, Object value) {
    if (!isValidKey(key)) {
      return false;
    }

    if (!isValidValue(value)) {
      return false;
    }

    attributes.put(key, value);
    return true;
  }

  private boolean isValidKey(String key) {
    if (size() >= MAX_ATTRIBUTES_SIZE && !attributes.containsKey(key)) {
      Log.w(TAG, "Attributes already reached max size. Ignored: " + key);
      return false;
    }

    if (key == null || key.equals("true") || key.equals("false") || key.equals("null")) {
      Log.w(TAG, "Not allowed key: " + key);
      return false;
    }

    if (key.length() < MIN_KEY_LENGTH || key.length() > MAX_KEY_LENGTH || !VALID_KEY_PATTERN.matcher(key).matches()) {
      Log.w(TAG, "Invalid key: " + key);
      return false;
    }

    return true;
  }

  private boolean isValidValue(Object value) {
    if (value == null) {
      Log.w(TAG, "Value is null");
      return false;
    }

    if (value instanceof String && ((String) value).length() > MAX_VALUE_LENGTH) {
        Log.w(TAG, "Value too long: " + value);
        return false;
    } else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
      return true;
    } else {
      // dead code
      Log.w(TAG, "Invalid value: " + value);
      return false;
    }
  }
}
