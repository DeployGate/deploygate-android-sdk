package com.deploygate.sdk;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.DeployGateEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class SendLogcatRequest extends Instruction {
    enum Position {
        Beginning,
        Content,
        Termination;

        private String label() {
            switch (this) {
                case Beginning:
                    return "beginning";
                case Content:
                    return "content";
                case Termination:
                    return "termination";
                default:
                    throw new IllegalStateException(String.format(Locale.US, "%s is not mapped", name()));
            }
        }
    }

    public static SendLogcatRequest createTermination(String processId) {
        return new SendLogcatRequest(processId, new ArrayList<String>(), Position.Termination);
    }

    public static SendLogcatRequest createBeginning(String processId) {
        return new SendLogcatRequest(processId, new ArrayList<String>(), Position.Beginning);
    }

    public final ArrayList<String> lines;
    public final Position position;
    @Nullable public final String captureId;
    private int retryCount;

    SendLogcatRequest(
            String pid,
            List<String> lines
    ) {
        this(pid, lines, Position.Content);
    }

    SendLogcatRequest(
            String pid,
            List<String> lines,
            String captureId
    ) {
        this(pid, lines, Position.Content, captureId);
    }

    /**
     * @param pid
     *         a process id. non-null
     * @param lines
     *         logcat contents if available. Zero value is an empty list.
     * @param position
     *         a position of this request. non-null
     */
    private SendLogcatRequest(
            String pid,
            List<String> lines,
            Position position
    ) {
        this(pid, lines, position, null);
    }

    /**
     * @param pid a process id. non-null
     * @param lines logcat contents if available. Zero value is an empty list.
     * @param position a position of this request. non-null
     * @param captureId the id of the capture. nullable
     */
    private SendLogcatRequest(
            String pid,
            List<String> lines,
            Position position,
            @Nullable String captureId
    ) {
        super(pid);
        this.lines = lines instanceof ArrayList ? (ArrayList<String>) lines : new ArrayList<>(lines);
        this.position = position;
        this.captureId = captureId;
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

        if (count == 1 || position != Position.Content) {
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

            splits.add(new SendLogcatRequest(gid, lines.subList(offset, endIndex), Position.Content));
        }

        return splits;
    }

    @Override
    void applyValues(Bundle extras) {
        extras.putStringArrayList(DeployGateEvent.EXTRA_LOG, lines);
        extras.putString(DeployGateEvent.EXTRA_BUNDLE_POSITION, position.label());
        extras.putString(DeployGateEvent.EXTRA_CAPTURE_ID, captureId);
    }
}
