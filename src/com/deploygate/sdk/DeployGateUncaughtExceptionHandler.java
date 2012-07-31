package com.deploygate.sdk;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.content.Intent;

/**
 * Exception handler class that provides crash reporting feature of DeployGate.
 * 
 * @author tnj
 */
class DeployGateUncaughtExceptionHandler implements
		UncaughtExceptionHandler {

	private final UncaughtExceptionHandler mParentHandler;
	private final Context mApplicationContext;

	public DeployGateUncaughtExceptionHandler(Context applicationContext,
			UncaughtExceptionHandler parentHandler) {
		mApplicationContext = applicationContext;
		mParentHandler = parentHandler;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		sendExceptionToService(ex);

		if (mParentHandler != null)
			mParentHandler.uncaughtException(thread, ex);
	}

	private void sendExceptionToService(Throwable ex) {
		Intent service = new Intent(DeployGate.ACTION_REPORT_EXCEPTION);
		service.setPackage(DeployGate.DEPLOYGATE_PACKAGE);
		service.putExtra(DeployGate.EXTRA_PACKAGE, mApplicationContext.getPackageName());
		service.putExtra(DeployGate.EXTRA_EXCEPTION, ex);
		try {
			mApplicationContext.startService(service);
		} catch (Exception e) {
			// we care nothing here
		}
	}
}
