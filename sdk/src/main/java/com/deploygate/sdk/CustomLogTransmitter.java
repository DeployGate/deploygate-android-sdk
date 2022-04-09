package com.deploygate.sdk;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.IDeployGateSdkService;

import java.util.LinkedList;

/**
 * This class transmits pending logs to the DeployGate client.
 * The internal handler class creates a buffer pool and assure the sending-order.
 *
 * <p>
 * - Enqueue a new log to the buffer pool
 * - Send logs in the order of FIFO
 *
 * The cost of polling the log buffer pool may be high, so SDK starts sending logs only while a service is active.
 * <p>
 * - When a new log is stored to the buffer
 * -
 */
class CustomLogTransmitter {
    private final String packageName;
    private final CustomLogConfiguration configuration;

    @SuppressWarnings("FieldCanBeLocal")
    private final HandlerThread thread;
    private CustomLogHandler handler;

    private volatile IDeployGateSdkService service;

    CustomLogTransmitter(
            String packageName,
            CustomLogConfiguration configuration
    ) {
        this.packageName = packageName;
        this.configuration = configuration;

        this.thread = new HandlerThread("deploygate-sdk-event-log");
        this.thread.start();
    }

    public final synchronized void connect(IDeployGateSdkService service) {
        ensureHandlerInitialized();

        handler.cancelPendingSendLogsInstruction();
        this.service = service;
        handler.enqueueSendLogsInstruction();
    }

    public final void disconnect() {
        ensureHandlerInitialized();

        handler.cancelPendingSendLogsInstruction();
        this.service = null;
    }

    public final synchronized void transmit(
            String type,
            String body
    ) {
        ensureHandlerInitialized();

        CustomLog log = new CustomLog(type, body);
        handler.enqueueAddNewLogInstruction(log);
    }

    /**
     * @return
     *
     * @hide Only for testing.
     */
    Looper getLooper() {
        return handler.getLooper();
    }

    int getPendingCount() {
        return handler.customLogs.size();
    }

    private boolean sendLog(CustomLog log) {
        IDeployGateSdkService service = this.service;

        if (service == null) {
            return false;
        }

        try {
            Bundle extras = new Bundle();
            extras.putSerializable(DeployGateEvent.EXTRA_LOG, log.body);
            extras.putSerializable(DeployGateEvent.EXTRA_LOG_TYPE, log.type);

            service.sendEvent(packageName, DeployGateEvent.ACTION_SEND_CUSTOM_LOG, extras);
            return true;
        } catch (RemoteException e) {
            Logger.w("failed to send custom log: %s", e.getMessage());
            return false;
        }
    }

    private boolean isConnected() {
        return service != null;
    }

    private void ensureHandlerInitialized() {
        if (handler != null) {
            return;
        }

        synchronized (configuration) {
            if (handler != null) {
                return;
            }

            handler = new CustomLogHandler(thread.getLooper(), this, configuration.backpressure, configuration.bufferSize);
        }
    }

    /**
     * This handler behaves as a ordered-buffer of instructions.
     * <p>
     * The instruction of adding a new log and sending buffered logs are synchronized.
     */
    private static class CustomLogHandler extends Handler {
        private static final int WHAT_SEND_LOGS = 0x30;
        private static final int WHAT_ADD_NEW_LOG = 0x100;

        private final CustomLogTransmitter transmitter;
        private final CustomLogConfiguration.Backpressure backpressure;
        private final int bufferSize;
        private final int maxWhatOffset;
        private final LinkedList<CustomLog> customLogs;
        private int pushWhatOffset = 0;

        /**
         * @param looper
         *         Do not use Main Looper to avoid wasting the main thread resource.
         * @param transmitter
         *         an instance to send logs
         * @param backpressure
         *         the backpressure strategy of the log buffer, not of instructions.
         * @param bufferSize
         *         the max size of the log buffer, not of instructions.
         */
        CustomLogHandler(
                Looper looper,
                CustomLogTransmitter transmitter,
                CustomLogConfiguration.Backpressure backpressure,
                int bufferSize
        ) {
            super(looper);
            this.transmitter = transmitter;
            this.backpressure = backpressure;
            this.bufferSize = bufferSize;
            this.maxWhatOffset = bufferSize * 2;
            this.customLogs = new LinkedList<>();
        }

        /**
         * Cancel the send-logs instruction in the handler message queue.
         * This doesn't interrupt the thread and stop the sending-logs instruction that is running at the time.
         */
        void cancelPendingSendLogsInstruction() {
            removeMessages(WHAT_SEND_LOGS);
        }

        /**
         * Enqueue the send-logs instruction in the handler message queue unless enqueued.
         */
        void enqueueSendLogsInstruction() {
            if (hasMessages(WHAT_SEND_LOGS)) {
                return;
            }

            sendEmptyMessage(WHAT_SEND_LOGS);
        }

        /**
         * Enqueue new add-new-log instruction to the handler message queue.
         */
        void enqueueAddNewLogInstruction(CustomLog log) {
            Message msg = obtainMessage(WHAT_ADD_NEW_LOG + getAndIncrementPushWhatOffset(), log);
            sendMessage(msg);
        }

        /**
         * Append the new log to the log buffer if the backpressure is not dropping LATEST.
         *
         * @param log
         *         a new log
         */
        void addLogToLast(CustomLog log) {
            boolean dropFirst = backpressure == CustomLogConfiguration.Backpressure.DROP_BUFFER_BY_OLDEST;
            int droppedCount = 0;

            while (customLogs.size() >= bufferSize) {
                if (dropFirst) {
                    customLogs.poll();
                    droppedCount++;
                } else {
                    Logger.d("the log buffer is already full and reject the new log.");
                    return;
                }
            }

            Logger.d("filtered out %d overflowed logs from the oldests.", droppedCount);

            customLogs.addLast(log);

            if (transmitter.isConnected()) {
                sendAllInBuffer();
            }
        }

        /**
         * send all logs from the oldest in the log buffers
         * If sending a log failed, it will be put into the head of the log buffer. And then, this schedules the sending-logs instruction with some delay.
         */
        void sendAllInBuffer() {
            while (!customLogs.isEmpty()) {
                CustomLog log = customLogs.poll();

                if (!transmitter.sendLog(log)) {
                    // Don't lost the failed log
                    customLogs.addFirst(log);
                    sendEmptyMessageDelayed(WHAT_SEND_LOGS, 1000L);
                    break;
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SEND_LOGS: {
                    sendAllInBuffer();

                    break;
                }
                default: {
                    if (msg.what >= WHAT_ADD_NEW_LOG) {
                        CustomLog log = (CustomLog) msg.obj;
                        addLogToLast(log);
                    }

                    break;
                }
            }
        }

        /**
         * To avoid what number conflicts, sdk uses a simple counter.
         *
         * @return positive number, which is greater than or equal to the value of {@link #WHAT_ADD_NEW_LOG}
         */
        private int getAndIncrementPushWhatOffset() {
            if (pushWhatOffset > maxWhatOffset) {
                pushWhatOffset = 0;
            }
            return pushWhatOffset++;
        }
    }
}
