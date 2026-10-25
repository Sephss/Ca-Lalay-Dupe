package com.example.ca_lalay.model;

public class    UserModel {
    String firstname, lastname, email, contactNum, uid, role, imageUrl, status, timestamp, suspendedReason;

    public UserModel() {

    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNum() {
        return contactNum;
    }

    public void setContactNum(String contactNum) {
        this.contactNum = contactNum;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSuspendedReason() {
        return suspendedReason;
    }

    public void setSuspendedReason(String suspendedReason) {
        this.suspendedReason = suspendedReason;
    }

    public UserModel(String firstname, String lastname, String email, String contactNum, String uid, String role, String imageUrl, String status, String timestamp, String suspendedReason) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.contactNum = contactNum;
        this.uid = uid;
        this.role = role;
        this.imageUrl = imageUrl;
        this.status = status;
        this.timestamp = timestamp;
        this.suspendedReason = suspendedReason;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
