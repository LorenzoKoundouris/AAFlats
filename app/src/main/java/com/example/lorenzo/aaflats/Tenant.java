package com.example.lorenzo.aaflats;

import java.util.Date;

/**
 * Created by Lorenzo on 15/02/2016.
 */
public class Tenant {

    private String tenantForename;
    private String tenantMiddlename;
    private String tenantSurname;
    private long tenantDoB;
    private int tenantTelephone;
    private String tenantEmail;
    private Property tenantProperty;
    private long tenantContractStart;
    private long tenantContractEnd;

    public Tenant() {
    }

    public String getTenantForename() {
        return tenantForename;
    }

    public void setTenantForename(String tenantForename) {
        this.tenantForename = tenantForename;
    }

    public String getTenantMiddlename() {
        return tenantMiddlename;
    }

    public void setTenantMiddlename(String tenantMiddlename) {
        this.tenantMiddlename = tenantMiddlename;
    }

    public String getTenantSurname() {
        return tenantSurname;
    }

    public void setTenantSurname(String tenantSurname) {
        this.tenantSurname = tenantSurname;
    }

    public long getTenantDoB() {
        return tenantDoB;
    }

    public void setTenantDoB(long tenantDoB) {
        this.tenantDoB = tenantDoB;
    }

    public int getTenantTelephone() {
        return tenantTelephone;
    }

    public void setTenantTelephone(int tenantTelephone) {
        this.tenantTelephone = tenantTelephone;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public Property getTenantProperty() {
        return tenantProperty;
    }

    public void setTenantProperty(Property tenantProperty) {
        this.tenantProperty = tenantProperty;
    }

    public long getTenantContractStart() {
        return tenantContractStart;
    }

    public void setTenantContractStart(long tenantContractStart) {
        this.tenantContractStart = tenantContractStart;
    }

    public long getTenantContractEnd() {
        return tenantContractEnd;
    }

    public void setTenantContractEnd(long tenantContractEnd) {
        this.tenantContractEnd = tenantContractEnd;
    }
}
