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
import java.util.Objects;

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

            SendLogcatRequest beginning = SendLogcatRequest.createBeginning("bsk1");

            BundleSubject.assertThat(beginning.toExtras()).isEqualTo(createLogExtra("bsk1", "unique_id", new ArrayList<String>(), "beginning", null));

            SendLogcatRequest content = new SendLogcatRequest("bsk2", new ArrayList<>(Arrays.asList("1", "2")), null);

            BundleSubject.assertThat(content.toExtras()).isEqualTo(createLogExtra("bsk2", "unique_id", new ArrayList<>(Arrays.asList("1", "2")), "content", null));

            SendLogcatRequest termination = SendLogcatRequest.createTermination("bsk3");

            BundleSubject.assertThat(termination.toExtras()).isEqualTo(createLogExtra("bsk3", "unique_id", new ArrayList<String>(), "termination", null));

            SendLogcatRequest capture = new SendLogcatRequest("bsk4", new ArrayList<>(Arrays.asList("1")), "capture_id");

            BundleSubject.assertThat(capture.toExtras()).isEqualTo(createLogExtra("bsk4", "unique_id", new ArrayList<>(Arrays.asList("1")), "content", "capture_id"));
        }
    }

    @Test
    public void splitInto_create_n_sublist() {
        SendLogcatRequest request = new SendLogcatRequest("bsk", arrayListOf(0, 10), null);

        List<SendLogcatRequest> singleRequests = request.splitInto(1);

        // 1 のときは split しない
        Truth.assertThat(singleRequests).hasSize(1);
        SendLogcatRequestSubject.assertThat(singleRequests.get(0)).isEqualTo(request);

        List<SendLogcatRequest> twoRequests = request.splitInto(2);
        Truth.assertThat(twoRequests).hasSize(2);
        SendLogcatRequestSubject.assertThat(twoRequests.get(0)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(0, 5), null);
        SendLogcatRequestSubject.assertThat(twoRequests.get(1)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(5, 5), null);

        List<SendLogcatRequest> threeRequests = request.splitInto(3);
        Truth.assertThat(threeRequests).hasSize(3);
        SendLogcatRequestSubject.assertThat(threeRequests.get(0)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(0, 3), null);
        SendLogcatRequestSubject.assertThat(threeRequests.get(1)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(3, 3), null);
        SendLogcatRequestSubject.assertThat(threeRequests.get(2)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(6, 4), null);

        List<SendLogcatRequest> nineRequests = request.splitInto(9);
        Truth.assertThat(nineRequests).hasSize(9);
        SendLogcatRequestSubject.assertThat(nineRequests.get(0)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(0, 1), null);
        SendLogcatRequestSubject.assertThat(nineRequests.get(1)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(1, 1), null);
        SendLogcatRequestSubject.assertThat(nineRequests.get(2)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(2, 1), null);
        SendLogcatRequestSubject.assertThat(nineRequests.get(3)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(3, 1), null);
        SendLogcatRequestSubject.assertThat(nineRequests.get(4)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(4, 1), null);
        SendLogcatRequestSubject.assertThat(nineRequests.get(5)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(5, 1), null);
        SendLogcatRequestSubject.assertThat(nineRequests.get(6)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(6, 1), null);
        SendLogcatRequestSubject.assertThat(nineRequests.get(7)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(7, 1), null);
        SendLogcatRequestSubject.assertThat(nineRequests.get(8)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(8, 2), null);

        List<SendLogcatRequest> overSplitRequests = request.splitInto(11);
        Truth.assertThat(overSplitRequests).hasSize(10);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(0)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(0, 1), null);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(1)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(1, 1), null);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(2)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(2, 1), null);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(3)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(3, 1), null);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(4)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(4, 1), null);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(5)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(5, 1), null);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(6)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(6, 1), null);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(7)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(7, 1), null);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(8)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(8, 1), null);
        SendLogcatRequestSubject.assertThat(overSplitRequests.get(9)).isSameInBundle(request.gid, SendLogcatRequest.Position.Content, arrayListOf(9, 1), null);

        SendLogcatRequest requestWithCapture = new SendLogcatRequest("bsk2", arrayListOf(0, 10), "capture_id");

        List<SendLogcatRequest> threeRequestsWithCapture = requestWithCapture.splitInto(3);
        Truth.assertThat(threeRequestsWithCapture).hasSize(3);
        SendLogcatRequestSubject.assertThat(threeRequestsWithCapture.get(0)).isSameInBundle(requestWithCapture.gid, SendLogcatRequest.Position.Content, arrayListOf(0, 3), "capture_id");
        SendLogcatRequestSubject.assertThat(threeRequestsWithCapture.get(1)).isSameInBundle(requestWithCapture.gid, SendLogcatRequest.Position.Content, arrayListOf(3, 3), "capture_id");
        SendLogcatRequestSubject.assertThat(threeRequestsWithCapture.get(2)).isSameInBundle(requestWithCapture.gid, SendLogcatRequest.Position.Content, arrayListOf(6, 4), "capture_id");
    }

    @Test
    public void beginning_splitInto_create_n_sublist() {
        SendLogcatRequest request = SendLogcatRequest.createBeginning("bsk");

        List<SendLogcatRequest> singleRequests = request.splitInto(1);

        Truth.assertThat(singleRequests).hasSize(1);
        SendLogcatRequestSubject.assertThat(singleRequests.get(0)).isEqualTo(request);

        // split 出来ない
        List<SendLogcatRequest> twoRequests = request.splitInto(2);
        Truth.assertThat(twoRequests).hasSize(1);
        SendLogcatRequestSubject.assertThat(twoRequests.get(0)).isEqualTo(request);
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
            String gid,
            String cid,
            ArrayList<String> lines,
            String positionLabel,
            String captureId
    ) {
        Bundle bundle = new Bundle();
        bundle.putString("e.gid", gid);
        bundle.putString("e.cid", cid);
        bundle.putStringArrayList("log", lines);
        bundle.putString("e.bundle-position", positionLabel);
        bundle.putString("e.capture-id", captureId);
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

        public void isSameInBundle(String expectedKey, SendLogcatRequest.Position expectedPosition, List<String> expectedLines, String captureId) {
            if (!actual.gid.equals(expectedKey)) {
                failWithActual(Fact.simpleFact(String.format(Locale.US, "%s is expected of %s", expectedKey, toString(actual))));
                return;
            }

            if (actual.position != expectedPosition) {
                failWithActual(Fact.simpleFact(String.format(Locale.US, "%s is expected to %s", actual.position.name(), expectedPosition.name())));
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

            if (!Objects.equals(actual.captureId, captureId)) {
                failWithActual(Fact.simpleFact(String.format(Locale.US, "%s is expected to be exactly included in %s",actual.captureId, captureId)));
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
            builder.append(request.gid);
            builder.append(", pos=");
            builder.append(request.position.name());
            builder.append(", lines=[");
            builder.append(String.join(", ", request.lines));
            builder.append(", capture_id=");
            builder.append(request.captureId);
            builder.append("]");
            builder.append(" }");
            return builder.toString();
        }
    }

}
