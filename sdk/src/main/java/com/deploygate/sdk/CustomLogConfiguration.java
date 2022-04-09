package com.deploygate.sdk;

import com.deploygate.sdk.internal.Logger;

public class CustomLogConfiguration {
    private static final int MAX_BUFFER_SIZE = 100; // FIXME experimental

    public final Backpressure backpressure;
    public final int bufferSize;

    private CustomLogConfiguration(
            Backpressure backpressure,
            int bufferSize
    ) {
        this.backpressure = backpressure;
        this.bufferSize = bufferSize;
    }

    public static class Builder {
        private Backpressure backpressure = Backpressure.DROP_OLDEST;
        private int bufferSize = MAX_BUFFER_SIZE;

        public Builder setBackpressure(Backpressure backpressure) {
            if (backpressure == null) {
                throw new IllegalArgumentException("backpressure must be non-null");
            }

            this.backpressure = backpressure;
            return this;
        }

        public Builder setBufferSize(int bufferSize) {
            if (bufferSize <= 0) {
                throw new IllegalArgumentException("buffer size must be greater than 0");
            }

            if (bufferSize > MAX_BUFFER_SIZE) {
                Logger.w("buffer size is exceeded %d so it's rounded to %d", bufferSize, MAX_BUFFER_SIZE);
                bufferSize = MAX_BUFFER_SIZE;
            }

            this.bufferSize = bufferSize;
            return this;
        }

        public CustomLogConfiguration build() {
            return new CustomLogConfiguration(backpressure, bufferSize);
        }
    }
}
