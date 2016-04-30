package com.example.lorenzo.aaflats;

/**
 * Created by Lorenzo on 08/04/2016.
 */
public class Notification {

    String objectID;
    String timestampSent;
    String type;

    public Notification(){

    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getTimestampSent() {
        return timestampSent;
    }

    public void setTimestampSent(String timestampSent) {
        this.timestampSent = timestampSent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
