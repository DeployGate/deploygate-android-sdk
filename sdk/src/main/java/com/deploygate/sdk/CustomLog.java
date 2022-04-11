package com.deploygate.sdk;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.deploygate.service.DeployGateEvent;

class CustomLog implements Parcelable {
    public final String type;
    public final String body;
    private int retryCount;

    CustomLog(
            String type,
            String body
    ) {
        this.type = type;
        this.body = body;
        this.retryCount = 0;
    }

    protected CustomLog(Parcel in) {
        type = in.readString();
        body = in.readString();
        retryCount = in.readInt();
    }

    /**
     * @return the number of current attempts
     */
    int getAndIncrementRetryCount() {
        return retryCount++;
    }

    /**
     * @return a bundle to send to the client service
     */
    Bundle toExtras() {
        Bundle extras = new Bundle();
        extras.putSerializable(DeployGateEvent.EXTRA_LOG, body);
        extras.putSerializable(DeployGateEvent.EXTRA_LOG_TYPE, type);
        return extras;
    }

    public static final Creator<CustomLog> CREATOR = new Creator<CustomLog>() {
        @Override
        public CustomLog createFromParcel(Parcel in) {
            return new CustomLog(in);
        }

        @Override
        public CustomLog[] newArray(int size) {
            return new CustomLog[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags
    ) {
        dest.writeString(type);
        dest.writeString(body);
        dest.writeInt(retryCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomLog customLog = (CustomLog) o;

        if (!type.equals(customLog.type)) {
            return false;
        }
        return body.equals(customLog.body);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }
}
