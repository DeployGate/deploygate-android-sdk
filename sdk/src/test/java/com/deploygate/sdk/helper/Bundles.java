package com.deploygate.sdk.helper;

import android.os.Bundle;

import java.util.Objects;

public class Bundles {
    public static boolean equals(
            Bundle left,
            Bundle right
    ) {
        if (left.size() != right.size()) {
            return false;
        }

        if (!left.keySet().equals(right.keySet())) {
            return false;
        }

        for (final String key : left.keySet()) {
            if (!Objects.equals(left.get(key), right.get(key))) {
                return false;
            }
        }

        return true;
    }
}
