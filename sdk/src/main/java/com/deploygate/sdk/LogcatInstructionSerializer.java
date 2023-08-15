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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.IDeployGateSdkService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class LogcatInstructionSerializer implements ILogcatInstructionSerializer {
    static final int MAX_RETRY_COUNT = 2;
    static final int MAX_CHUNK_CHALLENGE_COUNT = 2;
    static final int SEND_LOGCAT_RESULT_SUCCESS = 0;
    static final int SEND_LOGCAT_RESULT_FAILURE_RETRIABLE = -1;
    static final int SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE = -2;
    static final int SEND_LOGCAT_RESULT_FAILURE_RETRY_EXCEEDED = -3;

    static final int WHAT_SEND_LOGCAT = 0x20;
    static final int WHAT_ADD_LOGCAT_CHUNK = 0x30;

    @NonNull
    private static final Object LOCK = new Object();

    @NonNull
    private final String packageName;

    /**
     * nullable if logcat is not supported on this device
     */
    @Nullable
    private final LogcatProcess logcatProcess;

    /**
     * nullable if logcat is not supported on this device
     */
    @SuppressWarnings("FieldCanBeLocal")
    @NonNull
    private final HandlerThread thread;

    /**
     * NonNull if prepared once, however this is always null Logcat is not supported
     */
    @Nullable
    private LogcatHandler handler;
    private boolean isEnabled;

    @Nullable
    private volatile IDeployGateSdkService service;

    LogcatInstructionSerializer(
            @NonNull String packageName
    ) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName must be present");
        }

        this.packageName = packageName;
        this.isEnabled = true; // enabled by default because service availability is unknown yet

        this.logcatProcess = new LogcatProcess(new LogcatProcess.Callback() {
            @Override
            public void onStarted(@NonNull String processId) {
                //noinspection ConstantConditions
                handler.enqueueSendLogcatMessageInstruction(SendLogcatRequest.createBeginning(processId));
            }

            @Override
            public void emit(
                    @NonNull String processId,
                    @NonNull ArrayList<String> logcatLines,
                    @Nullable  String captureId
            ) {
                ensureHandlerPrepared();

                //noinspection ConstantConditions
                handler.enqueueSendLogcatMessageInstruction(new SendLogcatRequest(processId, logcatLines, captureId));
            }

            @Override
            public void onFinished(@NonNull String processId) {
                //noinspection ConstantConditions
                handler.enqueueSendLogcatMessageInstruction(SendLogcatRequest.createTermination(processId));
            }
        });
        this.thread = new HandlerThread("deploygate-sdk-logcat");
        this.thread.start();
    }

    @Override
    public final synchronized void connect(@NonNull IDeployGateSdkService service) {
        //noinspection ConstantConditions
        if (service == null) {
            throw new IllegalArgumentException("service must not be null");
        }

        ensureHandlerPrepared();

        this.service = service;
    }

    @Override
    public final void disconnect() {
        stopStream();
        this.service = null;
    }

    @Override
    public final synchronized boolean requestOneshotLogcat(@Nullable String captureId) {
        return requestLogcat(null, captureId);
    }

    @Override
    public boolean requestStreamedLogcat(@Nullable String sessionKey) {
        if (TextUtils.isEmpty(sessionKey)) {
            Logger.w("non-blank stream key is required");
            return false;
        }

        return requestLogcat(sessionKey);
    }

    @Override
    public final void setEnabled(boolean enabled) {
        isEnabled = enabled;

        if (isEnabled) {
            Logger.d("Enabled logcat instruction serializer");
        } else {
            Logger.d("Disabled logcat instruction serializer");
        }
    }

    @Override
    public final void stopStream() {
        ensureHandlerPrepared();

        if (logcatProcess != null) {
            logcatProcess.stop();
        }
        //noinspection ConstantConditions
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
            @NonNull SendLogcatRequest request
    ) {
        IDeployGateSdkService service = this.service;

        if (service == null) {
            return SEND_LOGCAT_RESULT_FAILURE_RETRIABLE;
        }

        if (!DeployGate.isFeatureSupported(Compatibility.LOGCAT_BUNDLE)) {
            if (request.position != SendLogcatRequest.Position.Content) {
                // skip these requests
                return SEND_LOGCAT_RESULT_SUCCESS;
            }
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

            if (request.position == SendLogcatRequest.Position.Content) {
                if (Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 <= Build.VERSION.SDK_INT) {
                    if (e instanceof TransactionTooLargeException) {
                        if (DeployGate.isFeatureSupported(Compatibility.LOGCAT_BUNDLE)) {
                            return SEND_LOGCAT_RESULT_FAILURE_REQUEST_CHUNK_CHALLENGE;
                        }
                    }
                }
            }

            return SEND_LOGCAT_RESULT_FAILURE_RETRIABLE;
        }
    }

    /**
     * @param streamSessionKey
     *         nullable. sdk can not generate this key.
     *
     * @return true if new process has lauched
     */
    private boolean requestLogcat(@Nullable String streamSessionKey) {
        return requestLogcat(streamSessionKey, null);
    }

    /**
     * @param streamSessionKey nullable. sdk can not generate this key.
     * @param captureId nullable.
     * @return true if new process has lauched
     */
    private boolean requestLogcat(@Nullable String streamSessionKey, @Nullable String captureId) {
        ensureHandlerPrepared();

        if (!isEnabled || logcatProcess == null) {
            return false;
        }

        Pair<String, String> ids = logcatProcess.execute(streamSessionKey, captureId);

        String retiredId = ids.first;
        String newId = ids.second;

        if (retiredId.equals(newId)) {
            // nothing is executed
            return false;
        }

        if (!LogcatProcess.UNKNOWN_PROCESS_ID.equals(retiredId)) {
            // the previous on-going execution has been retied
            //noinspection ConstantConditions
            handler.cancelPendingSendLogcatInstruction(retiredId);
        }

        // check if the new execution has been started
        return !LogcatProcess.UNKNOWN_PROCESS_ID.equals(newId);
    }

    private void ensureHandlerPrepared() {
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
            @NonNull LinkedList<SendLogcatRequest> pendingRequests
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

    /*
     * Only for testing
     */

    /**
     * @return the handler instance
     */
    @NonNull
    Handler getHandler() {
        ensureHandlerPrepared();
        //noinspection ConstantConditions
        return handler;
    }

    /**
     * Halt the process and the thread. Any of methods are not guaranteed after calling this.
     */
    void halt() {
        stopStream();

        thread.interrupt();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            thread.quitSafely();
        } else {
            thread.quit();
        }
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
         */
        void cancelPendingSendLogcatInstruction(String bundleId) {
            acquireRequests(bundleId);
            removeMessages(WHAT_SEND_LOGCAT, bundleId);
        }

        /**
         * Enqueue the send-logcat instruction in the handler message queue unless enqueued.
         */
        void enqueueSendLogcatMessageInstruction(
                SendLogcatRequest request
        ) {
            synchronized (requestMap) {
                if (!requestMap.containsKey(request.gid)) {
                    requestMap.put(request.gid, new LinkedList<SendLogcatRequest>());
                }
            }

            sendMessage(obtainMessage(WHAT_ADD_LOGCAT_CHUNK, request));
        }

        void sendAllInBuffer(String bundleId) {
            LinkedList<SendLogcatRequest> pendingRequests = acquireRequests(bundleId);

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
                    LinkedList<SendLogcatRequest> newlyPendingRequests = requestMap.remove(bundleId);

                    if (newlyPendingRequests != null) {
                        pendingRequests.addAll(newlyPendingRequests);
                    }

                    requestMap.put(bundleId, pendingRequests);
                }

                try {
                    removeMessages(WHAT_SEND_LOGCAT, bundleId);
                    Message msg = obtainMessage(WHAT_SEND_LOGCAT, bundleId);
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
                            sendAllInBuffer(request.gid);
                        }
                    }

                    break;
                }
                case WHAT_SEND_LOGCAT: {
                    String bundleId = (String) msg.obj;
                    sendAllInBuffer(bundleId);

                    break;
                }
            }
        }

        private boolean appendRequest(SendLogcatRequest request) {
            synchronized (requestMap) {
                LinkedList<SendLogcatRequest> requests = requestMap.get(request.gid);

                if (requests == null) {
                    return false;
                }

                requests.add(request);
            }

            return true;
        }

        private LinkedList<SendLogcatRequest> acquireRequests(String bundleId) {
            synchronized (requestMap) {
                return requestMap.remove(bundleId);
            }
        }
    }
}
