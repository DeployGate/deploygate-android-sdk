package com.deploygate.sdk;

import android.os.Bundle;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.DeployGateEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class SendLogcatRequest {
    public final String watchId;
    public final ArrayList<String> lines;
    private int retryCount;

    SendLogcatRequest(
            String watchId,
            List<String> lines
    ) {
        this.watchId = watchId;
        this.lines = lines instanceof ArrayList ? (ArrayList<String>) lines : new ArrayList<>(lines);
    }

    /**
     * @return the number of current attempts
     */
    int getAndIncrementRetryCount() {
        return retryCount++;
    }

    List<SendLogcatRequest> splitInto(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException(String.format(Locale.US, "split count must be greater than 1 but %d", count));
        }

        if (count == 1) {
            return Collections.singletonList(this);
        }

        int size = lines.size();

        if (count >= size) {
            Logger.w("split count is too large");
            count = size;
        }

        List<SendLogcatRequest> splits = new ArrayList<>();

        for (int i = 0, offset = 0, step = size / count; i < count; i++, offset += step) {
            final int endIndex = (i == count - 1) ? size : offset + step;

            splits.add(new SendLogcatRequest(watchId, lines.subList(offset, endIndex)));
        }

        return splits;
    }

    Bundle toExtras() {
        Bundle extras = new Bundle();

        extras.putStringArrayList(DeployGateEvent.EXTRA_LOG, lines);
        extras.putString(DeployGateEvent.EXTRA_LOG_CLIENT_ID, watchId);

        return extras;
    }
}