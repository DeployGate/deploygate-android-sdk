package com.deploygate.service;

import com.deploygate.service.IDeployGateSdkServiceCallback;

interface IDeployGateSdkService {
	/** Register a callback interface for DeployGate SDK Service. */
	void init(in IDeployGateSdkServiceCallback callback, in String packageName, in Bundle extras);
	void sendEvent(in String packageName, in String action, in Bundle extras);
}	