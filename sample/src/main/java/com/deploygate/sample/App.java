
package com.deploygate.sample;

import android.app.Application;

import com.deploygate.sdk.DeployGate;

/**
 * THIS CLASS IS NOT USED BY DEFAULT. To use this implementation, you have to add on AndroidManifest.xml manually.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // In most cases you don't have to do this, but if you have multiple processes in your application,
        // or you want to customize initialize options, you can install DeployGate manually.
        //
        // Note that you also need to edit your AndroidManifest.xml to activate customized initializer.
        // Refer the comment on AndroidManifest.xml included in this sample.

        DeployGate.install(this, null, true);

        // If you want to prevent the app distributed by someone else, specify your username on DeployGate
        // as a second argument of DeployGate.install, like:
        //
        // DeployGate.install(this, "YOURUSERNAME");
        //
        // You can use DeployGate.isAuthorized() later to check the installation is valid or not.
    }
}
