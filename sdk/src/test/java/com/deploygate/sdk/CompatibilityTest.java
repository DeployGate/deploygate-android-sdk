package com.deploygate.sdk;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class CompatibilityTest {

    @Test
    public void check_isUpdateMessageOfBuildSupported() {
        Truth.assertThat(Compatibility.ClientVersion.SUPPORT_UPDATE_MESSAGE_OF_BUILD).isEqualTo(39);

        try (MockedStatic<DeployGate> mocked = Mockito.mockStatic(DeployGate.class)) {
            mocked.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    DeployGate.getDeployGateVersionCode();
                }
            }).thenReturn(38, 39, 40);

            Truth.assertThat(Compatibility.isUpdateMessageOfBuildSupported()).isFalse();
            Truth.assertThat(Compatibility.isUpdateMessageOfBuildSupported()).isTrue();
            Truth.assertThat(Compatibility.isUpdateMessageOfBuildSupported()).isTrue();
        }
    }

    @Test
    public void check_isSerializedExceptionSupported() {
        Truth.assertThat(Compatibility.ClientVersion.SUPPORT_SERIALIZED_EXCEPTION).isEqualTo(42);

        try (MockedStatic<DeployGate> mocked = Mockito.mockStatic(DeployGate.class)) {
            mocked.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    DeployGate.getDeployGateVersionCode();
                }
            }).thenReturn(41, 42, 43);

            Truth.assertThat(Compatibility.isSerializedExceptionSupported()).isFalse();
            Truth.assertThat(Compatibility.isSerializedExceptionSupported()).isTrue();
            Truth.assertThat(Compatibility.isSerializedExceptionSupported()).isTrue();
        }
    }

    @Test
    public void check_isLogcatBundleSupported() {
        Truth.assertThat(Compatibility.ClientVersion.SUPPORT_LOGCAT_BUNDLE).isEqualTo(Integer.MAX_VALUE);

        try (MockedStatic<DeployGate> mocked = Mockito.mockStatic(DeployGate.class)) {
            mocked.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    DeployGate.getDeployGateVersionCode();
                }
            }).thenReturn(Integer.MAX_VALUE - 1, Integer.MAX_VALUE, Integer.MAX_VALUE);

            Truth.assertThat(Compatibility.isLogcatBundleSupported()).isFalse();
            Truth.assertThat(Compatibility.isLogcatBundleSupported()).isTrue();
            Truth.assertThat(Compatibility.isLogcatBundleSupported()).isTrue();
        }
    }
}
