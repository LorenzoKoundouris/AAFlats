package com.example.lorenzo.aaflats;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Lorenzo on 10/02/2016.
 */
public class Task implements Parcelable {
    private String title;
    private String description;
    private String priority; //Green: 2130837587 | Orange: 2130837589 | Red: 2130837578
    private boolean status;
    private String report;
    private String property;
    private String taskKey;
    private String completionTimestamp;
    private String targetDate;
    private String notes;
    private String creator;
    private String assignedStaff;
    private String completedBy;
    private boolean staffNotified;


    public Task() {

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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public String getCompletionTimestamp() {
        return completionTimestamp;
    }

    public void setCompletionTimestamp(String completionTimestamp) {
        this.completionTimestamp = completionTimestamp;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(String assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    public boolean isStaffNotified() {
        return staffNotified;
    }

    public void setStaffNotified(boolean staffNotified) {
        this.staffNotified = staffNotified;
    }

    public Task(Parcel in) {
        title = in.readString();
        description = in.readString();
        priority = in.readString();
        status = in.readByte() != 0x00;
        report = in.readString();
        property = in.readString();
        taskKey = in.readString();
        completionTimestamp = in.readString();
        targetDate = in.readString();
        notes = in.readString();
        creator = in.readString();
        assignedStaff = in.readString();
        completedBy = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(priority);
        dest.writeByte((byte) (status ? 0x01 : 0x00));
        dest.writeString(report);
        dest.writeString(property);
        dest.writeString(taskKey);
        dest.writeString(completionTimestamp);
        dest.writeString(targetDate);
        dest.writeString(notes);
        dest.writeString(creator);
        dest.writeString(assignedStaff);
        dest.writeString(completedBy);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
}
