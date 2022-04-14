package com.deploygate.sdk;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.sdk.truth.BundleSubject;
import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertAbout;

@RunWith(AndroidJUnit4.class)
public class SendLogcatRequestTest {

    @Test
    public void toExtras_must_be_valid_format() {
        SendLogcatRequest request = new SendLogcatRequest("watchId", new ArrayList<>(Arrays.asList("1", "2")));

        BundleSubject.assertThat(request.toExtras()).isEqualTo(createLogExtra("watchId", new ArrayList<>(Arrays.asList("1", "2"))));
    }

    @Test
    public void splitInto_create_n_sublist() {
        SendLogcatRequest request = new SendLogcatRequest("watchId", arrayListOf(0, 10));

        List<SendLogcatRequest> singleRequests = request.splitInto(1);

        Truth.assertThat(singleRequests).hasSize(1);
        SendLogcatRequestSubject.assertThat(singleRequests.get(0)).isEqualTo(request);

        List<SendLogcatRequest> twoRequests = request.splitInto(2);
        Truth.assertThat(twoRequests).hasSize(2);
        SendLogcatRequestSubject.assertThat(twoRequests.get(0)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(0, 5)));
        SendLogcatRequestSubject.assertThat(twoRequests.get(1)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(5, 5)));

        List<SendLogcatRequest> threeRequests = request.splitInto(3);
        Truth.assertThat(threeRequests).hasSize(3);
        SendLogcatRequestSubject.assertThat(threeRequests.get(0)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(0, 3)));
        SendLogcatRequestSubject.assertThat(threeRequests.get(1)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(3, 3)));
        SendLogcatRequestSubject.assertThat(threeRequests.get(2)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(6, 4)));

        List<SendLogcatRequest> nineRequests = request.splitInto(9);
        Truth.assertThat(nineRequests).hasSize(9);
        SendLogcatRequestSubject.assertThat(nineRequests.get(0)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(0, 1)));
        SendLogcatRequestSubject.assertThat(nineRequests.get(1)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(1, 1)));
        SendLogcatRequestSubject.assertThat(nineRequests.get(2)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(2, 1)));
        SendLogcatRequestSubject.assertThat(nineRequests.get(3)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(3, 1)));
        SendLogcatRequestSubject.assertThat(nineRequests.get(4)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(4, 1)));
        SendLogcatRequestSubject.assertThat(nineRequests.get(5)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(5, 1)));
        SendLogcatRequestSubject.assertThat(nineRequests.get(6)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(6, 1)));
        SendLogcatRequestSubject.assertThat(nineRequests.get(7)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(7, 1)));
        SendLogcatRequestSubject.assertThat(nineRequests.get(8)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(8, 2)));

        List<SendLogcatRequest> overSplitRequests = request.splitInto(11);
        Truth.assertThat(overSplitRequests).hasSize(10);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(0)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(0, 1)));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(1)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(1, 1)));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(2)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(2, 1)));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(3)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(3, 1)));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(4)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(4, 1)));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(5)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(5, 1)));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(6)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(6, 1)));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(7)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(7, 1)));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(8)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(8, 1)));
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(9)).isEqualTo(new SendLogcatRequest(request.watchId, arrayListOf(9, 1)));
    }

    private static Bundle createLogExtra(
            String watchId,
            ArrayList<String> lines
    ) {
        Bundle bundle = new Bundle();
        bundle.putString("logId", watchId);
        bundle.putStringArrayList("log", lines);
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

        @Override
        public void isEqualTo(Object expected) {
            Truth.assertThat(expected).isInstanceOf(SendLogcatRequest.class);

            SendLogcatRequest request = (SendLogcatRequest) expected;

            if (!actual.watchId.equals(request.watchId) || actual.lines.size() != request.lines.size()) {
                failWithActual(Fact.simpleFact(String.format(Locale.US, "%s is expected to %s", toString(request), toString(actual))));
                return;
            }

            for (int i = 0; i < request.lines.size(); i++) {
                if (!request.lines.get(i).equals(actual.lines.get(i))) {
                    failWithActual(Fact.simpleFact(String.format(Locale.US, "%s is expected to %s", toString(request), toString(actual))));
                    return;
                }
            }
        }

        private static String toString(SendLogcatRequest request) {
            StringBuilder builder = new StringBuilder();
            builder.append("{ ");
            builder.append("watchId=");
            builder.append(request.watchId);
            builder.append(", lines=[");
            builder.append(String.join(", ", request.lines));
            builder.append("]");
            builder.append(" }");
            return builder.toString();
        }
    }

}
