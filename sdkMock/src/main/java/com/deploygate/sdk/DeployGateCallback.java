package com.deploygate.sdk;


/**
 * A callback interface to receive DeployGate events. Implement this and pass to
 * {@link DeployGate#registerCallback(DeployGateCallback, boolean)} to listen.
 *
 * @author tnj
 * @deprecated since 4.9.0. Use each extended callback interfaces instead.
 */
@Deprecated
public interface DeployGateCallback extends DeployGateInitializeCallback, DeployGateStatusChangeCallback, DeployGateUpdateAvailableCallback {
    /**
     * Callback to tell the finish of DeployGate service initialization
     * procedure.
     *
     * @param isServiceAvailable
     *         true if DeployGate service is available on the
     *         device.
     * @deprecated since 4.9.0. Use {@link DeployGateInitializeCallback#onInitialized(boolean)} instead.
     */
    @Deprecated
    @Override
    public void onInitialized(boolean isServiceAvailable);

    /**
     * Callback to tell the app status on the DeployGate has changed. This will
     * also be called back just after {@link #onInitialized(boolean)},
     * {@link DeployGate#refresh()} and
     * {@link DeployGate#registerCallback(DeployGateCallback, boolean)} with
     * true in second argument.
     *
     * @param isManaged
     *         true if the app is known on DeployGate app
     * @param isAuthorized
     *         true if the app is on current user's installed list
     * @param loginUsername
     *         Current login username, returned only when
     *         isAuthorized is true.
     * @param isStopped
     *         Reserved.
     * @deprecated since 4.9.0. Use {@link DeployGateStatusChangeCallback#onStatusChanged(boolean, boolean, String, boolean)} instead.
     */
    @Deprecated
    @Override
    public void onStatusChanged(
        boolean isManaged,
        boolean isAuthorized,
        String loginUsername,
        boolean isStopped
    );

    /**
     * Callback to tell the new version is available.
     *
     * @param revision
     *         revision of new version
     * @param versionName
     *         user-defined version name of new version
     * @param versionCode
     *         user-defined version code of new version
     * @deprecated since 4.9.0. Use {@link DeployGateUpdateAvailableCallback#onUpdateAvailable(int, String, int)} instead.
     */
    @Deprecated
    @Override
    public void onUpdateAvailable(
        int revision,
        String versionName,
        int versionCode
    );
}