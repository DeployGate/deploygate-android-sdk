package com.deploygate.sdk;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * This test class will make sure all *public* interfaces are defined as expected
 * <p>
 * The result depends on the situation that no DeployGate app is found anyway.
 * It means that this class does not cover several important situations.
 */
@RunWith(AndroidJUnit4.class)
public class DeployGateInterfaceTest {
    @NonNull
    Application app;

    DeployGateCallback callback;
    DeployGateInitializeCallback initializeCallback;
    DeployGateStatusChangeCallback statusChangeCallback;
    DeployGateUpdateAvailableCallback updateAvailableCallback;

    @Before
    public void setUp() {
        app = getApplicationContext();
        callback = new DeployGateCallback() {

            @Override
            public void onInitialized(boolean isServiceAvailable) {

            }

            @Override
            public void onStatusChanged(
                    boolean isManaged,
                    boolean isAuthorized,
                    String loginUsername,
                    boolean isStopped
            ) {

            }

            @Override
            public void onUpdateAvailable(
                    int revision,
                    String versionName,
                    int versionCode
            ) {

            }
        };
        initializeCallback = new DeployGateInitializeCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {
                // no-op
            }
        };
        statusChangeCallback = new DeployGateStatusChangeCallback() {
            @Override
            public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {
                // no-op
            }
        };
        updateAvailableCallback = new DeployGateUpdateAvailableCallback() {
            @Override
            public void onUpdateAvailable(int revision, String versionName, int versionCode) {
                // no-op
            }
        };

