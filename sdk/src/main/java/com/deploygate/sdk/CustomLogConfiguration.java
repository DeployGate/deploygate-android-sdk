package com.deploygate.sdk;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.sdk.internal.annotations.Experimental;

@Experimental
public class CustomLogConfiguration {
    @Experimental
    public enum Backpressure {
        /**
         * SDK rejects new logs if buffer size is exceeded
         */
        PRESERVE_BUFFER,

        /**
         * SDK drops logs from the oldest if buffer size is exceeded
         */
        DROP_BUFFER_BY_OLDEST
    }

    /**
     * the log buffer is required until DeployGate client app receives BOOT_COMPLETED broadcast.
     * <p>
     * This is an experimental value.
     * <p>
     * - 10 seconds until boot-completed
     * - 10 logs per 1 seconds
     * - plus some buffer
     */
    private static final int DEFAULT_BUFFER_SIZE = 150;
    private static final int MAX_BUFFER_SIZE = DEFAULT_BUFFER_SIZE;

    public final Backpressure backpressure;
    public final int bufferSize;

    /**
     * Do not bypass {@link Builder} to instantiate this class.
     *
     * @see Builder
     */
    private CustomLogConfiguration(
            Backpressure backpressure,
            int bufferSize
    ) {
        this.backpressure = backpressure;
        this.bufferSize = bufferSize;
    }

    @Experimental
    public static class Builder {
        private Backpressure backpressure = Backpressure.DROP_BUFFER_BY_OLDEST;
        private int bufferSize = DEFAULT_BUFFER_SIZE;

        /**
         * @param backpressure
         *         the strategy of the backpressure in the log buffer
         *
         * @return self
         *
         * @see Backpressure
         */
        public Builder setBackpressure(Backpressure backpressure) {
            if (backpressure == null) {
                throw new IllegalArgumentException("backpressure must be non-null");
            }

            this.backpressure = backpressure;
            return this;
        }

        /**
         * @param bufferSize
         *         the max size of the log buffer
         *
         * @return self
         */
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
