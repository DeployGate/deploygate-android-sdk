package com.deploygate.sdk;

import android.os.Bundle;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.DeployGateEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class SendLogcatRequest {
    public final String bundleId;
    public final String uid;
    public final ArrayList<String> lines;
    private int retryCount;

    SendLogcatRequest(
            String bundleId,
            List<String> lines
    ) {
        this.bundleId = bundleId;
        this.uid = UniqueId.generate();
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

            splits.add(new SendLogcatRequest(bundleId, lines.subList(offset, endIndex)));
        }

        return splits;
    }

    Bundle toExtras() {
        Bundle extras = new Bundle();

        extras.putString(DeployGateEvent.EXTRA_BUNDLE_ID, bundleId);
        extras.putString(DeployGateEvent.EXTRA_UID, uid);
        extras.putStringArrayList(DeployGateEvent.EXTRA_LOG, lines);

        return extras;
    }
}
