package com.deploygate.sdk.truth;

import static com.google.common.truth.Truth.assertAbout;

import android.os.Bundle;

import com.deploygate.sdk.helper.Bundles;
import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

import java.util.Locale;

public class BundleSubject extends Subject {
    public static Factory<BundleSubject, Bundle> bundles() {
        return new Factory<BundleSubject, Bundle>() {
            @Override
            public BundleSubject createSubject(
                    FailureMetadata metadata,
                    Bundle actual
            ) {
                return new BundleSubject(metadata, actual);
            }
        };
    }

    public static BundleSubject assertThat(Bundle actual) {
        return assertAbout(bundles()).that(actual);
    }

    private final Bundle actual;

    /**
     * Constructor for use by subclasses. If you want to create an instance of this class itself, call
     * {@link Subject#check(String, Object ..) check(...)}{@code .that(actual)}.
     *
     * @param metadata
     * @param actual
     */
    protected BundleSubject(
            FailureMetadata metadata,
            Bundle actual
    ) {
        super(metadata, actual);
        this.actual = actual;
    }

    @Override
    public void isEqualTo(Object expected) {
        if (!Bundles.equals((Bundle) expected, actual)) {
            failWithActual(Fact.simpleFact(String.format(Locale.US, "%s to be same to %s", expected.toString(), actual.toString())));
        }
    }

    @Override
    public void isNotEqualTo(Object unexpected) {
        if (Bundles.equals((Bundle) unexpected, actual)) {
            failWithActual(Fact.simpleFact(String.format(Locale.US, "%s to unexpectedly be same to %s", unexpected.toString(), actual.toString())));
        }
    }
}
