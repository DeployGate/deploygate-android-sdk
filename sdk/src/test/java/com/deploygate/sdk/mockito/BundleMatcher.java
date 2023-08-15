package com.deploygate.sdk.mockito;

import static org.mockito.internal.progress.ThreadSafeMockingProgress.mockingProgress;

import android.os.Bundle;

import com.deploygate.sdk.helper.Bundles;

import org.mockito.ArgumentMatcher;

public class BundleMatcher {
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
            return Bundles.equals(expected, argument);
        }

        @Override
        public String toString() {
            return expected.toString();
        }
    }
}
