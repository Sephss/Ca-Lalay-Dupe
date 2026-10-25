package com.example.ca_lalay.model;

public class ShopOwnerModel {
    String shopName, verificationImage1, verificationImage2, verificationImage3, email, contactNum, uid, role, location, rating,
            status, shopImage, shopOpenAt, shopClosedAt, shopAbout, overallRating, totalReviews, fiveStarCount, fourStarCount, threeStarCount, twoStarCount, oneStarCount, timestamp, dateApproved, dateRejected, rejectedReason, suspendedReason, shopSlot;

    double latitude, longitude;
    public ShopOwnerModel() {

    }


    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getVerificationImage1() {
        return verificationImage1;
    }

    public void setVerificationImage1(String verificationImage1) {
        this.verificationImage1 = verificationImage1;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShopImage() {
        return shopImage;
    }

    public void setShopImage(String shopImage) {
        this.shopImage = shopImage;
    }

    public String getShopOpenAt() {
        return shopOpenAt;
    }

    public void setShopOpenAt(String shopOpenAt) {
        this.shopOpenAt = shopOpenAt;
    }

    public String getShopAbout() {
        return shopAbout;
    }

    public void setShopAbout(String shopAbout) {
        this.shopAbout = shopAbout;
    }

    public String getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(String overallRating) {
        this.overallRating = overallRating;
    }

    public String getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(String totalReviews) {
        this.totalReviews = totalReviews;
    }

    public String getFiveStarCount() {
        return fiveStarCount;
    }

    public void setFiveStarCount(String fiveStarCount) {
        this.fiveStarCount = fiveStarCount;
    }

    public String getFourStarCount() {
        return fourStarCount;
    }

    public void setFourStarCount(String fourStarCount) {
        this.fourStarCount = fourStarCount;
    }

    public String getThreeStarCount() {
        return threeStarCount;
    }

    public void setThreeStarCount(String threeStarCount) {
        this.threeStarCount = threeStarCount;
    }

    public String getTwoStarCount() {
        return twoStarCount;
    }

    public void setTwoStarCount(String twoStarCount) {
        this.twoStarCount = twoStarCount;
    }

    public String getOneStarCount() {
        return oneStarCount;
    }

    public void setOneStarCount(String oneStarCount) {
        this.oneStarCount = oneStarCount;
    }

    public String getShopClosedAt() {
        return shopClosedAt;
    }

    public void setShopClosedAt(String shopClosedAt) {
        this.shopClosedAt = shopClosedAt;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateApproved() {
        return dateApproved;
    }

    public void setDateApproved(String dateApproved) {
        this.dateApproved = dateApproved;
    }

    public String getDateRejected() {
        return dateRejected;
    }

    public void setDateRejected(String dateRejected) {
        this.dateRejected = dateRejected;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public String getSuspendedReason() {
        return suspendedReason;
    }

    public void setSuspendedReason(String suspendedReason) {
        this.suspendedReason = suspendedReason;
    }

    public String getVerificationImage2() {
        return verificationImage2;
    }

    public void setVerificationImage2(String verificationImage2) {
        this.verificationImage2 = verificationImage2;
    }

    public String getVerificationImage3() {
        return verificationImage3;
    }

    public void setVerificationImage3(String verificationImage3) {
        this.verificationImage3 = verificationImage3;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getShopSlot() {
        return shopSlot;
    }

    public void setShopSlot(String shopSlot) {
        this.shopSlot = shopSlot;
    }

    public ShopOwnerModel(String shopName, String verificationImage1, String verificationImage2, String verificationImage3, String email, String contactNum, String uid, String role, String location, String rating, String status, String shopImage, String shopOpenAt, String shopClosedAt, String shopAbout, String overallRating, String totalReviews, String fiveStarCount, String fourStarCount, String threeStarCount, String twoStarCount, String oneStarCount, String timestamp, String dateApproved, String dateRejected, String rejectedReason, String suspendedReason, double latitude, double longitude, String shopSlot) {
        this.shopName = shopName;
        this.verificationImage1 = verificationImage1;
        this.verificationImage2 = verificationImage2;
        this.verificationImage3 = verificationImage3;
        this.email = email;
        this.contactNum = contactNum;
        this.uid = uid;
        this.role = role;
        this.location = location;
        this.rating = rating;
        this.status = status;
        this.shopImage = shopImage;
        this.shopOpenAt = shopOpenAt;
        this.shopClosedAt = shopClosedAt;
        this.shopAbout = shopAbout;
        this.overallRating = overallRating;
        this.totalReviews = totalReviews;
        this.fiveStarCount = fiveStarCount;
        this.fourStarCount = fourStarCount;
        this.threeStarCount= threeStarCount;
        this.twoStarCount = twoStarCount;
        this.oneStarCount = oneStarCount;
        this.timestamp = timestamp;
        this.dateApproved = dateApproved;
        this.dateRejected = dateRejected;
        this.rejectedReason = rejectedReason;
        this.suspendedReason = suspendedReason;
        this.latitude = latitude;
        this.longitude = longitude;
        this.shopSlot = shopSlot;
    }
}
