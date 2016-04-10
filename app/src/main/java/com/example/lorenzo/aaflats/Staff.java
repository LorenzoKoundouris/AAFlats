package com.example.lorenzo.aaflats;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lorenzo on 04/04/2016.
 */
public class Staff implements Parcelable {

    private String forename;
    private String surname;
    private String username;
    private String password;
    private String staffKey;

    public Staff() {
    }

    protected Staff(Parcel in) {
        forename = in.readString();
        surname = in.readString();
        username = in.readString();
        password = in.readString();
        staffKey = in.readString();
    }

    public static final Creator<Staff> CREATOR = new Creator<Staff>() {
        @Override
        public Staff createFromParcel(Parcel in) {
            return new Staff(in);
        }

        @Override
        public Staff[] newArray(int size) {
            return new Staff[size];
        }
    };

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStaffKey() {
        return staffKey;
    }

    public void setStaffKey(String staffKey) {
        this.staffKey = staffKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(forename);
        dest.writeString(surname);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(staffKey);
    }
}
