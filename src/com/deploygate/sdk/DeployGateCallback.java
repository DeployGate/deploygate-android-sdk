package com.deploygate.sdk;

public interface DeployGateCallback {
    public void onInitialized(boolean isServiceAvailable);
    public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped);
    public void onUpdateAvailable(int serial, String versionName, int versionCode);
}