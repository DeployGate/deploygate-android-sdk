package com.deploygate.sdk;

import android.text.TextUtils;

import java.util.Locale;
import java.util.UUID;

class ClientId {
    private static final String CLIENT_ID_PREFIX = "c:";

    static String generate() {
        return String.format(Locale.US, "%s%s", CLIENT_ID_PREFIX, UUID.randomUUID().toString());
    }

    static boolean isValid(String id) {
        if (TextUtils.isEmpty(id) || !id.startsWith(CLIENT_ID_PREFIX)) {
            return false;
        }

        String maybeUuid = id.substring(CLIENT_ID_PREFIX.length());

        try {
            //noinspection ResultOfMethodCallIgnored
            UUID.fromString(maybeUuid);
            return true;
        } catch (Throwable ignore) {
            return false;
        }
    }
}
