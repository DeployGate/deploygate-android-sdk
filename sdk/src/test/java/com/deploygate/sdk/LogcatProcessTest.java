package com.deploygate.sdk;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.sdk.helper.FakeLogcat;
import com.google.common.truth.Truth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(AndroidJUnit4.class)
public class LogcatProcessTest {
    private FakeLogcat fakeLogcat;

    @Before
    public void before() {
        LogcatProcess.sLogcatProcessFactory = () -> fakeLogcat;
    }

    @After
    public void after() {
        if (fakeLogcat != null) {
            if (fakeLogcat.isAlive()) {
                fakeLogcat.destroy();
            }
        }
    }

    @Test(timeout = 3000L)
    public void nonOneShot_emits_multiple_log_chunks() {
        CaptureCallback capture = new CaptureCallback();

        try {
            fakeLogcat = new FakeLogcat(10);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(false, capture);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(10);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(fakeLogcat.getGeneratedLines().stream().map(s -> s + "\n").collect(Collectors.toList()));
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            fakeLogcat = new FakeLogcat(501);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(false, capture);

            watcher.run();

            List<String> generatedLineWithLF = fakeLogcat.getGeneratedLines().stream().map(s -> s + "\n").collect(Collectors.toList());

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

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
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(false, capture);

            watcher.run();

            List<String> generatedLineWithLF = fakeLogcat.getGeneratedLines().stream().map(s -> s + "\n").collect(Collectors.toList());

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

            Truth.assertThat(linesList).hasSize(2);
            Truth.assertThat(linesList.get(0)).hasSize(LogcatProcess.MAX_LINES);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(generatedLineWithLF.stream().limit(LogcatProcess.MAX_LINES).collect(Collectors.toList()));
            Truth.assertThat(linesList.get(1)).hasSize(LogcatProcess.MAX_LINES);
            Truth.assertThat(linesList.get(1)).containsExactlyElementsIn(generatedLineWithLF.stream().skip(LogcatProcess.MAX_LINES).limit(LogcatProcess.MAX_LINES).collect(Collectors.toList()));
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
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(true, capture);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(10);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(fakeLogcat.getGeneratedLines().stream().map(s -> s + "\n").collect(Collectors.toList()));
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            fakeLogcat = new FakeLogcat(501);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(true, capture);

            watcher.run();

            List<String> generatedLines = fakeLogcat.getGeneratedLines();
            List<String> generatedLineWithLF = generatedLines.stream().skip(generatedLines.size() - LogcatProcess.MAX_LINES).map(s -> s + "\n").collect(Collectors.toList());

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

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
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(true, capture);

            watcher.run();

            List<String> generatedLines = fakeLogcat.getGeneratedLines();
            List<String> generatedLineWithLF = generatedLines.stream().skip(generatedLines.size() - LogcatProcess.MAX_LINES).map(s -> s + "\n").collect(Collectors.toList());

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

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
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(false, capture);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(10);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(fakeLogcat.getGeneratedLines().stream().map(s -> s + "\n").collect(Collectors.toList()));

            watcher.interrupt(); // do not throw any error
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            // interrupt the on-going process that read lines less than MAX_LINES

            fakeLogcat = new FakeLogcat(20, 10);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(false, capture);

            destroyWorkerAfter(watcher, 300, TimeUnit.MILLISECONDS);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

            Truth.assertThat(linesList).isNull();
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            // interrupt the on-going process that read many lines more than MAX_LINES

            fakeLogcat = new FakeLogcat(550, 549);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(false, capture);

            destroyWorkerAfter(watcher, 500, TimeUnit.MILLISECONDS);

            watcher.run();

            List<String> generatedLines = fakeLogcat.getGeneratedLines();
            List<String> generatedLineWithLF = generatedLines.stream().map(s -> s + "\n").collect(Collectors.toList());

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

            // emit the first chunk but second chunk
            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(LogcatProcess.MAX_LINES);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(generatedLineWithLF.stream().limit(LogcatProcess.MAX_LINES).collect(Collectors.toList()));
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
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(true, capture);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

            Truth.assertThat(linesList).hasSize(1);
            Truth.assertThat(linesList.get(0)).hasSize(10);
            Truth.assertThat(linesList.get(0)).containsExactlyElementsIn(fakeLogcat.getGeneratedLines().stream().map(s -> s + "\n").collect(Collectors.toList()));

            watcher.interrupt(); // do not throw any error
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            // interrupt the on-going process that read lines less than MAX_LINES

            fakeLogcat = new FakeLogcat(20, 10);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(true, capture);

            destroyWorkerAfter(watcher, 300, TimeUnit.MILLISECONDS);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

            Truth.assertThat(linesList).isNull();
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }

        try {
            // interrupt the on-going process that read many lines more than MAX_LINES

            fakeLogcat = new FakeLogcat(550, 549);
            LogcatProcess.LogcatWatcher watcher = new LogcatProcess.LogcatWatcher(true, capture);

            destroyWorkerAfter(watcher, 300, TimeUnit.MILLISECONDS);

            watcher.run();

            List<List<String>> linesList = capture.captured.get(watcher.getWatchId());

            // no chunk should be emitted
            Truth.assertThat(linesList).isNull();
        } finally {
            if (fakeLogcat != null) {
                fakeLogcat.destroy();
            }
        }
    }

    private void destroyWorkerAfter(
            LogcatProcess.LogcatWatcher watcher,
            long duration,
            TimeUnit unit
    ) {
        Thread anotherThread = new Thread(() -> {
            try {
                Thread.sleep(unit.toMillis(duration));
            } catch (InterruptedException e) {
            }
            watcher.interrupt();
        });

        anotherThread.start();
    }

    private static class CaptureCallback implements LogcatProcess.Callback {
        private final Map<String, List<List<String>>> captured = new HashMap<>();

        @Override
        public void emit(
                String watchId,
                ArrayList<String> logcatLines
        ) {
            List<List<String>> linesList = captured.get(watchId);

            if (linesList == null) {
                linesList = new ArrayList<>();
            }

            linesList.add(logcatLines);

            captured.put(watchId, linesList);
        }
    }
}
