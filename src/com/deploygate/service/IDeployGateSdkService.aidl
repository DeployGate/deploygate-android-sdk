package com.deploygate.service;

import com.deploygate.service.IDeployGateSdkServiceCallback;

oneway interface IDeployGateSdkService {
	/** Register a callback interface for DeployGate SDK Service. */
	void init(in IDeployGateSdkServiceCallback callback, in Bundle extras);
}	