package com.deploygate.sdk;

/**
 * A callback interface to receive the app update event. Implement this and pass to
 * {@link DeployGate#registerUpdateAvailableCallback(DeployGateUpdateAvailableCallback)} to listen.
 *
 * @since 4.9.0.
 * @see DeployGate#registerUpdateAvailableCallback(DeployGateUpdateAvailableCallback)
 */
public interface DeployGateUpdateAvailableCallback {
    /**
     * Callback to tell the new version is available.
     *
     * @param revision    revision of new version
     * @param versionName user-defined version name of new version
     * @param versionCode user-defined version code of new version
     */
    public void onUpdateAvailable(
            int revision,
            String versionName,
            int versionCode
    );
}
