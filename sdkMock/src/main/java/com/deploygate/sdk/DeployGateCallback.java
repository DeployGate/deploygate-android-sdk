package com.deploygate.sdk;

/**
 * A callback interface to receive DeployGate events. Implement this and pass to
 * {@link DeployGate#registerCallback(DeployGateCallback, boolean)} to listen.
 *
 * @author tnj
 * @deprecated since 4.9.0. Use each extended callback interfaces instead.
 * @see com.deploygate.sdk.DeployGateInitializeCallback
 * @see com.deploygate.sdk.DeployGateStatusChangeCallback
 * @see com.deploygate.sdk.DeployGateUpdateAvailableCallback
 */
@Deprecated
public interface DeployGateCallback extends DeployGateInitializeCallback, DeployGateStatusChangeCallback, DeployGateUpdateAvailableCallback {
}