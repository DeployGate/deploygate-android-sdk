package com.deploygate.sdk.internal;

import android.util.Log;

import java.util.Locale;

public class Logger {
    public static final String TAG = "DeployGateSDK";

    public static void d(
            String format,
            Object... args
    ) {
        Log.d(TAG, String.format(Locale.US, format, args));
    }

    public static void i(
            String format,
            Object... args
    ) {
        Log.i(TAG, String.format(Locale.US, format, args));
    }

    public static void w(
            String format,
            Object... args
    ) {
        Log.w(TAG, String.format(Locale.US, format, args));
    }

    public static void w(
            Throwable th,
            String format,
            Object... args
    ) {
        Log.w(TAG, String.format(Locale.US, format, args), th);
    }

    public static void e(
            String format,
            Object... args
    ) {
        Log.e(TAG, String.format(Locale.US, format, args));
    }

    public static void e(
            Throwable th,
            String format,
            Object... args
    ) {
        Log.e(TAG, String.format(Locale.US, format, args), th);
    }
}
