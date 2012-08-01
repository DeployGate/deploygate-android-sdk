
package com.deploygate.sdk;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class DeployGate {
    private static final String TAG = "DeployGate";

    static final String ACTION_APPLICATION_START = "com.deploygate.action.ApplicationStart";
    static final String ACTION_APPLICATION_CRASHED = "com.deploygate.action.ApplicationCrashed";

    static final String DEPLOYGATE_PACKAGE = "com.deploygate";
    static final String EXTRA_EXCEPTION = "com.deploygate.exception";
    static final String EXTRA_PACKAGE = "com.deploygate.package";

    /**
     * Install DeployGate on your application instance. Call this method inside
     * of your {@link Application#onCreate()}.
     * 
     * @param app Application instance, typically just pass <em>this<em>.
     */
    public static void install(Application app) {
        Thread.setDefaultUncaughtExceptionHandler(new DeployGateUncaughtExceptionHandler(
                app.getApplicationContext(), Thread
                        .getDefaultUncaughtExceptionHandler()));
        tellApplicationStart(app);
    }

    private static void tellApplicationStart(Application app) {
        Intent service = new Intent(ACTION_APPLICATION_START);
        service.setPackage(DEPLOYGATE_PACKAGE);
        service.putExtra(EXTRA_PACKAGE, app.getPackageName());
        try {
            app.startService(service);
        } catch (Exception e) {
            // we care nothing here
        }
    }

    public static boolean isManaged() {
        Log.w(TAG, "isManaged() is not implemented yet");
        return false;
    }

    public static boolean isAuthorized() {
        Log.w(TAG, "isAuthorized() is not implemented yet");
        return false;
    }

    public static String getLoginUser() {
        Log.w(TAG, "getLoginUser() is not implemented yet");
        return null;
    }
}
