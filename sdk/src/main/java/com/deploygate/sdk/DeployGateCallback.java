
package com.deploygate.sdk;


/**
 * A callback interface to receive DeployGate events. Implement this and pass to
 * {@link DeployGate#registerCallback(DeployGateCallback, boolean)} to listen.
 *
 * @author tnj
 */
public interface DeployGateCallback {
    /**
     * Callback to tell the finish of DeployGate service initialization
     * procedure.
     *
     * @param isServiceAvailable true if DeployGate service is available on the
     *                           device.
     */
    public void onInitialized(
            boolean isServiceAvailable
    );

    /**
     * Callback to tell the app status on the DeployGate has changed. This will
     * also be called back just after {@link #onInitialized(boolean)}.
     * {@link DeployGate#refresh()} and @link DeployGate#registerCallback(DeployGateCallback, boolean)}
     * with true in the second argument called this in the prior to 5.0.0 but it's no longer called.
     *
     * @param isManaged     true if the app is known on DeployGate app
     * @param isAuthorized  true if the app is on current user's installed list
     * @param loginUsername Current login username, returned only when
     *                      isAuthorized is true.
     * @param isStopped     Reserved.
     */
    public void onStatusChanged(
            boolean isManaged,
            boolean isAuthorized,
            String loginUsername,
            boolean isStopped
    );

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

    /**
     * Callback to tell the current app status if requested. This will
     * be called back just after {@link DeployGate#refresh()} and
     * {@link DeployGate#registerCallback(DeployGateCallback, boolean)} with
     * true in the second argument. This is called only after initialized.
     *
     * @param isAvailable   true if DeployGate service is available on the device
     * @param isManaged     true if the app is known on DeployGate app
     * @param isAuthorized  true if the app is on current user's installed list
     * @param loginUsername Current login username, returned only when
     *                      isAuthorized is true.
     * @param isStopped     Reserved.
     *
     * @since 5.0.0
     */
    void onCurrentStateNotified(
            boolean isAvailable,
            boolean isManaged,
            boolean isAuthorized,
            String loginUsername,
            boolean isStopped
    );
}
