package com.deploygate.sdk;

import android.os.Bundle;
import android.os.SystemClock;

import com.deploygate.service.DeployGateEvent;

class CustomLog extends Instruction {
    public final String type;
    public final String body;
    private final long elapsedTime;
    private int retryCount;

    CustomLog(
            String type,
            String body
    ) {
        super(null);
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

    @Override
    void applyValues(Bundle extras) {
        extras.putString(DeployGateEvent.EXTRA_LOG, body);
        extras.putString(DeployGateEvent.EXTRA_LOG_TYPE, type);
        extras.putLong(DeployGateEvent.EXTRA_BUFFERED_AT_IN_MILLI_SECONDS, elapsedTime);
    }
}
