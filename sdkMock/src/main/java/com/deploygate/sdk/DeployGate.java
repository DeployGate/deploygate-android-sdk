package com.deploygate.sdk;

import android.app.Application;
import android.content.Context;

public class DeployGate {

    private static final CustomAttributes sAttributes = new CustomAttributes();

    static void clear() {
    }

    public static void install(
            Context context,
            DeployGateSdkConfiguration sdkConfiguration
    ) {
    }

    public static void install(Application app) {
    }

    public static void install(
            Application app,
            String author
    ) {
    }

    public static void install(
            Application app,
            DeployGateCallback callback
    ) {
    }

    public static void install(
            Application app,
            boolean forceApplyOnReleaseBuild
    ) {
    }

    public static void install(
            Application app,
            String author,
            DeployGateCallback callback
    ) {
    }

    public static void install(
            Application app,
            DeployGateCallback callback,
            boolean forceApplyOnReleaseBuild
    ) {
    }

    public static void install(
            Application app,
            String author,
            DeployGateCallback callback,
            boolean forceApplyOnReleaseBuild
    ) {
    }

    public static void install(
            Application app,
            String author,
            DeployGateCallback callback,
            boolean forceApplyOnReleaseBuild,
            CustomLogConfiguration configuration
    ) {
    }

    public static void refresh() {
    }

    public static void registerInitializeCallback(DeployGateInitializeCallback callback) {
    }

    public static void unregisterInitializeCallback(DeployGateInitializeCallback callback) {
    }

    public static void registerStatusChangeCallback(DeployGateStatusChangeCallback callback) {
    }

    public static void unregisterStatusChangeCallback(DeployGateStatusChangeCallback callback) {
    }

    public static void registerUpdateAvailableCallback(DeployGateUpdateAvailableCallback callback) {
    }

    public static void unregisterUpdateAvailableCallback(DeployGateUpdateAvailableCallback callback) {
    }

    public static void registerCaptureCreateCallback(DeployGateCaptureCreateCallback callback) {
    }

    public static void unregisterCaptureCreateCallback(DeployGateCaptureCreateCallback callback) {
    }

    @Deprecated
    public static void registerCallback(
            DeployGateCallback listener,
            boolean refreshImmediately
    ) {
    }

    @Deprecated
    public static void unregisterCallback(final DeployGateCallback listener) {
    }

    public static boolean isInitialized() {
        return false;
    }

    public static boolean isDeployGateAvaliable() {
        return false;
    }

    public static boolean isManaged() {
        return false;
    }

    public static boolean isAuthorized() {
        return false;
    }

    public static String getLoginUsername() {
        return null;
    }

    public static String getAuthorUsername() {
        return null;
    }

    public static void logError(String message) {
    }

    public static void logWarn(String message) {
    }

    public static void logDebug(String message) {
    }

    public static void logInfo(String message) {
    }

    public static void logVerbose(String message) {
    }

    public static void requestLogCat() {
    }

    public static int getCurrentRevision() {
        return 0;
    }

    public static String getDistributionUrl() {
        return null;
    }

    public static String getDistributionId() {
        return null;
    }

    public static String getDistributionTitle() {
        return null;
    }

    public static int getDeployGateVersionCode() {
        return 0;
    }

    public static long getDeployGateLongVersionCode() {
        return 0;
    }

    public static boolean hasUpdate() {
        return false;
    }

    public static int getUpdateRevision() {
        return 0;
    }

    public static int getUpdateVersionCode() {
        return 0;
    }

    public static String getUpdateVersionName() {
        return null;
    }

    public static String getUpdateMessage() {
        return null;
    }

    public static void installUpdate() {
    }

    public static void openComments() {
    }

    public static void composeComment() {
    }

    public static void composeComment(String defaultComment) {
    }

    public static String getDistributionUserName() {
        return null;
    }

    public static CustomAttributes getBuildEnvironment() {
        return sAttributes;
    }

    public static CustomAttributes getRuntimeExtra() {
        return sAttributes;
    }
}
