package com.example.ca_lalay.model;

public class OnlinePaymentDetailsModel {
    String uid;
    String updatedAt;
    String details;

    public OnlinePaymentDetailsModel() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public OnlinePaymentDetailsModel(String uid, String updatedAt, String details) {
        this.uid = uid;
        this.updatedAt = updatedAt;
        this.details = details;
    }
}
