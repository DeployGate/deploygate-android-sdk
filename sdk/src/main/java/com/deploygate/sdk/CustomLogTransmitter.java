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

        handler.cancelExtruding();
        this.service = service;
        handler.extrudeAllLogs();
    }

    public final void disconnect() {
        ensureHandlerInitialized();

        handler.cancelExtruding();
        this.service = null;
    }

    public final synchronized void transmit(
            String type,
            String body
    ) {
        ensureHandlerInitialized();

        CustomLog log = new CustomLog(type, body);
        handler.enqueueNewLog(log);
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

    static class CustomLogHandler extends Handler {
        static final int WHAT_EXTRUDE_ALL_BUFFER = 0x30;
        static final int WHAT_PUSH_LOG = 0x100;

        private final CustomLogTransmitter extruder;
        private final Backpressure backpressure;
        private final int bufferSize;
        private final LinkedList<CustomLog> customLogs;
        private int pushWhatOffset = 0;

        CustomLogHandler(
                Looper looper,
                CustomLogTransmitter extruder,
                Backpressure backpressure,
                int bufferSize
        ) {
            super(looper);
            this.extruder = extruder;
            this.backpressure = backpressure;
            this.bufferSize = bufferSize;
            this.customLogs = new LinkedList<>();
        }

        void cancelExtruding() {
            removeMessages(WHAT_EXTRUDE_ALL_BUFFER);
        }

        void extrudeAllLogs() {
            if (hasMessages(WHAT_EXTRUDE_ALL_BUFFER)) {
                return;
            }

            sendEmptyMessage(WHAT_EXTRUDE_ALL_BUFFER);
        }

        void enqueueNewLog(CustomLog log) {
            Message msg = obtainMessage(WHAT_PUSH_LOG + getAndIncrementPushWhatOffset(), log);
            sendMessage(msg);
        }

        void addLogToLast(CustomLog log) {
            boolean dropFirst = backpressure == Backpressure.DROP_OLDEST;
            int droppedCount = 0;

            while (customLogs.size() >= bufferSize) {
                if (dropFirst) {
                    customLogs.poll();
                    droppedCount++;
                } else {
                    Logger.d("the queue is already full and reject the new element.");
                    return;
                }
            }

            Logger.d("filtered out %d overflowed old elements.", droppedCount);

            customLogs.addLast(log);

            if (extruder.isConnected()) {
                extrudeAllInBuffer();
            }
        }

        void extrudeAllInBuffer() {
            while (!customLogs.isEmpty()) {
                CustomLog log = customLogs.poll();

                if (!extruder.sendLog(log)) {
                    // push back
                    customLogs.addFirst(log);
                    sendEmptyMessageDelayed(WHAT_EXTRUDE_ALL_BUFFER, 1000L);
                    break;
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_EXTRUDE_ALL_BUFFER: {
                    extrudeAllInBuffer();

                    break;
                }
                default: {
                    if (msg.what >= WHAT_PUSH_LOG) {
                        CustomLog log = (CustomLog) msg.obj;
                        addLogToLast(log);
                    }

                    break;
                }
            }
        }

        private int getAndIncrementPushWhatOffset() {
            if (pushWhatOffset > 1000) {
                pushWhatOffset = 0;
            }
            return pushWhatOffset++;
        }
    }
}
