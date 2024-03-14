package com.deploygate.sdk;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.deploygate.sdk.internal.Logger;

/**
 * Shadow of an application that embeds this SDK.
 */
class HostApp {
    public final String packageName;

    /**
     * true if SDK is enabled. Nothing should work if this value is false.
     */
    public final boolean isSdkEnabled;

    /**
     * true if this app can read LogCat.
     */
    public final boolean canUseLogcat;

    /**
     * SDK's model version
     */
    public final int sdkVersion;

    /**
     * SDK's artifact version
     */
    public final String sdkArtifactVersion;

    /**
     * Bit flag representation of active features
     */
    public final int activeFeatureFlags;

    HostApp(
            Context context,
            DeployGateSdkConfiguration sdkConfiguration
    ) {
        this.packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();

        ApplicationInfo info = null;

        try {
            info = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w(e, "unexpected code");
        }

        boolean shouldDisable =
                info == null || info.metaData == null || sdkConfiguration.isDisabled ||
                        !sdkConfiguration.isEnabledOnNonDebuggableBuild && (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != ApplicationInfo.FLAG_DEBUGGABLE;

        if (shouldDisable) {
            Logger.d("DeployGate SDK is unavailable on this app");

            this.isSdkEnabled = false;
            this.canUseLogcat = false;
            this.sdkVersion = 0;
            this.sdkArtifactVersion = null;
            this.activeFeatureFlags = 0;
            return;
        }

        this.isSdkEnabled = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.canUseLogcat = true;
        } else {
            this.canUseLogcat = pm.checkPermission(Manifest.permission.READ_LOGS, packageName) == PackageManager.PERMISSION_GRANTED;
        }

        this.sdkVersion = info.metaData.getInt("com.deploygate.sdk.version", 0);
        this.sdkArtifactVersion = info.metaData.getString("com.deploygate.sdk.artifact_version");

        int supportedFeatureFlags = info.metaData.getInt("com.deploygate.sdk.feature_flags", 0);

        if (!sdkConfiguration.isCaptureEnabled) {
            supportedFeatureFlags ^= Compatibility.DEVICE_CAPTURE.bitMask;
        }

        this.activeFeatureFlags = supportedFeatureFlags;
    }

    final boolean canUseDeviceCapture() {
        return (activeFeatureFlags & Compatibility.DEVICE_CAPTURE.bitMask) == BuildConfig.DEVICE_CAPTURE;
    }
}
