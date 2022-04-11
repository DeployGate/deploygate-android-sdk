package com.deploygate.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.deploygate.sdk.DeployGate;

public class DirectBootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        Log.i(App.TAG, "Direct-booted");
        DeployGate.logInfo("Direct-booted");

        if (Build.VERSION_CODES.N <= Build.VERSION.SDK_INT) {
            context.registerReceiver(((App) context).userUnlockBroadcastReceiver, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
        }
    }
}
