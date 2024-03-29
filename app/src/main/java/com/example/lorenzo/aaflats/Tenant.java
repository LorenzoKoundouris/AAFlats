package com.example.lorenzo.aaflats;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lorenzo on 15/02/2016.
 */
public class Tenant implements Parcelable{

    private String contractEnd;
    private String contractStart;
    private boolean currentTenant;
    private String dob;
    private String email;
    private String forename;
    private String middlename;
    private String notes;
    private String property;
    private String surname;
    private String telephone;
    private String tenantKey;

    public Tenant() {
    }

    public Tenant(Tenant cloneTenant) {
        this.contractEnd = cloneTenant.getContractEnd();
        this.contractStart = cloneTenant.getContractStart();
        this.currentTenant = cloneTenant.isCurrentTenant();
        this.dob = cloneTenant.getDob();
        this.email = cloneTenant.getEmail();
        this.forename = cloneTenant.getForename();
        this.middlename = cloneTenant.getMiddlename();
        this.notes = cloneTenant.getNotes();
        this.property = cloneTenant.getProperty();
        this.surname = cloneTenant.getSurname();
        this.telephone = cloneTenant.getTelephone();
        this.tenantKey = cloneTenant.getTenantKey();
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getContractStart() {
        return contractStart;
    }

    public void setContractStart(String contractStart) {
        this.contractStart = contractStart;
    }

    public String getContractEnd() {
        return contractEnd;
    }

    public void setContractEnd(String contractEnd) {
        this.contractEnd = contractEnd;
    }

    public boolean isCurrentTenant() {
        return currentTenant;
    }

    public void setCurrentTenant(boolean currentTenant) {
        this.currentTenant = currentTenant;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTenantKey() {
        return tenantKey;
    }

    public void setTenantKey(String tenantKey) {
        this.tenantKey = tenantKey;
    }

    protected Tenant(Parcel in){
        contractEnd = in.readString();
        contractStart = in.readString();
        currentTenant = in.readByte() != 0x00;
        dob = in.readString();
        email = in.readString();
        forename = in.readString();
        middlename = in.readString();
        property = in.readString();
        surname = in.readString();
        telephone = in.readString();
        notes = in.readString();
        tenantKey = in.readString();
    }

    public static Creator<Tenant> getCREATOR() {
        return CREATOR;
    }

    public static final Creator<Tenant> CREATOR = new Creator<Tenant>() {
        @Override
        public Tenant createFromParcel(Parcel in) {
            return new Tenant(in);
        }

        @Override
        public Tenant[] newArray(int size) {
            return new Tenant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contractEnd);
        dest.writeString(contractStart);
        dest.writeByte((byte) (currentTenant ? 0x01 : 0x00));
        dest.writeString(dob);
        dest.writeString(email);
        dest.writeString(forename);
        dest.writeString(middlename);
        dest.writeString(property);
        dest.writeString(surname);
        dest.writeString(telephone);
        dest.writeString(notes);
        dest.writeString(tenantKey);
    }
}
