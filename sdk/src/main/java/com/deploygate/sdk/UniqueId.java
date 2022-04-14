package com.deploygate.sdk;

import java.util.UUID;

class UniqueId {
    static String generate() {
        return UUID.randomUUID().toString();
    }
}
