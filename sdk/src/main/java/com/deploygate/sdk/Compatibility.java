package com.deploygate.sdk;

enum Compatibility {
    UPDATE_MESSAGE_OF_BUILD(1),
    SERIALIZED_EXCEPTION(1 << 1),
    LOGCAT_BUNDLE(1 << 2),
    STREAMED_LOGCAT(1 << 3);

    final int bitMask;

    Compatibility(int bitMask) {
        this.bitMask = bitMask;
    }

    /**
     * @return sum of all bits
     */
    static int all() {
        int bit = 0;

        for (Compatibility compatibility : Compatibility.values()) {
            bit |= compatibility.bitMask;
        }

        return bit;
    }

    /**
     * All values must be *inclusive*.
     * Use manifest metadata to know if a feature is supported on the client app.
     */
    @Deprecated
    static final class ClientVersion {
        static final int SUPPORT_UPDATE_MESSAGE_OF_BUILD = 39;
        static final int SUPPORT_SERIALIZED_EXCEPTION = 42;
    }
}
