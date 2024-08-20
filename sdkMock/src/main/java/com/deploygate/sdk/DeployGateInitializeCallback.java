package com.deploygate.sdk;

/**
 * A callback interface to receive DeployGate service initialization events.Implement this and pass to
 * {@link DeployGate#registerInitializeCallback(DeployGateInitializeCallback)} to listen.
 *
 * @since 4.9.0.
 * @see DeployGate#registerInitializeCallback(DeployGateInitializeCallback)
 */
public interface DeployGateInitializeCallback {
  /**
   * Callback to tell the finish of DeployGate service initialization
   * procedure.
   *
   * @param isServiceAvailable true if DeployGate service is available on the
   *                           device.
   */
  public void onInitialized(boolean isServiceAvailable);
}
