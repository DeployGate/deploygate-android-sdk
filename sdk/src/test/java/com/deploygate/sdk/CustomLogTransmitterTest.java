package com.deploygate.sdk;

import android.os.Bundle;
import android.os.RemoteException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.deploygate.service.DeployGateEvent;
import com.deploygate.service.IDeployGateSdkService;
import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;
import org.robolectric.Shadows;
import org.robolectric.annotation.LooperMode;

import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class CustomLogTransmitterTest {

    private static final String PACKAGE_NAME = "com.deploygate.sample";

    @Mock
    private IDeployGateSdkService service;

    private CustomLogTransmitter customLogTransmitter;

    private CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().build();

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);

        customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);
    }

    @Test(timeout = 3000L)
    public void check_buffer_size_works() throws RemoteException {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().setBufferSize(5).build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            customLogTransmitter.transmit("type", String.valueOf(i));
        }

        customLogTransmitter.connect(service);

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        VerificationMode once = Mockito.times(1);

        for (int i = 5; i < 10; i++) {
            Mockito.verify(service, once).sendEvent(Mockito.eq(PACKAGE_NAME), Mockito.eq(DeployGateEvent.ACTION_SEND_CUSTOM_LOG), BundleMatcher.eq(createLogExtra("type", String.valueOf(i))));
        }
    }

    @Test(timeout = 3000L)
    public void transmit_works_regardless_of_service() {
        CustomLogConfiguration configuration = new CustomLogConfiguration.Builder().setBufferSize(8).build();
        CustomLogTransmitter customLogTransmitter = new CustomLogTransmitter(PACKAGE_NAME, configuration);

        for (int i = 0; i < 10; i++) {
            customLogTransmitter.transmit("type", String.valueOf(i));
        }

        Shadows.shadowOf(customLogTransmitter.getLooper()).idle();

        Truth.assertThat(customLogTransmitter.getPendingCount()).isEqualTo(8);
    }

    private static Bundle createLogExtra(
            String type,
            String body
    ) {
        Bundle bundle = new Bundle();
        bundle.putString("logType", type);
        bundle.putString("log", body);
        return bundle;
    }
}
