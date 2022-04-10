package com.deploygate.sdk;

public class CustomLogConfiguration {
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

    private CustomLogConfiguration(
    ) {
    }

    public static class Builder {
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

            return this;
        }

        /**
         * @param bufferSize
         *         the max size of the log buffer
         *
         * @return self
         */
        public Builder setBufferSize(int bufferSize) {
            return this;
        }

        public CustomLogConfiguration build() {
            return new CustomLogConfiguration();
        }
    }
}
