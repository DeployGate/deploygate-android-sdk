package com.deploygate.sdk;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.sdk.helper.FakeLogcat;
import com.deploygate.sdk.mockito.BundleMatcher;
import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.FakeDeployGateClientService;
import com.google.common.truth.Truth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Shadows;
import org.robolectric.annotation.LooperMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.deploygate.sdk.mockito.BundleMatcher.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class LogcatInstructionSerializerTest {

    private static final String PACKAGE_NAME = "com.deploygate.sample";

    private FakeDeployGateClientService service;
    private LogcatInstructionSerializer instructionSerializer;
    private MockedStatic<LogcatProcess.LogcatWatcher> mockStatic;
    private List<Process> processes;

    @Before
    public void before() {
        service = Mockito.spy(new FakeDeployGateClientService(PACKAGE_NAME));
        processes = new ArrayList<>();

        mockStatic = mockStatic(LogcatProcess.LogcatWatcher.class);
        mockStatic.when(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                LogcatProcess.LogcatWatcher.toArrayList(ArgumentMatchers.<String>anyCollection());
            }
        }).thenCallRealMethod();
        mockStatic.when(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                LogcatProcess.LogcatWatcher.execLogcatCommand(anyBoolean());
            }
        }).thenAnswer(new Answer<Process>() {
            private final Random RANDOM = new Random();

            @Override
            public Process answer(InvocationOnMock invocation) throws Throwable {
                FakeLogcat logcat = new FakeLogcat(Math.max(1, RANDOM.nextInt(700)));
                processes.add(logcat);
                return logcat;
            }
        });
    }

    @After
    public void after() {
        if (instructionSerializer != null) {
            instructionSerializer.disconnect();
            instructionSerializer.halt();
        }

        for (Process process: processes) {
            if (process.isAlive()) {
                process.destroy();
            }
        }

        if (mockStatic != null) {
            mockStatic.close();
        }
    }

    @Test(timeout = 3000L)
    public void sendSingleChunk_always_returns_retriable_status_if_service_is_none() throws RemoteException {
        instructionSerializer = new LogcatInstructionSerializer(PACKAGE_NAME);

        instructionSerializer.requestSendingLogcat(true);

        SendLogcatRequest chunk1 = new SendLogcatRequest("tid1", new ArrayList<>(Arrays.asList("line1", "line2", "line3")));
        SendLogcatRequest chunk2 = new SendLogcatRequest("tid2", new ArrayList<>(Arrays.asList("line4", "line5", "line6")));
        SendLogcatRequest chunk3 = new SendLogcatRequest("tid3", new ArrayList<>(Arrays.asList("line7", "line8", "line9")));

        doNothing().when(service).sendEvent(anyString(), anyString(), any(Bundle.class));

        Truth.assertThat(instructionSerializer.sendSingleChunk(chunk1)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(instructionSerializer.sendSingleChunk(chunk2)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(instructionSerializer.sendSingleChunk(chunk3)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_FAILURE_RETRIABLE);

        Mockito.verifyNoInteractions(service);
    }

    @Test(timeout = 3000L)
    public void sendSingleChunk_uses_retry_barrier() throws RemoteException {
        instructionSerializer = new LogcatInstructionSerializer(PACKAGE_NAME);
        instructionSerializer.connect(service);

        Shadows.shadowOf(instructionSerializer.getHandler().getLooper()).pause();

        SendLogcatRequest noIssue = new SendLogcatRequest("noIssue", new ArrayList<>(Arrays.asList("line1", "line2", "line3")));
        SendLogcatRequest successAfterRetries = new SendLogcatRequest("successAfterRetries", new ArrayList<>(Arrays.asList("line4", "line5", "line6")));
        SendLogcatRequest retryExceeded = new SendLogcatRequest("retryExceeded", new ArrayList<>(Arrays.asList("line7", "line8", "line9")));
        SendLogcatRequest chunkRequest = new SendLogcatRequest("chunkRequest", new ArrayList<>(Arrays.asList("line10", "line11", "line12")));

        doNothing().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_LOGCAT), BundleMatcher.eq(noIssue.toExtras()));
        doThrow(RemoteException.class).doNothing().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_LOGCAT), eq(successAfterRetries.toExtras()));
        doThrow(RemoteException.class).when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_LOGCAT), eq(retryExceeded.toExtras()));
        doThrow(TransactionTooLargeException.class).doNothing().when(service).sendEvent(eq(PACKAGE_NAME), eq(DeployGateEvent.ACTION_SEND_LOGCAT), eq(chunkRequest.toExtras()));

        Truth.assertThat(instructionSerializer.sendSingleChunk(noIssue)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_SUCCESS);
        Truth.assertThat(instructionSerializer.sendSingleChunk(successAfterRetries)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(instructionSerializer.sendSingleChunk(successAfterRetries)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_SUCCESS);
        Truth.assertThat(instructionSerializer.sendSingleChunk(retryExceeded)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(instructionSerializer.sendSingleChunk(retryExceeded)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_FAILURE_RETRIABLE);
        Truth.assertThat(instructionSerializer.sendSingleChunk(retryExceeded)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_FAILURE_RETRY_EXCEEDED);
        Truth.assertThat(instructionSerializer.sendSingleChunk(chunkRequest)).isEqualTo(LogcatInstructionSerializer.SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE);
    }

    @Test(timeout = 3000L)
    public void requestSendingLogcat_works_regardless_of_service() throws RemoteException {
        instructionSerializer = new LogcatInstructionSerializer(PACKAGE_NAME);

        // Don't connect a service

        for (int i = 0; i < 10; i++) {
            instructionSerializer.requestSendingLogcat(true);
        }

        Shadows.shadowOf(instructionSerializer.getHandler().getLooper()).idle();

        // don't fail
    }

    @Test(timeout = 3000L)
    public void requestSendingLogcat_does_nothing_if_disabled() throws RemoteException {
        instructionSerializer = new LogcatInstructionSerializer(PACKAGE_NAME);

        instructionSerializer.setEnabled(false);

        for (int i = 0; i < 30; i++) {
            Truth.assertThat(instructionSerializer.requestSendingLogcat(i % 2 == 0)).isFalse();
        }

        // Even if a service connection is established, this does nothing.
        instructionSerializer.connect(service);

        for (int i = 0; i < 30; i++) {
            Truth.assertThat(instructionSerializer.requestSendingLogcat(i % 2 == 0)).isFalse();
        }

        Shadows.shadowOf(instructionSerializer.getHandler().getLooper()).idle();

        Truth.assertThat(instructionSerializer.getHandler().hasMessages(LogcatInstructionSerializer.WHAT_SEND_LOGCAT)).isFalse();
    }
}
