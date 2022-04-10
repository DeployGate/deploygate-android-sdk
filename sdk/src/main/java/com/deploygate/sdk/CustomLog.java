package com.deploygate.sdk;

import android.os.Parcel;
import android.os.Parcelable;

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
}
