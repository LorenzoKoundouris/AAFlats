package com.example.lorenzo.aaflats;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lorenzo on 15/02/2016.
 */
public class Property implements Parcelable{
    private String addrline1;
    private String postcode;
    private String noOfFlats;
    private String notes;
    private String propertyKey;
//    private Flat flat;

    public Property(){}


    public String getAddrline1() {
        return addrline1;
    }

    public void setAddrline1(String addrline1) {
        this.addrline1 = addrline1;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getNoOfFlats() {
        return noOfFlats;
    }

    public void setNoOfFlats(String noOfFlats) {
        this.noOfFlats = noOfFlats;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    protected Property(Parcel in) {
        addrline1 = in.readString();
        postcode = in.readString();
        noOfFlats = in.readString();
        notes = in.readString();
        propertyKey = in.readString();
    }

    public static Creator<Property> getCREATOR() {
        return CREATOR;
    }

    public static final Creator<Property> CREATOR = new Creator<Property>() {
        @Override
        public Property createFromParcel(Parcel in) {
            return new Property(in);
        }

        @Override
        public Property[] newArray(int size) {
            return new Property[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(addrline1);
        dest.writeString(postcode);
        dest.writeString(noOfFlats);
        dest.writeString(notes);
        dest.writeString(propertyKey);
    }

}
