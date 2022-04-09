package com.deploygate.sdk;

import android.os.Bundle;

import org.mockito.ArgumentMatcher;

import java.util.Objects;

import static org.mockito.internal.progress.ThreadSafeMockingProgress.mockingProgress;

class BundleMatcher {
    public static Bundle eq(Bundle expected) {
        mockingProgress().getArgumentMatcherStorage().reportMatcher(new Equals(expected));
        return expected;
    }

    private static class Equals implements ArgumentMatcher<Bundle> {
        private final Bundle expected;

        public Equals(Bundle expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Bundle argument) {
            if (expected.size() != argument.size()) {
                return false;
            }

            if (!expected.keySet().equals(argument.keySet())) {
                return false;
            }

            for (final String key : expected.keySet()) {
                if (!Objects.equals(expected.get(key), argument.get(key))) {
                    return false;
                }
            }

            return true;
        }
    }
}
