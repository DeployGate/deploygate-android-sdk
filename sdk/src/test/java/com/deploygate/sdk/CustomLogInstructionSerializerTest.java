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
import java.util.concurrent.TimeUnit;

import static com.deploygate.sdk.mockito.BundleMatcher.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class CustomLogInstructionSerializerTest {

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
        CustomLogInstructionSerializer customLogInstructionSerializer = new CustomLogInstructionSerializer(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            customLogInstructionSerializer.requestSendingLog("type", String.valueOf(i));
        }

        Shadows.shadowOf(customLogInstructionSerializer.getLooper()).idle();

        customLogInstructionSerializer.connect(service);

        Shadows.shadowOf(customLogInstructionSerializer.getLooper()).idle();

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
        CustomLogInstructionSerializer customLogInstructionSerializer = new CustomLogInstructionSerializer(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            customLogInstructionSerializer.requestSendingLog("type", String.valueOf(i));
        }

        Shadows.shadowOf(customLogInstructionSerializer.getLooper()).idle();

        customLogInstructionSerializer.connect(service);

        Shadows.shadowOf(customLogInstructionSerializer.getLooper()).idle();

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
    public void sendLog_always_returns_retriable_status_if_service_is_none() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();
        CustomLogInstructionSerializer customLogInstructionSerializer = new CustomLogInstructionSerializer(PACKAGE_NAME, configuration);

        CustomLog noIssue = new CustomLog("type", "noIssue");
        CustomLog successAfterRetries = new CustomLog("type", "successAfterRetries");
        CustomLog retryExceeded = new CustomLog("type", "retryExceeded");

        doNothing().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(noIssue.toExtras()));
        doThrow(TransactionTooLargeException.class).doThrow(DeadObjectException.class).doNothing().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(successAfterRetries.toExtras()));
        doThrow(RemoteException.class).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(retryExceeded.toExtras()));

        for (int i = 0; i < 10; i++) {
            Truth.assertThat(customLogInstructionSerializer.sendLog(noIssue)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_FAILURE_RETRIABLE);
            Truth.assertThat(customLogInstructionSerializer.sendLog(successAfterRetries)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_FAILURE_RETRIABLE);
            Truth.assertThat(customLogInstructionSerializer.sendLog(retryExceeded)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        }
    }

    @Test(timeout = 3000L)
    public void sendLog_uses_retry_barrier() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();
        CustomLogInstructionSerializer customLogInstructionSerializer = new CustomLogInstructionSerializer(PACKAGE_NAME, configuration);
        customLogInstructionSerializer.connect(service);

        Shadows.shadowOf(customLogInstructionSerializer.getLooper()).pause();

        CustomLog noIssue = new CustomLog("type", "noIssue");
        CustomLog successAfterRetries = new CustomLog("type", "successAfterRetries");
        CustomLog retryExceeded = new CustomLog("type", "retryExceeded");

        doNothing().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(noIssue.toExtras()));
        doThrow(TransactionTooLargeException.class).doThrow(DeadObjectException.class).doNothing().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(successAfterRetries.toExtras()));
        doThrow(RemoteException.class).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(retryExceeded.toExtras()));

        Truth.assertThat(customLogInstructionSerializer.sendLog(noIssue)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_SUCCESS);
        Truth.assertThat(customLogInstructionSerializer.sendLog(successAfterRetries)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(customLogInstructionSerializer.sendLog(successAfterRetries)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(customLogInstructionSerializer.sendLog(successAfterRetries)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_SUCCESS);
        Truth.assertThat(customLogInstructionSerializer.sendLog(retryExceeded)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(customLogInstructionSerializer.sendLog(retryExceeded)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(customLogInstructionSerializer.sendLog(retryExceeded)).isEqualTo(CustomLogInstructionSerializer.SEND_LOG_RESULT_FAILURE_RETRY_EXCEEDED);
    }

    @Test(timeout = 3000L)
    public void requestSendingLog_works_regardless_of_service() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();
        CustomLogInstructionSerializer customLogInstructionSerializer = new CustomLogInstructionSerializer(PACKAGE_NAME, configuration);

        // Don't connect a service

        for (int i = 0; i < 30; i++) {
            customLogInstructionSerializer.requestSendingLog("type", "body");
        }

        Shadows.shadowOf(customLogInstructionSerializer.getLooper()).idle();

        Truth.assertThat(customLogInstructionSerializer.getPendingCount()).isEqualTo(30);
    }

    @Test(timeout = 3000L)
    public void requestSendingLog_does_nothing_if_disabled() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();
        CustomLogInstructionSerializer customLogInstructionSerializer = new CustomLogInstructionSerializer(PACKAGE_NAME, configuration);

        customLogInstructionSerializer.setDisabled(true);

        for (int i = 0; i < 30; i++) {
            customLogInstructionSerializer.requestSendingLog("type", "body");
        }

        Truth.assertThat(customLogInstructionSerializer.hasHandlerInitialized()).isFalse();
        Truth.assertThat(customLogInstructionSerializer.getPendingCount()).isEqualTo(0);

        // Even if a service connection is established, this does nothing.
        customLogInstructionSerializer.connect(service);

        for (int i = 0; i < 30; i++) {
            customLogInstructionSerializer.requestSendingLog("type", "body");
        }

        Shadows.shadowOf(customLogInstructionSerializer.getLooper()).idle();

        Truth.assertThat(customLogInstructionSerializer.getPendingCount()).isEqualTo(0);
    }

    @Test(timeout = 3000L)
    public void retry_barrier_can_prevent_holding_logs_that_always_fail() throws RemoteException, InterruptedException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();
        CustomLogInstructionSerializer customLogInstructionSerializer = new CustomLogInstructionSerializer(PACKAGE_NAME, configuration);

        doThrow(RemoteException.class).when(service).sendEvent(any(), any(), any());

        customLogInstructionSerializer.connect(service);

        for (int i = 0; i < 10; i++) {
            CustomLog log = new CustomLog("type", String.valueOf(i));
            customLogInstructionSerializer.requestSendingLog(log.type, log.body);
        }

        Shadows.shadowOf(customLogInstructionSerializer.getLooper()).idle();

        while (customLogInstructionSerializer.getPendingCount() > 0) {
            Shadows.shadowOf(customLogInstructionSerializer.getLooper()).idleFor(100, TimeUnit.MILLISECONDS);
        }

        Truth.assertThat(customLogInstructionSerializer.getPendingCount()).isEqualTo(0);
        Mockito.verify(service, times((CustomLogInstructionSerializer.MAX_RETRY_COUNT + 1) * 10)).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), any());
    }

    @Test(timeout = 3000L)
    public void requestSendingLog_works_as_expected_with_retry_barrier() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();
        CustomLogInstructionSerializer customLogInstructionSerializer = new CustomLogInstructionSerializer(PACKAGE_NAME, configuration);

        CustomLog noIssue = new CustomLog("type", "noIssue");
        CustomLog successAfterRetries = new CustomLog("type", "successAfterRetries");
        CustomLog retryExceeded = new CustomLog("type", "retryExceeded");

        //noinspection unchecked
        doThrow(TransactionTooLargeException.class, DeadObjectException.class).doCallRealMethod().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(successAfterRetries.toExtras()));
        doThrow(RemoteException.class).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), eq(retryExceeded.toExtras()));

        customLogInstructionSerializer.connect(service);

        customLogInstructionSerializer.requestSendingLog(successAfterRetries.type, successAfterRetries.body);
        customLogInstructionSerializer.requestSendingLog(noIssue.type, noIssue.body);
        customLogInstructionSerializer.requestSendingLog(retryExceeded.type, retryExceeded.body);

        Shadows.shadowOf(customLogInstructionSerializer.getLooper()).idle();

        List<Bundle> extras = service.getEventExtraList(DeployGateEvent.ACTION_SEND_CUSTOM_LOG);

        Truth.assertThat(extras).hasSize(2);
        BundleSubject.assertThat(extras.get(0)).isEqualTo(successAfterRetries.toExtras());
        BundleSubject.assertThat(extras.get(1)).isEqualTo(noIssue.toExtras());
    }
}
