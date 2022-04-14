package com.deploygate.service;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.google.common.truth.Truth;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FakeDeployGateClientService implements IDeployGateSdkService {
    public static final String ACTION_INIT = "com.deploygate.sdk.fake.INIT";

    private final String packageName;
    private final Map<String, List<Bundle>> eventExtrasMap;

    public FakeDeployGateClientService(
            String packageName
    ) {
        this.packageName = packageName;
        this.eventExtrasMap = new HashMap<>();

        for (final Field field : DeployGateEvent.class.getFields()) {
            final String name = field.getName();

            if (name.startsWith("ACTION_")) {
                try {
                    eventExtrasMap.put((String) field.get(null), new ArrayList<Bundle>());
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(String.format(Locale.US, "cannot access %s", name), e);
                }
            }
        }
    }

    public List<Bundle> getEventExtraList(String action) {
        return Collections.unmodifiableList(eventExtrasMap.getOrDefault(action, new ArrayList<Bundle>()));
    }

    @Override
    public void init(
            IDeployGateSdkServiceCallback callback,
            String packageName,
            Bundle extras
    ) throws RemoteException {
        Truth.assertThat(packageName).isEqualTo(this.packageName);
        recordEvent(ACTION_INIT, extras);
    }

    @Override
    public void sendEvent(
            String packageName,
            String action,
            Bundle extras
    ) throws RemoteException {
        Truth.assertThat(packageName).isEqualTo(this.packageName);
        recordEvent(action, extras);
    }

    @Override
    public IBinder asBinder() {
        return null;
    }

    private void recordEvent(
            String action,
            Bundle extras
    ) {
        List<Bundle> extraList = Objects.requireNonNull(eventExtrasMap.get(action), String.format(Locale.US, "%s is an unknown action", action));
        extraList.add(extras);
    }
}
