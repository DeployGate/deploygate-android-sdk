package com.deploygate.sdk;

import android.os.Bundle;

import com.deploygate.service.DeployGateEvent;

class CreateOneshotLogcatRequest {
    public final String cid;

    CreateOneshotLogcatRequest() {cid = ClientId.generate();}

    Bundle toExtras() {
        Bundle extras = new Bundle();

        extras.putString(DeployGateEvent.EXTRA_CID, cid);

        return extras;
    }
}
