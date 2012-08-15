
package com.deploygate.sdk;

public interface DeployGateCallback {
    /**
     * 
     * @param isServiceAvailable
     */
    public void onInitialized(boolean isServiceAvailable);

    /**
     * 
     * @param isManaged
     * @param isAuthorized
     * @param loginUsername
     * @param isStopped Unimplemented yet.
     */
    public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername,
            boolean isStopped);

    /**
     * Unimplemented yet.
     * 
     * @param revision
     * @param versionName
     * @param versionCode
     */
    public void onUpdateAvailable(int revision, String versionName, int versionCode);
}
