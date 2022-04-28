package com.deploygate.sdk;

import android.os.Bundle;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.DeployGateEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class SendLogcatRequest {
    public static final SendLogcatRequest createTermination(String bundleSessionKey) {
        return new SendLogcatRequest(bundleSessionKey, new ArrayList<String>(), true);
    }

    public final String bundleSessionKey;
    public final String cid;
    public final ArrayList<String> lines;
    public final boolean isBundleTermination;
    private int retryCount;

    SendLogcatRequest(
            String bundleSessionKey,
            List<String> lines
    ) {
        this(bundleSessionKey, lines, false);
    }

    private SendLogcatRequest(
            String bundleSessionKey,
            List<String> lines,
            boolean isBundleTermination
    ) {
        this.bundleSessionKey = bundleSessionKey;
        this.cid = ClientId.generate();
        this.lines = lines instanceof ArrayList ? (ArrayList<String>) lines : new ArrayList<>(lines);
        this.isBundleTermination = isBundleTermination;
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

        if (count == 1 || isBundleTermination) {
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

            splits.add(new SendLogcatRequest(bundleSessionKey, lines.subList(offset, endIndex), false));
        }

        return splits;
    }

    Bundle toExtras() {
        Bundle extras = new Bundle();

        extras.putString(DeployGateEvent.EXTRA_BUNDLE_SESSION_KEY, bundleSessionKey);
        extras.putString(DeployGateEvent.EXTRA_CID, cid);
        extras.putStringArrayList(DeployGateEvent.EXTRA_LOG, lines);
        extras.putBoolean(DeployGateEvent.EXTRA_IS_BUNDLE_TERMINATION, isBundleTermination);

        return extras;
    }
}
