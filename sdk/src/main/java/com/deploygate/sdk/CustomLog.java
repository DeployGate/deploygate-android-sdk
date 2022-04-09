package com.deploygate.sdk;

import android.os.Parcel;
import android.os.Parcelable;

class CustomLog implements Parcelable {
    public final String type;
    public final String body;

    CustomLog(
            String type,
            String body
    ) {
        this.type = type;
        this.body = body;
    }

    protected CustomLog(Parcel in) {
        type = in.readString();
        body = in.readString();
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
    }
}
