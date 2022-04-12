package com.deploygate.sdk;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.text.TextUtils;
import android.util.Pair;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.IDeployGateSdkService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class LogcatInstructionSerializer {
    static final int MAX_RETRY_COUNT = 2;
    static final int MAX_CHUNK_CHALLENGE_COUNT = 2;
    static final int SEND_LOGCAT_RESULT_SUCCESS = 0;
    static final int SEND_LOGCAT_RESULT_FAILURE_RETRIABLE = -1;
    static final int SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE = -2;
    static final int SEND_LOGCAT_RESULT_FAILURE_RETRY_EXCEEDED = -3;

    static final int WHAT_SEND_LOGCAT = 0x20;
    static final int WHAT_ADD_LOGCAT_CHUNK = 0x30;

    private static final Object LOCK = new Object();

    private final String packageName;
    private final LogcatProcess logcatProcess;

    @SuppressWarnings("FieldCanBeLocal")
    private final HandlerThread thread;
    private LogcatHandler handler;
    private boolean isDisabled;

    private volatile IDeployGateSdkService service;

    LogcatInstructionSerializer(
            String packageName
    ) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName must be present");
        }

        this.packageName = packageName;
        this.logcatProcess = new LogcatProcess(new LogcatProcess.Callback() {
            @Override
            public void emit(
                    String watchId,
                    ArrayList<String> logcatLines
            ) {
                ensureHandlerInitialized();

                handler.enqueueSendLogcatMessageInstruction(new SendLogcatRequest(watchId, logcatLines));
            }
        });
        this.isDisabled = false;

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

        this.service = service;
    }

    /**
     * Release a service connection and cancel all pending instructions and on-going instruction.
     */
    public final void disconnect() {
        ensureHandlerInitialized();

        cancel();
        this.service = null;
    }

    public final synchronized void requestSendingLogcat(
            boolean isOneShot
    ) {
        if (isDisabled) {
            return;
        }

        Pair<String, String> ids = logcatProcess.execute(isOneShot);

        String retiredId = ids.first;

        if (LogcatProcess.UNKNOWN_WATCHER_ID.equals(retiredId)) {
            ensureHandlerInitialized();
            handler.cancelPendingSendLogcatInstruction(retiredId);
        }
    }

    public final void setDisabled(boolean disabled) {
        isDisabled = disabled;

        if (disabled) {
            Logger.d("Disabled logcat instruction serializer");
        } else {
            Logger.d("Enabled logcat instruction serializer");
        }
    }

    public final void cancel() {
        ensureHandlerInitialized();
        logcatProcess.stop();
        handler.cancelPendingSendLogcatInstruction();
    }

    /**
     * Check if this serializer has a service connection.
     * <p>
     * The connection may return {@link android.os.DeadObjectException} even if this returns true;
     *
     * @return true if a service connection is assigned, otherwise false.
     */
    public final boolean hasServiceConnection() {
        return service != null;
    }

    /**
     * Send a set of lines to the receiver with a simple retry strategy.
     * <p>
     * Visible only for testing
     *
     * @param request
     *         a request that is same with a single chunk
     *
     * @return Return {@link LogcatInstructionSerializer#SEND_LOGCAT_RESULT_SUCCESS} if this can transmit the log cat,
     * or return {@link LogcatInstructionSerializer#SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE} if smaller transaction may pass,
     * or return {@link LogcatInstructionSerializer#SEND_LOGCAT_RESULT_FAILURE_RETRIABLE} if a simply retry may pass,
     * otherwise {@link LogcatInstructionSerializer#SEND_LOGCAT_RESULT_FAILURE_RETRY_EXCEEDED}
     */
    int sendSingleChunk(
            SendLogcatRequest request
    ) {
        IDeployGateSdkService service = this.service;

        if (service == null) {
            return SEND_LOGCAT_RESULT_FAILURE_RETRIABLE;
        }

        Bundle extras = request.toExtras();

        try {
            service.sendEvent(packageName, DeployGateEvent.ACTION_SEND_LOGCAT, extras);
            return SEND_LOGCAT_RESULT_SUCCESS;
        } catch (RemoteException e) {
            int currentAttempts = request.getAndIncrementRetryCount();

            if (currentAttempts >= MAX_RETRY_COUNT) {
                Logger.e("failed to send custom log and exceeded the max retry count: %s", e.getMessage());
                return SEND_LOGCAT_RESULT_FAILURE_RETRY_EXCEEDED;
            } else {
                Logger.w("failed to send custom log %d times: %s", currentAttempts + 1, e.getMessage());
            }

            if (Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 <= Build.VERSION.SDK_INT) {
                if (e instanceof TransactionTooLargeException) {
                    return SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE;
                }
            }

            return SEND_LOGCAT_RESULT_FAILURE_RETRIABLE;
        }
    }

    /**
     * @return
     *
     * @hide Only for testing.
     */
    Looper getLooper() {
        return getHandler().getLooper();
    }

    /**
     * @return
     *
     * @hide Only for testing.
     */
    Handler getHandler() {
        ensureHandlerInitialized();
        return handler;
    }

    /**
     * Only for testing
     */
    void halt() {
        cancel();
        thread.interrupt();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            thread.quitSafely();
        } else {
            thread.quit();
        }
    }

    boolean hasHandlerInitialized() {
        return handler != null;
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

    /**
     * Send a set or multiple smaller sets of lines in order to the receiver.
     *
     * @param splitTimes
     *         number of times that one transaction has been split.
     *
     * @see LogcatInstructionSerializer#sendSingleChunk(SendLogcatRequest)
     */
    private int sendChunkedLogcats(
            int splitTimes,
            LinkedList<SendLogcatRequest> pendingRequests
    ) {
        SendLogcatRequest request = pendingRequests.removeFirst();

        int result = sendSingleChunk(request);

        if (result == SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE) {
            if (splitTimes >= MAX_CHUNK_CHALLENGE_COUNT) {
                // stop splitting this request any more.
                pendingRequests.addFirst(request); // revert the request
                return SEND_LOGCAT_RESULT_FAILURE_RETRIABLE;
            }

            pendingRequests.addAll(0, request.splitInto(2));

            return sendChunkedLogcats(splitTimes + 1, pendingRequests);
        }

        return result;
    }

    private static class LogcatHandler extends Handler {
        private final LogcatInstructionSerializer transmitter;
        /**
         * Handler(MessageQueue) uses == to check the equality of msg.what, so we need to preserve the raw msg.what
         */
        private final Map<String, LinkedList<SendLogcatRequest>> requestMap;

        LogcatHandler(
                Looper looper,
                LogcatInstructionSerializer transmitter
        ) {
            super(looper);
            this.transmitter = transmitter;
            this.requestMap = new HashMap<>();
        }

        /**
         * Cancel the send-logcat instruction of all watchers in the handler message queue.
         *
         * @return true if canceled, otherwise false.
         */
        void cancelPendingSendLogcatInstruction() {
            synchronized (requestMap) {
                requestMap.clear();
            }

            removeMessages(WHAT_SEND_LOGCAT);
            removeMessages(WHAT_ADD_LOGCAT_CHUNK);
        }

        /**
         * Cancel the send-logcat instruction of the specific watcher in the handler message queue.
         *
         * @return true if canceled, otherwise false.
         */
        void cancelPendingSendLogcatInstruction(String watchId) {
            acquireRequests(watchId);
            removeMessages(WHAT_SEND_LOGCAT, watchId);
        }

        /**
         * Enqueue the send-logcat instruction in the handler message queue unless enqueued.
         */
        void enqueueSendLogcatMessageInstruction(
                SendLogcatRequest request
        ) {
            synchronized (requestMap) {
                if (!requestMap.containsKey(request.watchId)) {
                    requestMap.put(request.watchId, new LinkedList<>());
                }
            }

            sendMessage(obtainMessage(WHAT_ADD_LOGCAT_CHUNK, request));
        }

        void sendAllInBuffer(String watchId) {
            LinkedList<SendLogcatRequest> pendingRequests = acquireRequests(watchId);

            if (pendingRequests == null) {
                return;
            }

            boolean retry = false;

            while (!pendingRequests.isEmpty()) {
                if (transmitter.sendChunkedLogcats(0, pendingRequests) == SEND_LOGCAT_RESULT_FAILURE_RETRIABLE) {
                    retry = true;
                    break;
                }
            }

            if (retry) {
                synchronized (requestMap) {
                    LinkedList<SendLogcatRequest> newlyPendingRequests = requestMap.remove(watchId);

                    if (newlyPendingRequests != null) {
                        pendingRequests.addAll(newlyPendingRequests);
                    }

                    requestMap.put(watchId, pendingRequests);
                }

                try {
                    removeMessages(WHAT_SEND_LOGCAT, watchId);
                    Message msg = obtainMessage(WHAT_SEND_LOGCAT, watchId);
                    // Put the retry message at front of the queue because delay or enqueuing a message may cause unexpected overflow of the buffer.
                    sendMessageAtFrontOfQueue(msg);
                    Thread.sleep(600); // experimental value
                } catch (InterruptedException ignore) {
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_ADD_LOGCAT_CHUNK: {
                    SendLogcatRequest request = (SendLogcatRequest) msg.obj;

                    if (appendRequest(request)) {
                        if (transmitter.hasServiceConnection()) {
                            sendAllInBuffer(request.watchId);
                        }
                    }

                    break;
                }
                case WHAT_SEND_LOGCAT: {
                    String watchId = (String) msg.obj;
                    sendAllInBuffer(watchId);

                    break;
                }
            }
        }

        private boolean appendRequest(SendLogcatRequest request) {
            synchronized (requestMap) {
                LinkedList<SendLogcatRequest> requests = requestMap.get(request.watchId);

                if (requests == null) {
                    return false;
                }

                requests.add(request);
            }

            return true;
        }

        private LinkedList<SendLogcatRequest> acquireRequests(String watchId) {
            synchronized (requestMap) {
                return requestMap.remove(watchId);
            }
        }
    }
}
