package com.deploygate.sdk;

import android.os.Build;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.sdk.internal.annotations.Experimental;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class LogcatProcess {
    interface Callback {
        void onStarted(String processId);

        void emit(
            String processId,
            ArrayList<String> logcatLines,
            @Nullable String captureId
        );

        void onFinished(String processId);
    }

    static final String UNKNOWN_PROCESS_ID = "UNKNOWN";

    private static final int BUFFER_SIZE = 8192;
    // FIXME this should be flexible cuz the transaction needs to be reduced if exceeded.
    private static final Object LOCK = new Object();

    static final int MAX_LINES = 500; // plus 1 due to the legacy behavior

    private final LogcatProcess.Callback callback;
    private final ExecutorService executorService;
    private LogcatWatcher latestLogcatWatcher;

    LogcatProcess(
            LogcatProcess.Callback callback
    ) {
        this.callback = callback;
        this.executorService = Executors.newFixedThreadPool(1);
    }

    /**
     * @param streamSessionKey
     *
     * @return a pair of watcher ids (non-nulls). first is the previous watcher id, second is the new watcher id.
     */
    Pair<String, String> execute(
        @Experimental String streamSessionKey,
        @Nullable String captureId
    ) {
        Pair<String, String> ids;

        synchronized (LOCK) {
            final LogcatWatcher currentWatcher = latestLogcatWatcher;
            final String currentPid;

            if (currentWatcher != null) {
                currentPid = currentWatcher.processId;
            } else {
                currentPid = UNKNOWN_PROCESS_ID;
            }

            if (currentWatcher != null && currentWatcher.isAlive()) {
                // prioritize the ongoing process so never interrupt it.
                return Pair.create(currentPid, currentPid);
            }

            final LogcatWatcher newWatcher = new LogcatWatcher(streamSessionKey, captureId, callback);

            try {
                this.latestLogcatWatcher = newWatcher;
                this.executorService.submit(newWatcher);
                ids = Pair.create(currentPid, newWatcher.processId);
            } catch (RejectedExecutionException th) {
                Logger.e(th, "cannot schedule the logcat worker");
                this.latestLogcatWatcher = currentWatcher;
                ids = Pair.create(currentPid, currentPid);
            }
        }

        if (ids == null || ids.first == null || ids.second == null) {
            throw new IllegalStateException("execution ids must not be null");
        }

        return ids;
    }

    /**
     * Cancel the on-going watcher.
     *
     * @return canceled watch id. {@link LogcatProcess#UNKNOWN_PROCESS_ID}
     */
    String stop() {
        synchronized (LOCK) {
            if (latestLogcatWatcher == null || !latestLogcatWatcher.isAlive()) {
                return UNKNOWN_PROCESS_ID;
            }

            latestLogcatWatcher.interrupt();
            return latestLogcatWatcher.processId;
        }
    }

    static class LogcatWatcher implements Runnable {
        private static final int STATE_READY = 0;
        private static final int STATE_RUNNING = 1;
        private static final int STATE_INTERRUPTED = 2;
        private static final int STATE_FINISHED = 3;

        private final String processId;
        private final boolean isOneShot;
        @Nullable private final String captureId;
        private final WeakReference<Callback> callback;
        private final AtomicReference<Process> processRef;
        private final AtomicInteger state;

        LogcatWatcher(
            @Experimental String streamSessionKey,
            @Nullable String captureId,
            Callback callback
        ) {
            this.processId = streamSessionKey != null ? streamSessionKey : ClientId.generate();
            this.isOneShot = streamSessionKey == null;
            this.captureId = captureId;
            this.callback = new WeakReference<>(callback);
            this.processRef = new AtomicReference<>();
            this.state = new AtomicInteger(STATE_READY);
        }

        synchronized boolean isAlive() {
            int state = this.state.get();

            return state == STATE_READY || state == STATE_RUNNING;
        }

        /**
         * Interrupt the process of Logcat but never interrupt the thread itself.
         */
        void interrupt() {
            if (!(state.compareAndSet(STATE_READY, STATE_INTERRUPTED) || state.compareAndSet(STATE_RUNNING, STATE_INTERRUPTED))) {
                return;
            }

            Process process = processRef.getAndSet(null);

            if (process != null) {
                try {
                    process.destroy();
                } catch (Throwable th) {
                    Logger.e(th, "an unexpected error happened when destroying the process");
                }
            }
        }

        /**
         * Only for testing
         *
         * @return
         */
        String getProcessId() {
            return processId;
        }

        @Override
        public void run() {
            if (!state.compareAndSet(STATE_READY, STATE_RUNNING)) {
                Logger.w("Logcat stream is not ready to execute");
                return;
            }

            BufferedReader bufferedReader = null;

            try {
                {
                    Callback callback = this.callback.get();

                    if (callback != null) {
                        callback.onStarted(processId);
                    } else {
                        return;
                    }
                }

                Collection<String> logcatBuf = createBuffer(MAX_LINES);

                Process process = execLogcatCommand(isOneShot);
                processRef.set(process);
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), BUFFER_SIZE);

                Logger.d("Start retrieving logcat");
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Callback callback = this.callback.get();

                    if (callback == null) {
                        return;
                    }

                    if (isOneShot) {
                        if (logcatBuf.size() >= MAX_LINES) {
                            if (logcatBuf instanceof Deque) {
                                ((Deque<String>) logcatBuf).removeFirst();
                            } else if (logcatBuf instanceof List) {
                                ((List<String>) logcatBuf).remove(0);
                            } else {
                                throw new IllegalStateException("non-allowed collection class");
                            }
                        }
                    }

                    logcatBuf.add(line + "\n");

                    // check before emitting
                    if (state.get() != STATE_RUNNING) {
                        Logger.w("Logcat stream is interrupted");
                        return;
                    }

                    if (isOneShot) {
                        continue;
                    } else if (logcatBuf.size() >= MAX_LINES) {
                        callback.emit(processId, toArrayList(logcatBuf), captureId);
                        logcatBuf = createBuffer(MAX_LINES); // Don't reuse to make sure releasing the reference
                    } else if (!bufferedReader.ready()) {
                        callback.emit(processId, toArrayList(logcatBuf), captureId);
                        logcatBuf = createBuffer(MAX_LINES); // Don't reuse to make sure releasing the reference
                    } else {
                        continue;
                    }

                    // check after emitting
                    if (state.get() != STATE_RUNNING) {
                        Logger.w("Logcat stream is interrupted");
                        return;
                    }
                }

                if (!logcatBuf.isEmpty()) {
                    Callback callback = this.callback.get();

                    if (callback != null) {
                        callback.emit(processId, toArrayList(logcatBuf), captureId);
                    }
                }

                // EOF
            } catch (IOException e) {
                Logger.d("Logcat stopped: %s", e.getMessage());
            } catch (SecurityException e) {
                // FIXME notify this to the parent
                Logger.e("Subprocess is unavailable: %s", e.getMessage());
            } finally {
                if (!state.compareAndSet(STATE_RUNNING, STATE_FINISHED)) {
                    Logger.w("this process has already been interrupted");
                }

                Process process = processRef.getAndSet(null);

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        // ignored
                    }
                }
                if (process != null) {
                    try {
                        process.destroy();
                    } catch (Throwable th) {
                        Logger.e(th, "an unexpected error happened when destroying the process");
                    }
                }

                Callback callback = this.callback.get();

                if (callback != null) {
                    callback.onFinished(processId);
                }
            }
        }

        /**
         * ArrayList is the best unless isOneShot. Otherwise, ArrayDeque is the best.
         * - add(String) should be O(1)
         * - remove(0) should be O(1) if isOneShot is true
         * - toArray should be O(N) to transform itself to ArrayList
         *
         * @param size
         *         the capacity of the buffer (the actual collection class may adjust the cap based on this parameter)
         *
         * @return a buffer pool
         */
        private Collection<String> createBuffer(int size) {
            return isOneShot ? new ArrayDeque<String>(size) : new ArrayList<String>(size);
        }

        static ArrayList<String> toArrayList(Collection<String> collection) {
            return collection instanceof ArrayList ? (ArrayList<String>) collection : new ArrayList<>(collection);
        }

        static Process execLogcatCommand(boolean isOneShot) throws IOException {
            return Runtime.getRuntime().exec(buildCommands(isOneShot));
        }

        /**
         * @param isOneShot
         *         true if oneshot logcat is requested
         *
         * @return an array for command-exec
         */
        private static String[] buildCommands(boolean isOneShot) {
            List<String> commandLine = new ArrayList<>();
            commandLine.add("logcat");

            int MAX_LINES = 500;
            if (isOneShot) {
                commandLine.add("-d");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                    commandLine.add("-t");
                    commandLine.add(String.valueOf(MAX_LINES));
                }
            }
            commandLine.add("-v");
            commandLine.add("threadtime");
            commandLine.add("*:V");

            return commandLine.toArray(new String[0]);
        }
    }
}


