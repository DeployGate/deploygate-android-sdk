package com.deploygate.sdk;

import android.os.Bundle;
import android.os.SystemClock;

import com.deploygate.service.DeployGateEvent;

class CustomLog {
    public final String uid;
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
        this.uid = UniqueId.generate();
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
     * @return a bundle to send to the client service
     */
    Bundle toExtras() {
        Bundle extras = new Bundle();
        extras.putSerializable(DeployGateEvent.EXTRA_UID, uid);
        extras.putSerializable(DeployGateEvent.EXTRA_LOG, body);
        extras.putSerializable(DeployGateEvent.EXTRA_LOG_TYPE, type);
        extras.putLong(DeployGateEvent.EXTRA_BUFFERED_AT_IN_MILLI_SECONDS, elapsedTime);
        return extras;
    }
}
