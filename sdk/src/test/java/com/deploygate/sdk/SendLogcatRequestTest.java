package com.deploygate.sdk;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.sdk.truth.BundleSubject;
import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.google.common.truth.Truth.assertAbout;

@RunWith(AndroidJUnit4.class)
public class SendLogcatRequestTest {

    @Test
    public void toExtras_must_be_valid_format() {
        try (MockedStatic<ClientId> cidMock = Mockito.mockStatic(ClientId.class)) {
            cidMock.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    ClientId.generate();
                }
            }).thenReturn("unique_id");

            SendLogcatRequest request = new SendLogcatRequest("bsk", new ArrayList<>(Arrays.asList("1", "2")));

            BundleSubject.assertThat(request.toExtras()).isEqualTo(createLogExtra("bsk", "unique_id", new ArrayList<>(Arrays.asList("1", "2")), false));

            SendLogcatRequest termination = SendLogcatRequest.createTermination("bsk2");

            BundleSubject.assertThat(termination.toExtras()).isEqualTo(createLogExtra("bsk2", "unique_id", new ArrayList<String>(), true));
        }
    }

    @Test
    public void splitInto_create_n_sublist() {
        SendLogcatRequest request = new SendLogcatRequest("bsk", arrayListOf(0, 10));

        List<SendLogcatRequest> singleRequests = request.splitInto(1);

        // 1 のときは split しない
        Truth.assertThat(singleRequests).hasSize(1);
        SendLogcatRequestSubject.assertThat(singleRequests.get(0)).isEqualTo(request);

        List<SendLogcatRequest> twoRequests = request.splitInto(2);
        Truth.assertThat(twoRequests).hasSize(2);
        SendLogcatRequestSubject.assertThat(twoRequests.get(0)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(0, 5));
        SendLogcatRequestSubject.assertThat(twoRequests.get(1)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(5, 5));

        List<SendLogcatRequest> threeRequests = request.splitInto(3);
        Truth.assertThat(threeRequests).hasSize(3);
        SendLogcatRequestSubject.assertThat(threeRequests.get(0)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(0, 3));
        SendLogcatRequestSubject.assertThat(threeRequests.get(1)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(3, 3));
        SendLogcatRequestSubject.assertThat(threeRequests.get(2)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(6, 4));

        List<SendLogcatRequest> nineRequests = request.splitInto(9);
        Truth.assertThat(nineRequests).hasSize(9);
        SendLogcatRequestSubject.assertThat(nineRequests.get(0)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(0, 1));
        SendLogcatRequestSubject.assertThat(nineRequests.get(1)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(1, 1));
        SendLogcatRequestSubject.assertThat(nineRequests.get(2)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(2, 1));
        SendLogcatRequestSubject.assertThat(nineRequests.get(3)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(3, 1));
        SendLogcatRequestSubject.assertThat(nineRequests.get(4)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(4, 1));
        SendLogcatRequestSubject.assertThat(nineRequests.get(5)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(5, 1));
        SendLogcatRequestSubject.assertThat(nineRequests.get(6)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(6, 1));
        SendLogcatRequestSubject.assertThat(nineRequests.get(7)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(7, 1));
        SendLogcatRequestSubject.assertThat(nineRequests.get(8)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(8, 2));

        List<SendLogcatRequest> overSplitRequests = request.splitInto(11);
        Truth.assertThat(overSplitRequests).hasSize(10);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(0)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(0, 1));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(1)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(1, 1));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(2)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(2, 1));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(3)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(3, 1));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(4)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(4, 1));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(5)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(5, 1));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(6)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(6, 1));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(7)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(7, 1));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(8)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(8, 1));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(9)).isSameInBundle(request.bundleSessionKey, false, arrayListOf(9, 1));
    }

    @Test
    public void termination_splitInto_create_n_sublist() {
        SendLogcatRequest request = SendLogcatRequest.createTermination("bsk");

        List<SendLogcatRequest> singleRequests = request.splitInto(1);

        Truth.assertThat(singleRequests).hasSize(1);
        SendLogcatRequestSubject.assertThat(singleRequests.get(0)).isEqualTo(request);

        // split 出来ない
        List<SendLogcatRequest> twoRequests = request.splitInto(2);
        Truth.assertThat(twoRequests).hasSize(1);
        SendLogcatRequestSubject.assertThat(twoRequests.get(0)).isEqualTo(request);
    }

    private static Bundle createLogExtra(
            String bundleId,
            String cid,
            ArrayList<String> lines,
            boolean isBundleTermination
    ) {
        Bundle bundle = new Bundle();
        bundle.putString("e.bundle-session-key", bundleId);
        bundle.putString("e.cid", cid);
        bundle.putStringArrayList("log", lines);
        bundle.putBoolean("e.is-bundle-termination", isBundleTermination);
        return bundle;
    }

    private static ArrayList<String> arrayListOf(int startInclusive, int count) {
        ArrayList<String> list = new ArrayList<>();

        for (int i = startInclusive, max = startInclusive + count; i < max; i++) {
            list.add(String.valueOf(i));
        }

        return list;
    }

    private static class SendLogcatRequestSubject extends Subject {
        public static Factory<SendLogcatRequestSubject, SendLogcatRequest> sendLogcatRequests() {
            return new Factory<SendLogcatRequestSubject, SendLogcatRequest>() {
                @Override
                public SendLogcatRequestSubject createSubject(
                        FailureMetadata metadata,
                        SendLogcatRequest actual
                ) {
                    return new SendLogcatRequestSubject(metadata, actual);
                }
            };
        }

        public static SendLogcatRequestSubject assertThat(SendLogcatRequest actual) {
            return assertAbout(sendLogcatRequests()).that(actual);
        }

        private final SendLogcatRequest actual;

        protected SendLogcatRequestSubject(
                FailureMetadata metadata,
                SendLogcatRequest actual
        ) {
            super(metadata, actual);
            this.actual = actual;
        }

        public void isSameInBundle(String expectedKey, boolean expectedTermination, List<String> expectedLines) {
            if (!actual.bundleSessionKey.equals(expectedKey)) {
                failWithActual(Fact.simpleFact(String.format(Locale.US, "%s is expected of %s", expectedKey, toString(actual))));
                return;
            }

            if (actual.isBundleTermination != expectedTermination) {
                failWithActual(Fact.simpleFact(String.format(Locale.US, "termination (%s) is expected to %s", expectedTermination, !expectedTermination)));
                return;
            }

            if (actual.lines.size() != expectedLines.size()) {
                failWithActual(Fact.simpleFact(String.format(Locale.US, "%s is expected to be exactly included in %s", String.join(", ", expectedLines), toString(actual))));
                return;
            }

            for (int i = 0; i < expectedLines.size(); i++) {
                if (!expectedLines.get(i).equals(actual.lines.get(i))) {
                    failWithActual(Fact.simpleFact(String.format(Locale.US, "%s is expected to be exactly included in %s", String.join(", ", expectedLines), toString(actual))));
                    return;
                }
            }
        }

        @Override
        public void isEqualTo(Object expected) {
            Truth.assertThat(expected).isInstanceOf(SendLogcatRequest.class);

            SendLogcatRequest request = (SendLogcatRequest) expected;

            if (actual.cid.equals(request.cid)) {
                return;
            }

            failWithActual(Fact.simpleFact(String.format(Locale.US, "%s is expected to have %s", toString(request), actual.cid)));
        }

        private static String toString(SendLogcatRequest request) {
            StringBuilder builder = new StringBuilder();
            builder.append("{ ");
            builder.append("cid=");
            builder.append(request.cid);
            builder.append(", bsk=");
            builder.append(request.bundleSessionKey);
            builder.append(", isBundleTermination=");
            builder.append(request.isBundleTermination);
            builder.append(", lines=[");
            builder.append(String.join(", ", request.lines));
            builder.append("]");
            builder.append(" }");
            return builder.toString();
        }
    }

}
