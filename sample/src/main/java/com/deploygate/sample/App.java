package com.deploygate.sample;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.deploygate.sdk.DeployGate;
import com.deploygate.sdk.DeployGateCallback;

import java.util.Locale;

/**
 * THIS CLASS IS NOT USED BY DEFAULT. To use this implementation, you have to add on AndroidManifest.xml manually.
 */
public class App extends Application {
    public static final String TAG = "DeployGateSDKSample";

    @Override
    public void onCreate() {
        super.onCreate();

        // In most cases you don't have to do this, but if you have multiple processes in your application,
        // or you want to customize initialize options, you can install DeployGate manually.
        //
        // Note that you also need to edit your AndroidManifest.xml to activate customized initializer.
        // Refer the comment on realSdk/AndroidManifest.xml included in this sample.

        DeployGate.install(this, new DeployGateCallback() {
            // Please note that this callback is called iff you have removed the content provider.
            // For those who wanna use the content provider, SDK provides DeployGate#registerCallback for your use-case.

            @Override
            public void onInitialized(boolean isServiceAvailable) {
                if (isServiceAvailable) {
                    Log.i(TAG, "SDK is available");
                    DeployGate.logInfo("SDK is available");
                } else {
                    Log.i(TAG, "SDK is unavailable");
                    DeployGate.logInfo("SDK is unavailable"); // this fails silently
                }
            }

            @Override
            public void onStatusChanged(
                    boolean isManaged,
                    boolean isAuthorized,
                    String loginUsername,
                    boolean isStopped
            ) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isManaged", isManaged);
                bundle.putBoolean("isAuthorized", isAuthorized);
                bundle.putString("loginUsername", loginUsername);
                bundle.putBoolean("isStopped", isStopped);

                String message = String.format(Locale.US, "onStatusChanged(%s)", bundle.toString());

                Log.i(TAG, message);
                DeployGate.logInfo(message);
            }

            @Override
            public void onUpdateAvailable(
                    int revision,
                    String versionName,
                    int versionCode
            ) {
                Bundle bundle = new Bundle();
                bundle.putInt("revision", revision);
                bundle.putString("versionName", versionName);
                bundle.putInt("versionCode", versionCode);

                String message = String.format(Locale.US, "onUpdateAvailable(%s)", bundle.toString());

                Log.i(TAG, message);
                DeployGate.logInfo(message);
            }
        }, true);

        // If you want to prevent the app distributed by someone else, specify your username on DeployGate
        // as a second argument of DeployGate.install, like:
        //
        // DeployGate.install(this, "YOURUSERNAME");
        //
        // You can use DeployGate.isAuthorized() later to check the installation is valid or not.
    }
}
