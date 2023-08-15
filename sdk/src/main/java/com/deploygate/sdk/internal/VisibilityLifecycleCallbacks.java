package com.deploygate.sdk.internal;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

public final class VisibilityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    public interface OnVisibilityChangeListener {
        void onForeground(long elapsedRealtime, @NonNull TimeUnit timeUnit);
        void onBackground(long elapsedRealtime, @NonNull TimeUnit timeUnit);
    }

    private int onResumeCount = 0; // this is manipulated from the single thread

    @NonNull
    private final OnVisibilityChangeListener listener;

    public VisibilityLifecycleCallbacks(@NonNull OnVisibilityChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onActivityCreated(
            @NonNull Activity activity,
            @Nullable Bundle savedInstanceState
    ) {
        // no-op
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // no-op
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        onResumeCount++;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            listener.onForeground(SystemClock.elapsedRealtimeNanos(), TimeUnit.NANOSECONDS);
        } else {
            listener.onForeground(SystemClock.elapsedRealtime(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        onResumeCount = Math.max(onResumeCount - 1, 0); // cuz uint is unavailable.

        if (onResumeCount == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                listener.onBackground(SystemClock.elapsedRealtimeNanos(), TimeUnit.NANOSECONDS);
            } else {
                listener.onBackground(SystemClock.elapsedRealtime(), TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        // no-op
    }

    @Override
    public void onActivitySaveInstanceState(
            @NonNull Activity activity,
            @NonNull Bundle outState
    ) {
        // no-op
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // no-op
    }
}
