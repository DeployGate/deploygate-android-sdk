package com.deploygate.sdk;

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
