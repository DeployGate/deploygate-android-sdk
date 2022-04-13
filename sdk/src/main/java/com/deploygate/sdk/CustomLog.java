package com.deploygate.sdk;

import android.os.Bundle;
import android.os.SystemClock;

import com.deploygate.service.DeployGateEvent;

class CustomLog {
    public final String type;
    public final String body;
    private final long elapsedTime;
    private int retryCount;

    CustomLog(
            String type,
            String body
    ) {
        this.type = type;
        this.body = body;
        this.elapsedTime = SystemClock.elapsedRealtime();
        this.retryCount = 0;
    }

    /**
     * @return the number of current attempts
     */
    int getAndIncrementRetryCount() {
        return retryCount++;
    }

    /**
     * This measures the waiting-time in the buffer, so don't hold or reuse the returned value.
     *
     * @return a bundle to send to the client service
     */
    Bundle toExtras() {
        Bundle extras = new Bundle();
        extras.putSerializable(DeployGateEvent.EXTRA_LOG, body);
        extras.putSerializable(DeployGateEvent.EXTRA_LOG_TYPE, type);
        extras.putLong(DeployGateEvent.EXTRA_BUFFERED_TIME_IN_MILLI_SECONDS, SystemClock.elapsedRealtime() - elapsedTime);
        return extras;
    }
}
