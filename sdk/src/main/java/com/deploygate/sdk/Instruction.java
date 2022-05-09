package com.deploygate.sdk;

import android.os.Bundle;

import com.deploygate.service.DeployGateEvent;

abstract class Instruction {
    public final String gid;
    public final String cid;

    Instruction(String gid) {
        this.gid = gid;
        this.cid = ClientId.generate();
    }

    /**
     * put values into the given extras
     */
    abstract void applyValues(Bundle extras);

    /**
     * @return a bundle to send to the client service
     */
    final Bundle toExtras() {
        Bundle extras = new Bundle();
        if (gid != null) {
            extras.putString(DeployGateEvent.EXTRA_INSTRUCTION_GROUP_ID, gid);
        }
        extras.putString(DeployGateEvent.EXTRA_CID, cid);
        applyValues(extras);
        return extras;
    }
}
