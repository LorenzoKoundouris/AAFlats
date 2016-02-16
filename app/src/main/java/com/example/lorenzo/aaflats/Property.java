package com.example.lorenzo.aaflats;

/**
 * Created by Lorenzo on 15/02/2016.
 */
public class Property {
    private String postcode;
    private int noOfFlats;
    private String notes;
//    private Flat flat;

    public Property(){}

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public int getNoOfFlats() {
        return noOfFlats;
    }

    public void setNoOfFlats(int noOfFlats) {
        this.noOfFlats = noOfFlats;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

//    public Flat getFlat() {
//        return flat;
//    }
//
//    public void setFlat(Flat flat) {
//        this.flat = flat;
//    }
}