        DeployGate.clear();
    }

    @Test
    public void install__Context_SdkConfiguration() {
        DeployGate.install(
                app,
                new DeployGateSdkConfiguration.Builder()
                        .setCaptureEnabled(false)
                        .setDisabled(false)
                        .setEnabledOnNonDebuggableBuild(false)
                        .setAppOwnerName("sample")
                        .setCrashReportingEnabled(false)
                        .setCustomLogConfiguration(
                                new CustomLogConfiguration.Builder()
                                        .setBackpressure(CustomLogConfiguration.Backpressure.DROP_BUFFER_BY_OLDEST)
                                        .setBufferSize(5)
                                        .build()
                        )
                        .setCallback(new DeployGateCallback() {
                            @Override
                            public void onInitialized(boolean isServiceAvailable) {

                            }

                            @Override
                            public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {

                            }

                            @Override
                            public void onUpdateAvailable(int revision, String versionName, int versionCode) {

                            }
                        })
                        .build()
        );
    }

    @Test
    public void install__Application() {
        DeployGate.install(app);
    }

    @Test
    public void install__Application_String() {
        DeployGate.install(app, "author");
    }

    @Test
    public void install__Application_DeployGateCallback() {
        DeployGate.install(app, callback);
    }

    @Test
    public void install__Application_String_DeployGateCallback() {
        DeployGate.install(app, "author", callback);
    }

    @Test
    public void install__Application_DeployGateCallback_boolean() {
        DeployGate.install(app, callback, true);
    }

    @Test
    public void install__Application_String_DeployGateCallback_boolean() {
        DeployGate.install(app, "author", callback, true);
    }

    @Test
    public void install__Application_String_DeployGateCallback_CustomLogConfiguration() {
        DeployGate.install(app, "author", callback, true, new CustomLogConfiguration.Builder().build());
    }

    @Test
    public void refresh() {
        DeployGate.refresh();
    }

    @Test
    public void registerCallback() {
        DeployGate.registerCallback(null, true);
        DeployGate.registerCallback(null, false);
        DeployGate.registerCallback(callback, true);
        DeployGate.registerCallback(callback, false);
        DeployGate.registerCallback(new DeployGateCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {

            }

            @Override
            public void onStatusChanged(
                    boolean isManaged,
                    boolean isAuthorized,
                    String loginUsername,
                    boolean isStopped
            ) {

            }

            @Override
            public void onUpdateAvailable(
                    int revision,
                    String versionName,
                    int versionCode
            ) {

            }
        }, true);
        DeployGate.registerCallback(new DeployGateCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {

            }

            @Override
            public void onStatusChanged(
                    boolean isManaged,
                    boolean isAuthorized,
                    String loginUsername,
                    boolean isStopped
            ) {

            }

            @Override
            public void onUpdateAvailable(
                    int revision,
                    String versionName,
                    int versionCode
            ) {

            }
        }, false);

        DeployGate.registerInitializeCallback(null);
        DeployGate.registerInitializeCallback(initializeCallback);
        DeployGate.registerInitializeCallback(new DeployGateInitializeCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {

            }
        });

        DeployGate.registerStatusChangeCallback(null);
        DeployGate.registerStatusChangeCallback(statusChangeCallback);
        DeployGate.registerStatusChangeCallback(new DeployGateStatusChangeCallback() {
            @Override
            public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {

            }
        });

        DeployGate.registerUpdateAvailableCallback(null);
        DeployGate.registerUpdateAvailableCallback(updateAvailableCallback);
        DeployGate.registerUpdateAvailableCallback(new DeployGateUpdateAvailableCallback() {
            @Override
            public void onUpdateAvailable(int revision, String versionName, int versionCode) {

            }
        });
    }

    @Test
    public void unregisterCallback() {
        DeployGate.unregisterCallback(null);
        DeployGate.unregisterCallback(new DeployGateCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {

            }

            @Override
            public void onStatusChanged(
                    boolean isManaged,
                    boolean isAuthorized,
                    String loginUsername,
                    boolean isStopped
            ) {

            }

            @Override
            public void onUpdateAvailable(
                    int revision,
                    String versionName,
                    int versionCode
            ) {

            }
        });
        DeployGate.unregisterCallback(callback);

        DeployGate.unregisterInitializeCallback(null);
        DeployGate.unregisterInitializeCallback(new DeployGateInitializeCallback() {
            @Override
            public void onInitialized(boolean isServiceAvailable) {

            }
        });
        DeployGate.unregisterInitializeCallback(initializeCallback);

        DeployGate.unregisterStatusChangeCallback(null);
        DeployGate.unregisterStatusChangeCallback(new DeployGateStatusChangeCallback() {
            @Override
            public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername, boolean isStopped) {

            }
        });
        DeployGate.unregisterStatusChangeCallback(statusChangeCallback);

        DeployGate.unregisterUpdateAvailableCallback(null);
        DeployGate.unregisterUpdateAvailableCallback(new DeployGateUpdateAvailableCallback() {
            @Override
            public void onUpdateAvailable(int revision, String versionName, int versionCode) {

            }
        });
        DeployGate.unregisterUpdateAvailableCallback(updateAvailableCallback);
    }

    @Test
    public void isInitialized() {
        Truth.assertThat(DeployGate.isInitialized()).isFalse();
    }

    @Test
    public void isDeployGateAvaliable() {
        Truth.assertThat(DeployGate.isDeployGateAvaliable()).isFalse();
    }

    @Test
    public void isManaged() {
        Truth.assertThat(DeployGate.isManaged()).isFalse();
    }

    @Test
    public void isAuthorized() {
        Truth.assertThat(DeployGate.isAuthorized()).isFalse();
    }

    @Test
    public void getLoginUsername() {
        Truth.assertThat(DeployGate.getLoginUsername()).isNull();
    }

    @Test
    public void getAuthorUsername() {
        Truth.assertThat(DeployGate.getAuthorUsername()).isNull();
    }

    @Test
    public void logError() {
        DeployGate.logError("error");
    }

    @Test
    public void logWarn() {
        DeployGate.logWarn("warn");
    }

    @Test
    public void logDebug() {
        DeployGate.logDebug("debug");
    }

    @Test
    public void logInfo() {
        DeployGate.logInfo("info");
    }

    @Test
    public void logVerbose() {
        DeployGate.logVerbose("verbose");
    }

    @Test
    public void requestLogCat() {
        DeployGate.requestLogCat();
    }

    @Test
    public void getCurrentRevision() {
        Truth.assertThat(DeployGate.getCurrentRevision()).isEqualTo(0);
    }

    @Test
    public void getDistributionUrl() {
        Truth.assertThat(DeployGate.getDistributionUrl()).isNull();
    }

    @Test
    public void getDistributionId() {
        Truth.assertThat(DeployGate.getDistributionId()).isNull();
    }

    @Test
    public void getDistributionTitle() {
        Truth.assertThat(DeployGate.getDistributionTitle()).isNull();
    }

    @Test
    public void getDeployGateVersionCode() {
        Truth.assertThat(DeployGate.getDeployGateVersionCode()).isEqualTo(0);
    }

    @Test
    public void getDeployGateLongVersionCode() {
        Truth.assertThat(DeployGate.getDeployGateLongVersionCode()).isEqualTo(0);
    }

    @Test
    public void hasUpdate() {
        Truth.assertThat(DeployGate.hasUpdate()).isFalse();
    }

    @Test
    public void getUpdateRevision() {
        Truth.assertThat(DeployGate.getUpdateRevision()).isEqualTo(0);
    }

    @Test
    public void getUpdateVersionCode() {
        Truth.assertThat(DeployGate.getUpdateVersionCode()).isEqualTo(0);
    }

    @Test
    public void getUpdateVersionName() {
        Truth.assertThat(DeployGate.getUpdateVersionName()).isNull();
    }

    @Test
    public void getUpdateMessage() {
        Truth.assertThat(DeployGate.getUpdateMessage()).isNull();
    }

    @Test
    public void installUpdate() {
        DeployGate.installUpdate();
    }

    @Test
    public void openComments() {
        DeployGate.openComments();
    }

    @Test
    public void composeComment() {
        DeployGate.composeComment();
    }

    @Test
    public void composeComment__String() {
        DeployGate.composeComment("defaultComment");
    }

    @Test
    public void getDistributionUserName() {
        Truth.assertThat(DeployGate.getDistributionUserName()).isNull();
    }

    @Test
    public void getBuildEnvironment() {
        Truth.assertThat(DeployGate.getBuildEnvironment()).isNotNull();
    }

    @Test
    public void getRuntimeExtra() {
        Truth.assertThat(DeployGate.getRuntimeExtra()).isNotNull();
    }
}
