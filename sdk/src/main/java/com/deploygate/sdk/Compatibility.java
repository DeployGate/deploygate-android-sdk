package com.deploygate.sdk;

final class Compatibility {
    /**
     * All values must be *inclusive*. {@link Integer#MAX_VALUE} means the version is not fixed yet.
     */
    static final class ClientVersion {
        /**
         * older clients crash due to ClassNotFound if SDK sends custom exceptions.
         */
        static final int SUPPORT_SERIALIZED_EXCEPTION = 42;

        /**
         * older clients don't take care of buffered time.
         */
        static final int SUPPORT_BUFFERED_TIME_IN_MILLI_SECONDS = Integer.MAX_VALUE;
    }
}
