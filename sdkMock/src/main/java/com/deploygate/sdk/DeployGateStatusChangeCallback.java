package com.deploygate.sdk;

/**
 * A callback interface to receive the app status on the DeployGate. Implement this and pass to
 * {@link DeployGate#registerStatusChangeCallback(DeployGateStatusChangeCallback)} to listen.
 *
 * @since 4.9.0.
 * @see DeployGate#registerStatusChangeCallback(DeployGateStatusChangeCallback)
 */
public interface DeployGateStatusChangeCallback {
  /**
   * Callback to tell the app status on the DeployGate has changed. This will
   * also be called back just after {@link DeployGateInitializeCallback#onInitialized(boolean)},
   * {@link DeployGate#refresh()}.
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
}
