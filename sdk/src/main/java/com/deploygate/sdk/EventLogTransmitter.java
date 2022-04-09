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

class EventLogTransmitter {
    private final String packageName;
    private final EventLogConfiguration configuration;

    @SuppressWarnings("FieldCanBeLocal")
    private final HandlerThread thread;
    private EventLogHandler handler;

    private volatile IDeployGateSdkService service;

    EventLogTransmitter(
            String packageName,
            EventLogConfiguration configuration
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

        EventLog log = new EventLog(type, body);
        handler.enqueueNewLog(log);
    }

    private boolean sendLog(EventLog log) {
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

    private void ensureHandlerInitialized() {
        if (handler != null) {
            return;
        }

        synchronized (configuration) {
            if (handler != null) {
                return;
            }

            handler = new EventLogHandler(thread.getLooper(), this, configuration.backpressure, configuration.bufferSize);
        }
    }

    static class EventLogHandler extends Handler {
        private static final int WHAT_EXTRUDE_ALL_BUFFER = 0x30;
        private static final int WHAT_PUSH_LOG = 0x100;

        private final EventLogTransmitter extruder;
        private final Backpressure backpressure;
        private final int bufferSize;
        private final LinkedList<EventLog> eventLogs;
        private int pushWhatOffset = 0;

        EventLogHandler(
                Looper looper,
                EventLogTransmitter extruder,
                Backpressure backpressure,
                int bufferSize
        ) {
            super(looper);
            this.extruder = extruder;
            this.backpressure = backpressure;
            this.bufferSize = bufferSize;
            this.eventLogs = new LinkedList<>();
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

        void enqueueNewLog(EventLog log) {
            Message msg = obtainMessage(WHAT_PUSH_LOG + getAndIncrementPushWhatOffset(), log);
            sendMessage(msg);
        }

        void addLogToLast(EventLog log) {
            boolean dropFirst = backpressure == Backpressure.DROP_OLDEST;
            int droppedCount = 0;

            while (eventLogs.size() >= bufferSize) {
                if (dropFirst) {
                    eventLogs.poll();
                    droppedCount++;
                } else {
                    Logger.d("the queue is already full and reject the new element.");
                    return;
                }
            }

            Logger.d("filtered out %d overflowed old elements.", droppedCount);

            eventLogs.addLast(log);
        }

        void extrudeAllInBuffer() {
            while (!eventLogs.isEmpty()) {
                EventLog log = eventLogs.poll();

                if (!extruder.sendLog(log)) {
                    // push back
                    eventLogs.addFirst(log);
                    break;
                }
            }

            if (!eventLogs.isEmpty()) {
                sendEmptyMessageDelayed(WHAT_EXTRUDE_ALL_BUFFER, 1000L);
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
                        EventLog log = (EventLog) msg.obj;
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
