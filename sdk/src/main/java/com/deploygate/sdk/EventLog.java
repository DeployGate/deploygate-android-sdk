package com.deploygate.sdk;

import android.os.Parcel;
import android.os.Parcelable;

class EventLog implements Parcelable {
    public final String type;
    public final String body;

    EventLog(
            String type,
            String body
    ) {
        this.type = type;
        this.body = body;
    }

    protected EventLog(Parcel in) {
        type = in.readString();
        body = in.readString();
    }

    public static final Creator<EventLog> CREATOR = new Creator<EventLog>() {
        @Override
        public EventLog createFromParcel(Parcel in) {
            return new EventLog(in);
        }

        @Override
        public EventLog[] newArray(int size) {
            return new EventLog[size];
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
