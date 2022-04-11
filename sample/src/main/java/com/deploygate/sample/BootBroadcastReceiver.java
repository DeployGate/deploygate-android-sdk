package com.deploygate.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deploygate.sdk.DeployGate;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        Log.i(App.TAG, "Booted");
        DeployGate.logInfo("Booted");
    }
}
