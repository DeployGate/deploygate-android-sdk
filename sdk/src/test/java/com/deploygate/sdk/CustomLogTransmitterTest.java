package com.deploygate.sdk;

import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.IDeployGateSdkService;
import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.verification.VerificationMode;
import org.robolectric.Shadows;
import org.robolectric.annotation.LooperMode;

import java.util.ArrayList;
import java.util.List;

import static com.deploygate.sdk.mockito.BundleMatcher.eq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class CustomLogTransmitterTest {

    private static final String PACKAGE_NAME = "com.deploygate.sample";

    @Mock
    private IDeployGateSdkService service;

    private CustomLogTransmitter customLogTransmitter;

    private CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);

        customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);
    }

    @Test(timeout = 3000L)
    public void check_buffer_size_works_with_drop_by_oldest() throws RemoteException, InterruptedException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().setBufferSize(5).setBackpressure(CustomLogConfiguration.Backpressure.DROP_BUFFER_BY_OLDEST).build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            customLogTransmitter.transmit("type", String.valueOf(i));
        }

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        customLogTransmitter.connect(service);

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        for (int i = 0; i < 5; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));
            Mockito.verify(service, Mockito.never()).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(log.toExtras()));
        }

        VerificationMode once = Mockito.times(1);

        for (int i = 5; i < 10; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));
            Mockito.verify(service, once).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(log.toExtras()));
        }
    }

    @Test(timeout = 3000L)
    public void check_buffer_size_works_with_preserve_buffer() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().setBufferSize(5).setBackpressure(CustomLogConfiguration.Backpressure.PRESERVE_BUFFER).build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            customLogTransmitter.transmit("type", String.valueOf(i));
        }

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        customLogTransmitter.connect(service);

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        VerificationMode once = Mockito.times(1);

        for (int i = 0; i < 5; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));
            Mockito.verify(service, once).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(log.toExtras()));
        }

        for (int i = 5; i < 10; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));
            Mockito.verify(service, Mockito.never()).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(log.toExtras()));
        }
    }

    @Test(timeout = 3000L)
    public void transmit_works_regardless_of_service() throws RemoteException {
        customLogTransmitter.connect(service);

        CustomLog noIssue = new CustomLog("type", "noIssue");
        CustomLog successAfterRetries = new CustomLog("type", "successAfterRetries");
        CustomLog retryExceeded = new CustomLog("type", "retryExceeded");

        doNothing().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(noIssue.toExtras()));
        doThrow(TransactionTooLargeException.class).doThrow(DeadObjectException.class).doNothing().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(successAfterRetries.toExtras()));
        doThrow(RemoteException.class).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(retryExceeded.toExtras()));

        Truth.assertThat(customLogTransmitter.sendLog(noIssue)).isEqualTo(CustomLogTransmitter.SEND_LOG_RESULT_SUCCESS);
        Truth.assertThat(customLogTransmitter.sendLog(successAfterRetries)).isEqualTo(CustomLogTransmitter.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(customLogTransmitter.sendLog(successAfterRetries)).isEqualTo(CustomLogTransmitter.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(customLogTransmitter.sendLog(successAfterRetries)).isEqualTo(CustomLogTransmitter.SEND_LOG_RESULT_SUCCESS);
        Truth.assertThat(customLogTransmitter.sendLog(retryExceeded)).isEqualTo(CustomLogTransmitter.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(customLogTransmitter.sendLog(retryExceeded)).isEqualTo(CustomLogTransmitter.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(customLogTransmitter.sendLog(retryExceeded)).isEqualTo(CustomLogTransmitter.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(customLogTransmitter.sendLog(retryExceeded)).isEqualTo(CustomLogTransmitter.SEND_LOG_RESULT_FAILURE_RETRY_EXCEEDED);
    }

    @Test(timeout = 3000L)
    public void retry_barrier_can_prevent_holding_logs_that_always_fail() {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().setBufferSize(8).build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            customLogTransmitter.transmit("type", String.valueOf(i));
        }

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        Truth.assertThat(customLogTransmitter.getPendingCount()).isEqualTo(8);
    }

    @Test(timeout = 3000L)
    public void transmit_works_as_expected_with_retry_barrier() throws RemoteException {
        List<Bundle> extras = new ArrayList<>();
        Answer capture = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Bundle extra = invocation.getArgument(2);
                extras.add(extra);
                return null;
            }
        };

        CustomLog noIssue = new CustomLog("type", "noIssue");
        CustomLog successAfterRetries = new CustomLog("type", "successAfterRetries");
        CustomLog retryExceeded = new CustomLog("type", "retryExceeded");

        doAnswer(capture).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(noIssue.toExtras()));
        doThrow(TransactionTooLargeException.class).doThrow(DeadObjectException.class).doAnswer(capture).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(successAfterRetries.toExtras()));
        doThrow(RemoteException.class).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(retryExceeded.toExtras()));

        customLogTransmitter.connect(service);

        customLogTransmitter.transmit(successAfterRetries.type, successAfterRetries.body);
        customLogTransmitter.transmit(noIssue.type, noIssue.body);
        customLogTransmitter.transmit(retryExceeded.type, retryExceeded.body);

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        Truth.assertThat(extras).hasSize(2);
        Truth.assertThat(extras.get(0).getString(DeployGateEvent.EXTRA_LOG)).isEqualTo(successAfterRetries.body);
        Truth.assertThat(extras.get(1).getString(DeployGateEvent.EXTRA_LOG)).isEqualTo(noIssue.body);
    }
}
