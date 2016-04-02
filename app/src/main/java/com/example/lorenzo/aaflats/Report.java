package com.example.lorenzo.aaflats;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lorenzo on 14/02/2016.
 */
public class Report implements Parcelable{
    private String content;
    private String sender;
    private String status;
    private String timestamp;
    private String type;

    public Report(){}

    protected Report(Parcel in) {
        content = in.readString();
        sender = in.readString();
        status = in.readString();
        timestamp = in.readString();
        type = in.readString();
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeString(sender);
        dest.writeString(status);
        dest.writeString(timestamp);
        dest.writeString(type);
    }
}
