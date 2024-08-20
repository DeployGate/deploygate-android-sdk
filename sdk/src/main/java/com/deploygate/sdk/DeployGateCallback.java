package com.deploygate.sdk;

/**
 * A callback interface to receive DeployGate events.
 *
 * @author tnj
 * @deprecated since 4.9.0. Use fine-grained callback interfaces for each purpose.
 * @see com.deploygate.sdk.DeployGateInitializeCallback
 * @see com.deploygate.sdk.DeployGateStatusChangeCallback
 * @see com.deploygate.sdk.DeployGateUpdateAvailableCallback
 */
@Deprecated
public interface DeployGateCallback extends DeployGateInitializeCallback, DeployGateStatusChangeCallback, DeployGateUpdateAvailableCallback {
}