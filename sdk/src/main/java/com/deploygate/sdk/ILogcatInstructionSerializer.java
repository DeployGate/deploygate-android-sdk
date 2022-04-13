package com.deploygate.sdk;

import com.deploygate.sdk.internal.Logger;
import com.deploygate.service.IDeployGateSdkService;

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
     * Create and enqueue a request to send logcat
     *
     * @param isOneShot
     *         specify true for oneshot logcat, otherwise for streamed logcat
     */
    void requestSendingLogcat(boolean isOneShot);

    /**
     * Enable the logcat process. Serialization won't be disabled.
     *
     * @param enabled
     *         specify true to enable the logcat process
     */
    void setEnabled(boolean enabled);

    /**
     * Cancel the on-going logcat process
     */
    void cancel();

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
        public void requestSendingLogcat(boolean isOneShot) {
            Logger.d("Logcat (no-op): requestSendingLogcat(%s)", String.valueOf(isOneShot));
        }

        @Override
        public void setEnabled(boolean enabled) {
            Logger.d("Logcat (no-op): setEnabled(%s)", String.valueOf(enabled));
        }

        @Override
        public void cancel() {
            Logger.d("Logcat (no-op): cancel");
        }
    };
}
