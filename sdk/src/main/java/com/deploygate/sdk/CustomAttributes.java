package com.deploygate.sdk;

import com.deploygate.sdk.internal.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public final class CustomAttributes {

  private static final String TAG = "CustomAttributes";

  private static final int MAX_ATTRIBUTES_SIZE = 8;
  private static final Pattern VALID_KEY_PATTERN = Pattern.compile("^[a-z][_a-z0-9]{2,31}$");
  private static final int MAX_VALUE_LENGTH = 64;

  private final Object mLock;
  private JSONObject attributes;

  CustomAttributes() {
    mLock = new Object();
    attributes = new JSONObject();
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
    synchronized (mLock) {
      attributes.remove(key);
    }
  }

  public void removeAll() {
    synchronized (mLock) {
      // recreate new object instead of removing all keys
      attributes = new JSONObject();
    }
  }

  int size() {
    synchronized (mLock) {
      return attributes.length();
    }
  }

  String getJSONString() {
    synchronized (mLock) {
      return attributes.toString();
    }
  }

  private boolean putInternal(String key, Object value) {
    if (!isValidKey(key)) {
      return false;
    }

    if (!isValidValue(value)) {
      return false;
    }

    synchronized (mLock) {
      try {
        attributes.put(key, value);

        if (attributes.length() > MAX_ATTRIBUTES_SIZE) {
          // rollback put operation
          attributes.remove(key);
          Logger.w(TAG, "Attributes already reached max size. Ignored: " + key);
          return false;
        }
      } catch (JSONException e) {
        Logger.w(TAG, "Failed to put attribute: " + key, e);
        return false;
      }
    }

    return true;
  }

  private boolean isValidKey(String key) {
    if (key == null || key.equals("true") || key.equals("false") || key.equals("null")) {
      Logger.w(TAG, "Not allowed key: " + key);
      return false;
    }

    if (!VALID_KEY_PATTERN.matcher(key).matches()) {
      Logger.w(TAG, "Invalid key: " + key);
      return false;
    }

    return true;
  }

  private boolean isValidValue(Object value) {
    if (value == null) {
      Logger.w(TAG, "Value is null");
      return false;
    }

    if (value instanceof String && ((String) value).length() > MAX_VALUE_LENGTH) {
      Logger.w(TAG, "Value too long: " + value);
      return false;
    } else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
      return true;
    } else {
      // dead code
      Logger.w(TAG, "Invalid value: " + value);
      return false;
    }
  }
}
