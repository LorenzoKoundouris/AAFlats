package com.example.lorenzo.aaflats;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Lorenzo on 15/02/2016.
 */
public class Flat implements Parcelable{
    private String tenant; // private Tenant tenant;
    private String pendingTask; //private Task pendingTask;
    private String notes;
    private String postcode;
    private String addressLine1;
    private String flatNum;
    private String flatKey;

    public Flat(){}

    protected Flat(Parcel in) {
        tenant = in.readString();
        pendingTask = in.readString();// (ArrayList<String>) in.readSerializable();
        notes = in.readString();
        postcode = in.readString();
        addressLine1 = in.readString();
        flatNum = in.readString();
        flatKey = in.readString();
    }

    public static final Creator<Flat> CREATOR = new Creator<Flat>() {
        @Override
        public Flat createFromParcel(Parcel in) {
            return new Flat(in);
        }

        @Override
        public Flat[] newArray(int size) {
            return new Flat[size];
        }
    };

    public String getPendingTask() {
        return pendingTask;
    }

    public void setPendingTask(String pendingTask) {
        this.pendingTask = pendingTask;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getFlatNum() {
        return flatNum;
    }

    public void setFlatNum(String flatNum) {
        this.flatNum = flatNum;
    }

    public String getFlatKey() {
        return flatKey;
    }

    public void setFlatKey(String flatKey) {
        this.flatKey = flatKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tenant);
        dest.writeString(pendingTask); //writeSerializable
        dest.writeString(notes);
        dest.writeString(postcode);
        dest.writeString(addressLine1);
        dest.writeString(flatNum);
        dest.writeString(flatKey);
    }
}
