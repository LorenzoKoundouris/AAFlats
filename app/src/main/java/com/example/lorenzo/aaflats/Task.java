package com.example.lorenzo.aaflats;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lorenzo on 10/02/2016.
 */
public class Task implements Parcelable{
    private String title;
    private String description;
    private int priority; //Green: 2130837587 | Orange: 2130837589 | Red: 2130837578
    private boolean status;

    String p = String.valueOf(this.priority);
    String s = Boolean.toString(this.status);

    public Task(){

    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.title,
                this.description, this.p, this.s});
    }

    public Task(Parcel in){
        String[] parcelData = new String[4];

        in.readStringArray(parcelData);
        this.title = parcelData[0];
        this.description = parcelData[1];
        this.p = parcelData[2];
        this.s = parcelData[3];
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
}
