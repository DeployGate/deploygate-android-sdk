package com.deploygate.sample;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.deploygate.sdk.CustomAttributes;
import com.deploygate.sdk.DeployGate;
import com.deploygate.sdk.DeployGateSdkConfiguration;

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
        // Refer the comment on stableReal/AndroidManifest.xml included in this sample.

        DeployGateSdkConfiguration configuration = new DeployGateSdkConfiguration.Builder()
            // Please note that this callback is called iff you have removed the content provider.
            // For those who wanna use the content provider, SDK provides DeployGate#registerXXXCallback for your use-case.
            .setInitializeCallback(isServiceAvailable -> {
                if (isServiceAvailable) {
                    Log.i(TAG, "SDK is available");
                    DeployGate.logInfo("SDK is available");

                    CustomAttributes attrs = DeployGate.getBuildEnvironment();
                    attrs.putString("build_type", BuildConfig.BUILD_TYPE);
                    attrs.putString("flavor", BuildConfig.FLAVOR);
                } else {
                    Log.i(TAG, "SDK is unavailable");
                    DeployGate.logInfo("SDK is unavailable"); // this fails silently
                }
            })
            .setStatusChangeCallback((isManaged, isAuthorized, loginUsername, isStopped) -> {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isManaged", isManaged);
                bundle.putBoolean("isAuthorized", isAuthorized);
                bundle.putString("loginUsername", loginUsername);
                bundle.putBoolean("isStopped", isStopped);

                String message = String.format(Locale.US, "onStatusChanged(%s)", bundle);

                Log.i(TAG, message);
                DeployGate.logInfo(message);
            })
            .setUpdateAvailableCallback((revision, versionName, versionCode) -> {
                Bundle bundle = new Bundle();
                bundle.putInt("revision", revision);
                bundle.putString("versionName", versionName);
                bundle.putInt("versionCode", versionCode);

                String message = String.format(Locale.US, "onUpdateAvailable(%s)", bundle);

                Log.i(TAG, message);
                DeployGate.logInfo(message);
            })
            .setEnabledOnNonDebuggableBuild(true)
            .build();

        DeployGate.install(this, configuration);

        // If you want to prevent the app distributed by someone else, specify your username on DeployGate
        // with setAuthor method when creating DeployGate SdkConfiguration. like:
        //
        // builder.setAuthor("YOURUSERNAME");
        //
        // You can use DeployGate.isAuthorized() later to check the installation is valid or not.
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        CustomAttributes attrs = DeployGate.getRuntimeExtra();
        attrs.putString("locale", newConfig.locale.toString());
        attrs.putInt("orientation", newConfig.orientation);
        attrs.putFloat("font_scale", newConfig.fontScale);
    }
}
