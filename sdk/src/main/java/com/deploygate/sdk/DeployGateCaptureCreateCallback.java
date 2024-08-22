package com.deploygate.sdk;

/**
 * A callback interface to receive the capture creation event.
 * 
 * @since 4.9.0
 * @see DeployGate#registerCaptureCreateCallback(DeployGateCaptureCreateCallback) 
 */
public interface DeployGateCaptureCreateCallback {
    /**
     * Callback to tell the new capture is successfully created.
     *
     * @param captureUrl      URL of the created capture
     * @param createdAtMillis Created time of the capture in milliseconds
     * @since 4.9.0
     */
    public void onCaptureCreated(
            String captureUrl,
            long createdAtMillis
    );
}
