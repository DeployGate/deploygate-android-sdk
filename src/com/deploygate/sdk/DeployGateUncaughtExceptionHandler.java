
package com.deploygate.sdk;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Exception handler class that provides crash reporting feature of DeployGate.
 * 
 * @author tnj
 */
class DeployGateUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private static final String TAG = "DeployGateUncaughtExceptionHandler";
    private final UncaughtExceptionHandler mParentHandler;
    private final Context mApplicationContext;

    public DeployGateUncaughtExceptionHandler(Context applicationContext,
            UncaughtExceptionHandler parentHandler) {
        mApplicationContext = applicationContext;
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
        instance.sendCrashReport(ex);
        /*
        Intent service = new Intent(DeployGate.ACTION_APPLICATION_CRASHED);
        service.setPackage(DeployGate.DEPLOYGATE_PACKAGE);
        service.putExtra(DeployGate.EXTRA_PACKAGE_NAME, mApplicationContext.getPackageName());
        service.putExtra(DeployGate.EXTRA_EXCEPTION, ex);
        try {
            mApplicationContext.startService(service);
        } catch (Exception e) {
            // we care nothing here
            Log.v(TAG, "failed to start service: " + e.getMessage());
        }
        */
    }
}
