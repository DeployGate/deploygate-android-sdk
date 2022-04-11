package com.deploygate.sdk;

import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.text.TextUtils;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.IDeployGateSdkService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class LogcatInstructionSerializer {
    static final int MAX_RETRY_COUNT = 2;
    static final int MAX_CHUNK_CHALLENGE_COUNT = 2;
    static final int SEND_LOGCAT_RESULT_SUCCESS = 0;
    static final int SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE = -1;
    static final int SEND_LOGCAT_RESULT_FAILURE_ANYWAY = -2;

    private static final Object LOCK = new Object();
    private static final int BUFFER_SIZE = 8192;
    // FIXME this should be flexible cuz the transaction needs to be reduced if exceeded.
    private static final int MAX_LINES = 500;

    private final String packageName;

    @SuppressWarnings("FieldCanBeLocal")
    private final HandlerThread thread;
    private LogcatHandler handler;
    private boolean isDisabledTransmission;

    private volatile IDeployGateSdkService service;

    LogcatInstructionSerializer(
            String packageName
    ) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName must be present");
        }

        this.packageName = packageName;
        this.isDisabledTransmission = false;

        this.thread = new HandlerThread("deploygate-sdk-logcat");
        this.thread.start();
    }

    /**
     * Bind a service and trigger several instructions immediately.
     *
     * @param service
     *         the latest service connection
     */
    public final synchronized void connect(IDeployGateSdkService service) {
        if (service == null) {
            throw new IllegalArgumentException("service must not be null");
        }

        ensureHandlerInitialized();

        Boolean isOneShot = handler.cancelPendingSendLogcatInstruction();

        this.service = service;

        if (isOneShot == null) {
            return;
        }

        handler.enqueueSendLogcatInstruction(isOneShot);
    }

    /**
     * Release a service connection and cancel all pending instructions.
     */
    public final void disconnect() {
        ensureHandlerInitialized();

        handler.cancelPendingSendLogcatInstruction();
        this.service = null;
    }

    public final synchronized void request(
            boolean isOneShot
    ) {
        if (isDisabledTransmission) {
            return;
        }

        ensureHandlerInitialized();
        handler.enqueueSendLogcatInstruction(isOneShot);
    }

    /**
     * Disable transmissions.
     *
     * @param disabledTransmission
     *         specify true if wanna disable the transmitter, otherwise false.
     */
    public final void setDisabledTransmission(boolean disabledTransmission) {
        isDisabledTransmission = disabledTransmission;

        if (disabledTransmission) {
            Logger.d("Disabled custom log transmitter");
        } else {
            Logger.d("Enabled custom log transmitter");
        }
    }

    /**
     * Check if this transmitter has a service connection.
     * <p>
     * The connection may return {@link android.os.DeadObjectException} even if this returns true;
     *
     * @return true if a service connection is assigned, otherwise false.
     */
    public final boolean hasServiceConnection() {
        return service != null;
    }

    /**
     * @return
     *
     * @hide Only for testing.
     */
    Looper getLooper() {
        return handler.getLooper();
    }

    /**
     * Send a set of lines to the receiver.
     * <p>
     * Visible only for testing
     *
     * @param lines
     *         logcat lines to send
     *
     * @return Return {@link LogcatInstructionSerializer#SEND_LOGCAT_RESULT_SUCCESS} if this can transmit the log cat,
     * or return {@link LogcatInstructionSerializer#SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE} if smaller transaction may pass,
     * otherwise {@link LogcatInstructionSerializer#SEND_LOGCAT_RESULT_FAILURE_ANYWAY}
     */
    int sendLogcat(ArrayList<String> lines) {
        IDeployGateSdkService service = this.service;

        if (service == null) {
            // never support pending requests
            return SEND_LOGCAT_RESULT_FAILURE_ANYWAY;
        }

        Bundle extras = new Bundle();
        extras.putStringArrayList(DeployGateEvent.EXTRA_LOG, lines);

        int retryCount = 0;

        do {
            try {
                service.sendEvent(packageName, DeployGateEvent.ACTION_SEND_LOGCAT, extras);
                return SEND_LOGCAT_RESULT_SUCCESS;
            } catch (DeadObjectException e) {
                return SEND_LOGCAT_RESULT_FAILURE_ANYWAY;
            } catch (RemoteException e) {
                if (Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 <= Build.VERSION.SDK_INT) {
                    if (e instanceof TransactionTooLargeException) {
                        return SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE;
                    }
                }
            }
        } while (retryCount++ < MAX_RETRY_COUNT);

        return SEND_LOGCAT_RESULT_FAILURE_ANYWAY;
    }

    private void ensureHandlerInitialized() {
        if (handler != null) {
            return;
        }

        synchronized (LOCK) {
            if (handler != null) {
                return;
            }

            handler = new LogcatHandler(thread.getLooper(), this);
        }
    }

    private static class LogcatHandler extends Handler {
        private static final int WHAT_SEND_LOGCAT = 0x20;

        private final LogcatInstructionSerializer transmitter;

        LogcatHandler(
                Looper looper,
                LogcatInstructionSerializer transmitter
        ) {
            super(looper);
            this.transmitter = transmitter;
        }

        /**
         * Cancel the send-logcat instruction in the handler message queue.
         * This doesn't interrupt the thread and stop the sending-logcat instruction that is running at the time.
         *
         * @return nullable. null means no request has been canceled. non-null value is same to isOneShot parameter of the cancled request, so it doesn't mean if cancel succeed or not.
         */
        Boolean cancelPendingSendLogcatInstruction() {
            if (hasMessages(WHAT_SEND_LOGCAT, true)) {
                removeMessages(WHAT_SEND_LOGCAT);
                return true;
            } else if (hasMessages(WHAT_SEND_LOGCAT, false)) {
                removeMessages(WHAT_SEND_LOGCAT);
                return false;
            }

            // dirty implementation :shrug:
            return null;
        }

        /**
         * Enqueue the send-logcat instruction in the handler message queue unless enqueued.
         *
         * @param isOneShot
         *         specify true to send oneshot LogCat, otherwise false.
         */
        void enqueueSendLogcatInstruction(boolean isOneShot) {
            removeMessages(WHAT_SEND_LOGCAT);
            sendMessage(obtainMessage(WHAT_SEND_LOGCAT, isOneShot));
        }

        void getAndSendLogcat(boolean isOneShot) {
            Process process = null;
            BufferedReader bufferedReader = null;

            try {
                // ArrayList is the best unless isOneShot. Otherwise, ArrayDeque is the best.
                //
                // - add(String) should be O(1)
                // - remove(0) should be O(1) if isOneShot is true
                // - toArray should be O(N) to transform itself to ArrayList
                Collection<String> logcatBuf = isOneShot ? new ArrayDeque<>(MAX_LINES) : new ArrayList<>(MAX_LINES);

                process = Runtime.getRuntime().exec(buildCommands(isOneShot));
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), BUFFER_SIZE);

                Logger.d("Start retrieving logcat");
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    logcatBuf.add(line + "\n");
                    if (isOneShot) {
                        if (logcatBuf.size() > MAX_LINES) {
                            ((ArrayDeque<String>) logcatBuf).removeFirst();
                        }
                    } else {
                        if (!bufferedReader.ready()) {
                            if (sendLogcat(0, logcatBuf) == SEND_LOGCAT_RESULT_SUCCESS) {
                                logcatBuf.clear();
                            } else {
                                return;
                            }
                        }
                    }
                }

                if (!logcatBuf.isEmpty()) {
                    sendLogcat(0, logcatBuf);
                }

                // EOF
            } catch (IOException e) {
                Logger.d("Logcat stopped: %s", e.getMessage());
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        // ignored
                    }
                }
                try {
                    if (process != null) {
                        process.destroy();
                    }
                } catch (Throwable th) {
                    Logger.e(th, "failed to stop the logcat process");
                }
            }
        }

        /**
         * @param splitCount
         *         number of times that one transaction has been split.
         * @param lines
         *         logcat lines to send
         *
         * @return return {@link LogcatInstructionSerializer#SEND_LOGCAT_RESULT_SUCCESS} if succeeded, otherwise {@link LogcatInstructionSerializer#SEND_LOGCAT_RESULT_FAILURE_ANYWAY}
         */
        private int sendLogcat(
                int splitCount,
                Collection<String> lines
        ) {
            ArrayList<String> serializee = lines instanceof ArrayList ? (ArrayList<String>) lines : new ArrayList<>(lines);

            int result = transmitter.sendLogcat(serializee);

            if (result != SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE) {
                return result;
            }

            if (splitCount >= MAX_CHUNK_CHALLENGE_COUNT) {
                // terminate this request due to too many attempts.
                return SEND_LOGCAT_RESULT_FAILURE_ANYWAY;
            }

            int partitionIndex = serializee.size();

            if (sendLogcat(splitCount + 1, serializee.subList(0, partitionIndex)) != SEND_LOGCAT_RESULT_SUCCESS) {
                return SEND_LOGCAT_RESULT_FAILURE_ANYWAY;
            }

            return sendLogcat(splitCount + 1, serializee.subList(partitionIndex, serializee.size()));
        }

        /**
         * @param isOneShot
         *         true if oneshot logcat is requested
         *
         * @return an array for command-exec
         */
        private String[] buildCommands(boolean isOneShot) {
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

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SEND_LOGCAT: {
                    getAndSendLogcat((Boolean) msg.obj);

                    break;
                }
            }
        }
    }
}
