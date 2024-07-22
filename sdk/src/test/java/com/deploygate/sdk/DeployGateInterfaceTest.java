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
    public void putBuildEnvironmentValue__string() {
        DeployGate.putBuildEnvironmentValue("key", "value");
    }

    @Test
    public void putBuildEnvironmentValue__int() {
        DeployGate.putBuildEnvironmentValue("key", 1);
    }

    @Test
    public void putBuildEnvironmentValue__float() {
        DeployGate.putBuildEnvironmentValue("key", 1.0f);
    }

    @Test
    public void putBuildEnvironmentValue__double() {
        DeployGate.putBuildEnvironmentValue("key", 1.0);
    }

    @Test
    public void putBuildEnvironmentValue__long() {
        DeployGate.putBuildEnvironmentValue("key", 1L);
    }

    @Test
    public void removeBuildEnvironmentValue() {
        DeployGate.removeBuildEnvironmentValue("key");
    }

    @Test
    public void removeAllBuildEnvironmentValues() {
        DeployGate.removeAllBuildEnvironmentValues();
    }

    @Test
    public void putRuntimeExtraValue__string() {
        DeployGate.putRuntimeExtraValue("key", "value");
    }

    @Test
    public void putRuntimeExtrasValue__int() {
        DeployGate.putRuntimeExtraValue("key", 1);
    }

    @Test
    public void putRuntimeExtrasValue__float() {
        DeployGate.putRuntimeExtraValue("key", 1.0f);
    }

    @Test
    public void putRuntimeExtrasValue__double() {
        DeployGate.putRuntimeExtraValue("key", 1.0);
    }

    @Test
    public void putRuntimeExtrasValue__long() {
        DeployGate.putRuntimeExtraValue("key", 1L);
    }

    @Test
    public void removeRuntimeExtrasValue() {
        DeployGate.removeRuntimeExtraValue("key");
    }

    @Test
    public void removeAllRuntimeExtraValues() {
        DeployGate.removeAllRuntimeExtraValues();
    }

    @Test
    public void putCustomValues() {
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid", "value")).isFalse();

        DeployGate.install(app);

        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid", "value")).isTrue();
    }

    @Test
    public void putCustomValues__keyPattern() {
        DeployGate.install(app);

        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid_underscore", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid_1_number", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("min", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("ng", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("true", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("false", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("null", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("invalid-hyphen", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("invalid#sharp", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("invalid$dollar", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("invalid.dot", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("invalid!bang", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("invalid*glob", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("invalidUpperCase", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("12345", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("1_invalid_begin_number", "value")).isFalse();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid_key_with_length_under_32", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("invalid_key_with_length_over_32_characters", "value")).isFalse();

        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid_underscore", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid_1_number", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("min", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("ng", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("true", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("false", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("null", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("invalid-hyphen", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("invalid#sharp", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("invalid$dollar", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("invalid.dot", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("invalid!bang", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("invalid*glob", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("invalidUpperCase", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("12345", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("1_invalid_begin_number", "value")).isFalse();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid_key_with_length_under_32", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("invalid_key_with_length_over_32_characters", "value")).isFalse();
    }

    @Test
    public void putCustomValues__valuePattern() {
        DeployGate.install(app);

        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid_string", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid_int", 1)).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid_long", 1L)).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid_float", 1.1f)).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid_double", 1.1)).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("valid_boolean", true)).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("invalid_too_long_string", "this is too long string value. we cannot accept value if size over 64.")).isFalse();

        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid_string", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid_int", 1)).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid_long", 1L)).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid_float", 1.1f)).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid_double", 1.1)).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("valid_boolean", true)).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("invalid_too_long_string", "this is too long string value. we cannot accept value if size over 64.")).isFalse();
    }

    @Test
    public void setBuildEnvironment__maxSize() {
        DeployGate.install(app);
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key1", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key2", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key3", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key4", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key5", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key6", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key7", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key8", "value")).isTrue();

        // allow to overwrite
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key1", "value2")).isTrue();
        // not allow to put value with new key because of max size
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key9", "value")).isFalse();

        DeployGate.removeBuildEnvironmentValue("key8");

        // allow to put value with new key after remove exists key
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key9", "value")).isTrue();
        // allow to overwrite
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key1", "value3")).isTrue();
        // not allow to put value with new key because of max size
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key10", "value")).isFalse();

        DeployGate.removeAllBuildEnvironmentValues();

        // allow to put value less than max size
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key1", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key2", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key3", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key4", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key5", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key6", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key7", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key8", "value")).isTrue();
        Truth.assertThat(DeployGate.putBuildEnvironmentValue("key9", "value")).isFalse();
    }

    @Test
    public void setRuntimeExtra__maxSize() {
        DeployGate.install(app);
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key1", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key2", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key3", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key4", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key5", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key6", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key7", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key8", "value")).isTrue();

        // allow to overwrite
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key1", "value2")).isTrue();
        // not allow to put value with new key because of max size
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key9", "value")).isFalse();

        DeployGate.removeRuntimeExtraValue("key8");

        // allow to put value with new key after remove exists key
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key9", "value")).isTrue();
        // allow to overwrite
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key1", "value3")).isTrue();
        // not allow to put value with new key because of max size
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key10", "value")).isFalse();

        DeployGate.removeAllRuntimeExtraValues();

        // allow to put value less than max size
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key1", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key2", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key3", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key4", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key5", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key6", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key7", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key8", "value")).isTrue();
        Truth.assertThat(DeployGate.putRuntimeExtraValue("key9", "value")).isFalse();
    }
}
