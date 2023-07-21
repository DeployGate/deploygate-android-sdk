package com.deploygate.sdk;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.IDeployGateSdkService;

import java.util.UUID;

interface ILogcatInstructionSerializer {

    /**
     * Bind a service and trigger several instructions immediately.
     *
     * @param service
     *         the latest service connection
     */
    void connect(IDeployGateSdkService service);

    /**
     * Release a service connection and cancel all pending instructions and on-going instruction.
     */
    void disconnect();

    /**
     * Create and enqueue a request to start sending oneshot logcat
     */
    boolean requestOneshotLogcat();

    /**
     * Create and enqueue a request to start sending oneshot logcat with a capture id
     */
    boolean requestOneshotLogcat(UUID captureId);

    /**
     * Create and enqueue a request to start sending streamed logcat
     */
    boolean requestStreamedLogcat(
            String sessionKey
    );

    /**
     * Enable the logcat process. Serialization won't be disabled.
     *
     * @param enabled
     *         specify true to enable the logcat process
     */
    void setEnabled(boolean enabled);

    /**
     * Cancel the on-going streamed logcat process
     */
    void stopStream();

    ILogcatInstructionSerializer NULL_INSTANCE = new ILogcatInstructionSerializer() {

        @Override
        public void connect(IDeployGateSdkService service) {
            Logger.d("Logcat (no-op): connect");
        }

        @Override
        public void disconnect() {
            Logger.d("Logcat (no-op): disconnect");
        }

        @Override
        public boolean requestOneshotLogcat() {
            Logger.d("Logcat (no-op): requestOneshotLogcat");
            return false;
        }

        @Override
        public boolean requestOneshotLogcat(UUID captureId) {
            Logger.d("Logcat (no-op): requestOneshotLogcat");
            return false;
        }

        @Override
        public boolean requestStreamedLogcat(String sessionKey) {
            Logger.d("Logcat (no-op): requestStreamedLogcat(%s)", sessionKey);
            return false;
        }

        @Override
        public void setEnabled(boolean enabled) {
            Logger.d("Logcat (no-op): setEnabled(%s)", String.valueOf(enabled));
        }

        @Override
        public void stopStream() {
            Logger.d("Logcat (no-op): stopStream");
        }
    };
}
