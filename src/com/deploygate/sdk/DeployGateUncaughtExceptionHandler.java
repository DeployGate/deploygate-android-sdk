
package com.deploygate.sdk;

import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

/**
 * Exception handler class that provides crash reporting feature of DeployGate.
 * 
 * @author tnj
 */
class DeployGateUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private static final String TAG = "DeployGateUncaughtExceptionHandler";
    private final UncaughtExceptionHandler mParentHandler;

    public DeployGateUncaughtExceptionHandler(UncaughtExceptionHandler parentHandler) {
        mParentHandler = parentHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.v(TAG, "DeployGate caught exception, trying to send to service");
        sendExceptionToService(ex);

        if (mParentHandler != null)
            mParentHandler.uncaughtException(thread, ex);
    }

    private void sendExceptionToService(Throwable ex) {
        DeployGate instance = DeployGate.getInstance();
        if (instance != null)
            instance.sendCrashReport(ex);
    }
}
