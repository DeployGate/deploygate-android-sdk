package com.deploygate.sdk;

import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.sdk.truth.BundleSubject;
import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.FakeDeployGateClientService;
import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.robolectric.Shadows;
import org.robolectric.annotation.LooperMode;

import java.util.List;

import static com.deploygate.sdk.mockito.BundleMatcher.eq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class CustomLogTransmitterTest {

    private static final String PACKAGE_NAME = "com.deploygate.sample";

    private FakeDeployGateClientService service;

    @Before
    public void before() {
        service = Mockito.spy(new FakeDeployGateClientService(PACKAGE_NAME));
    }

    @Test(timeout = 3000L)
    public void check_buffer_size_works_with_drop_by_oldest() throws RemoteException, InterruptedException {
        final int bufferSize = 5;

        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().setBufferSize(bufferSize).setBackpressure(CustomLogConfiguration.Backpressure.DROP_BUFFER_BY_OLDEST).build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            customLogTransmitter.transmit("type", String.valueOf(i));
        }

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        customLogTransmitter.connect(service);

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        for (int i = 0; i < bufferSize; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));
            Mockito.verify(service, never()).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(log.toExtras()));
        }

        VerificationMode once = Mockito.times(1);
        InOrder inOrder = Mockito.inOrder(service);

        for (int i = bufferSize; i < 10; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));
            inOrder.verify(service, once).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(log.toExtras()));
        }
    }

    @Test(timeout = 3000L)
    public void check_buffer_size_works_with_preserve_buffer() throws RemoteException {
        final int bufferSize = 5;

        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().setBufferSize(bufferSize).setBackpressure(CustomLogConfiguration.Backpressure.PRESERVE_BUFFER).build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            customLogTransmitter.transmit("type", String.valueOf(i));
        }

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        customLogTransmitter.connect(service);

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        VerificationMode once = Mockito.times(1);
        InOrder inOrder = Mockito.inOrder(service);

        for (int i = 0; i < bufferSize; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));
            inOrder.verify(service, once).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(log.toExtras()));
        }

        for (int i = bufferSize; i < 10; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));
            Mockito.verify(service, never()).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(log.toExtras()));
        }
    }

    @Test(timeout = 3000L)
    public void transmit_works_regardless_of_service() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);
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
    public void retry_barrier_can_prevent_holding_logs_that_always_fail() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().setBufferSize(8).build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));

            doThrow(RemoteException.class).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(log.toExtras()));

            customLogTransmitter.transmit(log.type, log.body);
        }

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        Truth.assertThat(customLogTransmitter.getPendingCount()).isEqualTo(8);
    }

    @Test(timeout = 3000L)
    public void transmit_works_as_expected_with_retry_barrier() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);

        CustomLog noIssue = new CustomLog("type", "noIssue");
        CustomLog successAfterRetries = new CustomLog("type", "successAfterRetries");
        CustomLog retryExceeded = new CustomLog("type", "retryExceeded");

        doThrow(TransactionTooLargeException.class).doThrow(DeadObjectException.class).doCallRealMethod().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(successAfterRetries.toExtras()));
        doThrow(RemoteException.class).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(retryExceeded.toExtras()));

        customLogTransmitter.connect(service);

        customLogTransmitter.transmit(successAfterRetries.type, successAfterRetries.body);
        customLogTransmitter.transmit(noIssue.type, noIssue.body);
        customLogTransmitter.transmit(retryExceeded.type, retryExceeded.body);

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        List<Bundle> extras = service.getEventExtraList(DeployGateEvent.ACTION_SEND_CUSTOM_LOG);

        Truth.assertThat(extras).hasSize(2);
        BundleSubject.assertThat(extras.get(0)).isEqualTo(successAfterRetries.toExtras());
        BundleSubject.assertThat(extras.get(1)).isEqualTo(noIssue.toExtras());
    }
}
