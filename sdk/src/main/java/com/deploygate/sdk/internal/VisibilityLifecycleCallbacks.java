package com.deploygate.sdk.internal;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

public final class VisibilityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    public interface OnVisibilityChangeListener {
        void onForeground(long elapsedRealtime, TimeUnit timeUnit);
        void onBackground(long elapsedRealtime, TimeUnit timeUnit);
    }

    private int onResumeCount = 0; // this is manipulated from the single thread

    private final OnVisibilityChangeListener listener;

    public VisibilityLifecycleCallbacks(OnVisibilityChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onActivityCreated(
            Activity activity,
            Bundle savedInstanceState
    ) {
        // no-op
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // no-op
    }

    @Override
    public void onActivityResumed(Activity activity) {
        onResumeCount++;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            listener.onForeground(SystemClock.elapsedRealtimeNanos(), TimeUnit.NANOSECONDS);
        } else {
            listener.onForeground(SystemClock.elapsedRealtime(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
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
    public void onActivityStopped(Activity activity) {
        // no-op
    }

    @Override
    public void onActivitySaveInstanceState(
            Activity activity,
            Bundle outState
    ) {
        // no-op
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // no-op
    }
}
