package com.deploygate.sdk;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowPackageManager;

@RunWith(AndroidJUnit4.class)
public class DeployGateClientTest {
    @NonNull
    Application app;

    @Before
    public void setUp() {
        app = getApplicationContext();
    }

    @Test
    public void return_defaults_unless_installed() {
        DeployGateClient client = new DeployGateClient(app, "com.deploygate");
        Truth.assertThat(client.isInstalled).isFalse();
        Truth.assertThat(client.versionCode).isEqualTo(0);

        for (final Compatibility compatibility : Compatibility.values()) {
            Truth.assertThat(client.isSupported(compatibility)).isFalse();
        }
    }

    @Test
    public void return_defaults_if_signature_is_not_matched() {
        DeployGateClient client = installDeployGate(100, false, Compatibility.all());

        Truth.assertThat(client.isInstalled).isFalse();
        Truth.assertThat(client.versionCode).isEqualTo(0);

        for (final Compatibility compatibility : Compatibility.values()) {
            Truth.assertThat(client.isSupported(compatibility)).isFalse();
        }
    }

    @Test
    public void check_features_if_client_version_is_38() {
        DeployGateClient client = installDeployGate(Compatibility.ClientVersion.SUPPORT_UPDATE_MESSAGE_OF_BUILD - 1, true, -1);

        for (final Compatibility compatibility : Compatibility.values()) {
            Truth.assertThat(client.isSupported(compatibility)).isFalse();
        }
    }

    @Test
    public void check_features_if_client_version_is_39() {
        DeployGateClient client = installDeployGate(Compatibility.ClientVersion.SUPPORT_UPDATE_MESSAGE_OF_BUILD, true, -1);

        for (final Compatibility compatibility : Compatibility.values()) {
            switch (compatibility) {
                case UPDATE_MESSAGE_OF_BUILD: {
                    Truth.assertThat(client.isSupported(compatibility)).isTrue();
                    break;
                }
                default: {
                    Truth.assertThat(client.isSupported(compatibility)).isFalse();
                    break;
                }
            }
        }
    }

    @Test
    public void check_features_if_client_version_is_41() {
        DeployGateClient client = installDeployGate(Compatibility.ClientVersion.SUPPORT_SERIALIZED_EXCEPTION - 1, true, -1);

        for (final Compatibility compatibility : Compatibility.values()) {
            switch (compatibility) {
                case UPDATE_MESSAGE_OF_BUILD: {
                    Truth.assertThat(client.isSupported(compatibility)).isTrue();
                    break;
                }
                default: {
                    Truth.assertThat(client.isSupported(compatibility)).isFalse();
                    break;
                }
            }
        }
    }

    @Test
    public void check_features_if_client_version_is_42() {
        DeployGateClient client = installDeployGate(Compatibility.ClientVersion.SUPPORT_SERIALIZED_EXCEPTION, true, -1);

        for (final Compatibility compatibility : Compatibility.values()) {
            switch (compatibility) {
                case UPDATE_MESSAGE_OF_BUILD:
                case SERIALIZED_EXCEPTION: {
                    Truth.assertThat(client.isSupported(compatibility)).isTrue();
                    break;
                }
                default: {
                    Truth.assertThat(client.isSupported(compatibility)).isFalse();
                    break;
                }
            }
        }
    }

    @Test
    public void check_features_if_features_has_build_message() {
        DeployGateClient client = installDeployGate(100, true, 0b1);

        Truth.assertThat(client.isInstalled).isTrue();
        Truth.assertThat(client.versionCode).isEqualTo(100);

        for (final Compatibility compatibility : Compatibility.values()) {
            switch (compatibility) {
                case UPDATE_MESSAGE_OF_BUILD: {
                    Truth.assertThat(client.isSupported(compatibility)).isTrue();
                    break;
                }
                default: {
                    Truth.assertThat(client.isSupported(compatibility)).isFalse();
                    break;
                }
            }
        }
    }

    @Test
    public void check_features_if_features_has_serialized_exception() {
        DeployGateClient client = installDeployGate(100, true, 0b10);

        Truth.assertThat(client.isInstalled).isTrue();
        Truth.assertThat(client.versionCode).isEqualTo(100);

        for (final Compatibility compatibility : Compatibility.values()) {
            switch (compatibility) {
                case SERIALIZED_EXCEPTION: {
                    Truth.assertThat(client.isSupported(compatibility)).isTrue();
                    break;
                }
                default: {
                    Truth.assertThat(client.isSupported(compatibility)).isFalse();
                    break;
                }
            }
        }
    }

    @Test
    public void check_features_if_features_has_logcat_bundle() {
        DeployGateClient client = installDeployGate(100, true, 0b100);

        Truth.assertThat(client.isInstalled).isTrue();
        Truth.assertThat(client.versionCode).isEqualTo(100);

        for (final Compatibility compatibility : Compatibility.values()) {
            switch (compatibility) {
                case LOGCAT_BUNDLE: {
                    Truth.assertThat(client.isSupported(compatibility)).isTrue();
                    break;
                }
                default: {
                    Truth.assertThat(client.isSupported(compatibility)).isFalse();
                    break;
                }
            }
        }
    }

    @Test
    public void check_features_if_features_has_serialized_exception_and_logcat_bundle() {
        DeployGateClient client = installDeployGate(100, true, 0b110);

        Truth.assertThat(client.isInstalled).isTrue();
        Truth.assertThat(client.versionCode).isEqualTo(100);

        for (final Compatibility compatibility : Compatibility.values()) {
            switch (compatibility) {
                case SERIALIZED_EXCEPTION:
                case LOGCAT_BUNDLE: {
                    Truth.assertThat(client.isSupported(compatibility)).isTrue();
                    break;
                }
                default: {
                    Truth.assertThat(client.isSupported(compatibility)).isFalse();
                    break;
                }
            }
        }
    }

    private DeployGateClient installDeployGate(
            long versionCode,
            boolean matchSignature,
            int features
    ) {
        ShadowPackageManager packageManager = Shadows.shadowOf(app.getPackageManager());

        PackageInfo packageInfo = new PackageInfo();

        packageInfo.setLongVersionCode(versionCode);
        packageInfo.versionName = "1.0.0";
        packageInfo.packageName = "com.deploygate";

        if (Build.VERSION_CODES.P > Build.VERSION.SDK_INT) {
            packageInfo.signatures = new Signature[0];
        } else {
            packageInfo.signingInfo = new SigningInfo();
        }

        Bundle metaData = new Bundle();

        if (features > 0) {
            metaData.putInt("com.deploygate.features", features);
        }

        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.packageName = "com.deploygate";
        applicationInfo.metaData = metaData;

        packageInfo.applicationInfo = applicationInfo;

        packageManager.installPackage(packageInfo);

        try (MockedStatic<DeployGateClient> mocked = Mockito.mockStatic(DeployGateClient.class)) {
            mocked.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    DeployGateClient.checkSignature(Mockito.<Signature[]>any());
                }
            }).thenReturn(matchSignature);

            return new DeployGateClient(app, "com.deploygate");
        }
    }
}
