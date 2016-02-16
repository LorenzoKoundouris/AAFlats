package com.example.lorenzo.aaflats;

/**
 * Created by Lorenzo on 15/02/2016.
 */
public class Flat {
    private String tenant; // private Tenant tenant;
    private String pendingTask; //private Task pendingTask;
    private String notes;
    private String postcode;

    public Flat(){}

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
}
