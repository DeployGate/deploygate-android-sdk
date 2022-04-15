package com.deploygate.sdk;

enum Compatibility {
    UPDATE_MESSAGE_OF_BUILD(0b1),
    SERIALIZED_EXCEPTION(0b10),
    LOGCAT_BUNDLE(0b100);

    final int bitMask;

    Compatibility(int bitMask) {
        this.bitMask = bitMask;
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
