package com.deploygate.sdk;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.deploygate.sdk.internal.Logger;

class HostApp {
    public final String packageName;
    public final boolean canUseLogcat;
    public final boolean debuggable;
    public final int sdkVersion;

    HostApp(
            Context context
    ) {
        this.packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();

        ApplicationInfo info = null;

        try {
            info = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w(e, "unexpected code");
        }

        if (info == null) {
            this.debuggable = false;
            this.canUseLogcat = false;
            this.sdkVersion = 0;
            return;
        }

        this.debuggable = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.canUseLogcat = true;
        } else {
            this.canUseLogcat = pm.checkPermission(Manifest.permission.READ_LOGS, packageName) == PackageManager.PERMISSION_GRANTED;
        }

        this.sdkVersion = info.metaData.getInt("com.deploygate.sdk.version", 0);
    }
}
