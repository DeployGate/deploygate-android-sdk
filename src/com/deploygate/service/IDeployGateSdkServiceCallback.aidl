package com.deploygate.service;

oneway interface IDeployGateSdkServiceCallback {
    /** Event callback from DeployGate client service. */
	void onEvent(String action, in Bundle extras);
}