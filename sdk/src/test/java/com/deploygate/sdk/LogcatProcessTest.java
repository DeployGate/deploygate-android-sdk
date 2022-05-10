package com.deploygate.sdk;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.sdk.helper.FakeLogcat;
import com.google.common.truth.Truth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mockStatic;

@RunWith(AndroidJUnit4.class)
public class LogcatProcessTest {
    private FakeLogcat fakeLogcat;
    private MockedStatic<LogcatProcess.LogcatWatcher> mockStatic;

    @Before
    public void before() {
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
            @Override
            public Process answer(InvocationOnMock invocation) throws Throwable {
                return fakeLogcat;
            }
        });
    }

    @After
    public void after() {
        if (fakeLogcat != null) {
            if (fakeLogcat.isAlive()) {
                fakeLogcat.destroy();
            }
        }

        if (mockStatic != null) {
            mockStatic.close();
        }
    }

    @Test(timeout = 3000L)
    public void nonOneShot_emits_multiple_log_chunks() {
        CaptureCallback capture = new CaptureCallback();

        try {
            fakeLogcat = new FakeLogcat(10);

            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher("bsk1", capture);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(10);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(fakeLogcat.getGeneratedLines());
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            fakeLogcat = new FakeLogcat(501);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher("bks2", capture);

            watcher.run();

            List<String> generatedLineWithLF = fakeLogcat.getGeneratedLines();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).hasSize(2);
            Truth.assertThat(linesList.get(0)).hasSize(LogcatProcess.MAX_LINES);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(generatedLineWithLF.stream().limit(LogcatProcess.MAX_LINES).collect(Collectors.toList()));
            Truth.assertThat(linesList.get(1)).hasSize(1);
            Truth.assertThat(linesList.get(1)).containsExactlyElementsIn(generatedLineWithLF.stream().skip(LogcatProcess.MAX_LINES).collect(Collectors.toList()));
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            fakeLogcat = new FakeLogcat(1000);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher("bks3", capture);

            watcher.run();

            List<String> generatedLineWithLF = fakeLogcat.getGeneratedLines();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).hasSize(2);
            Truth.assertThat(linesList.get(0)).hasSize(LogcatProcess.MAX_LINES);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(generatedLineWithLF.subList(0, LogcatProcess.MAX_LINES));
            Truth.assertThat(linesList.get(1)).hasSize(LogcatProcess.MAX_LINES);
            Truth.assertThat(linesList.get(1)).containsExactlyElementsIn(generatedLineWithLF.subList(LogcatProcess.MAX_LINES, generatedLineWithLF.size()));
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }
    }

    @Test(timeout = 3000L)
    public void OneShot_emits_single_log_chunk() {
        CaptureCallback capture = new CaptureCallback();

        try {
            fakeLogcat = new FakeLogcat(10);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(null, capture);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(10);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(fakeLogcat.getGeneratedLines());
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            fakeLogcat = new FakeLogcat(501);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(null, capture);

            watcher.run();

            List<String> generatedLines = fakeLogcat.getGeneratedLines();
            List<String> generatedLineWithLF = generatedLines.subList(generatedLines.size() - LogcatProcess.MAX_LINES, generatedLines.size());

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(LogcatProcess.MAX_LINES);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(generatedLineWithLF);
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            fakeLogcat = new FakeLogcat(1000);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(null, capture);

            watcher.run();

            List<String> generatedLines = fakeLogcat.getGeneratedLines();
            List<String> generatedLineWithLF = generatedLines.subList(generatedLines.size() - LogcatProcess.MAX_LINES, generatedLines.size());

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(LogcatProcess.MAX_LINES);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(generatedLineWithLF);
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }
    }

    @Test(timeout = 3000L)
    public void nonOneShot_interrupt_stops_later_emits() {
        CaptureCallback capture = new CaptureCallback();

        try {
            // call the method for the finished process

            fakeLogcat = new FakeLogcat(10);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher("bks1", capture);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(10);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(fakeLogcat.getGeneratedLines());

            watcher.interrupt(); // do not throw any error
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            // interrupt the on-going process that read lines less than MAX_LINES

            fakeLogcat = new FakeLogcat(20, 10);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher("bks2", capture);

            destroyWorkerAfter(watcher, 300, TimeUnit.MILLISECONDS);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).isEmpty();
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            // interrupt the on-going process that read many lines more than MAX_LINES

            fakeLogcat = new FakeLogcat(550, 549);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher("bks3", capture);

            destroyWorkerAfter(watcher, 500, TimeUnit.MILLISECONDS);

            watcher.run();

            List<String> generatedLines = fakeLogcat.getGeneratedLines();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            // emit the first chunk but second chunk
            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(LogcatProcess.MAX_LINES);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(generatedLines.subList(0, LogcatProcess.MAX_LINES));
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }
    }

    @Test(timeout = 3000L)
    public void OneShot_interrupt_stops_emitting_logs() {
        CaptureCallback capture = new CaptureCallback();

        try {
            // call the method for the finished process

            fakeLogcat = new FakeLogcat(10);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(null, capture);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(10);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(fakeLogcat.getGeneratedLines());

            watcher.interrupt(); // do not throw any error
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            // interrupt the on-going process that read lines less than MAX_LINES

            fakeLogcat = new FakeLogcat(20, 10);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(null, capture);

            destroyWorkerAfter(watcher, 300, TimeUnit.MILLISECONDS);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            Truth.assertThat(linesList).isEmpty();
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            // interrupt the on-going process that read many lines more than MAX_LINES

            fakeLogcat = new FakeLogcat(550, 549);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(null, capture);

            destroyWorkerAfter(watcher, 300, TimeUnit.MILLISECONDS);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getProcessId());

            // no chunk should be emitted
            Truth.assertThat(linesList).isEmpty();
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }
    }

    private void destroyWorkerAfter(
            final LogcatProcess.LogcatWatcher watcher,
            final long duration,
            final TimeUnit unit
    ) {
        Thread anotherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(unit.toMillis(duration));
                } catch (InterruptedException e) {
                }
                watcher.interrupt();
            }
        });

        anotherThread.start();
    }

    private static class CaptureCallback implements LogcatProcess.Callback {
        private final Map<String, List<List<String>>> captured = new HashMap<>();
        private final Map<String, Boolean> finished = new HashMap<>();

        @Override
        public void onStarted(String processId) {
            if (captured.containsKey(processId)) {
                throw new IllegalStateException("only unique process id is allowed");
            }

            captured.put(processId, new ArrayList<List<String>>());
        }

        @Override
        public void emit(
                String processId,
                ArrayList<String> logcatLines
        ) {
            captured.get(processId).add(logcatLines);
        }

        @Override
        public void onFinished(String processId) {
            captured.put(processId, Collections.unmodifiableList(captured.get(processId)));
            finished.put(processId, true);
        }
    }
}
