package com.deploygate.sdk;

/**
 * A metadata content to represent feature compatibilities of the client app.
 *
 * NOTE: Do not remove any enum entry even if it's completely removed from the client app.
 */
enum Compatibility {
    UPDATE_MESSAGE_OF_BUILD(BuildConfig.UPDATE_MESSAGE_OF_BUILD),
    SERIALIZED_EXCEPTION(BuildConfig.SERIALIZED_EXCEPTION),
    LOGCAT_BUNDLE(BuildConfig.LOGCAT_BUNDLE),
    STREAMED_LOGCAT(BuildConfig.STREAMED_LOGCAT),

    DEVICE_CAPTURE(BuildConfig.DEVICE_CAPTURE);

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
