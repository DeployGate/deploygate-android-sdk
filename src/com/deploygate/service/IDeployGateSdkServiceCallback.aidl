package com.deploygate.service;

oneway interface IDeployGateSdkServiceCallback {
	/** Called back after the service connection inititialized. This may be called several time in case of service reconnection. 
	 * @param isManaged Indicates this app is managed on DeployGate or not. If this is false, any calls to DeployGate will not be accepted.
	 * @param isAuthorized Indicates the DeployGate user has granted to use this app. This will only be true if this app is secure and the user is invited or in owner group.  
	 * @param loginUserName Current login user name on DeployGate.
	 * @param isStopped Indicates if the app requested to stop working. If this is true, the app should stop working immediately.
	 */
	void onInitialized(in boolean isManaged, in boolean isAuthorized, in String loginUserName, in boolean isStopped);
	
	void onLogCatRequested(); 
	void onLogCatStopRequested();
	void onStopApplicationRequested();
	void onUpdateArrived(int serial, String versionName, String versionCode);
}