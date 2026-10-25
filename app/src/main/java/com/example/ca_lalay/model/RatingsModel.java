package com.example.ca_lalay.model;

import android.media.Rating;

public class RatingsModel {
    String raterName, raterImage, raterID, ratingStar, ratingComment, dateRated, ratingsID, serviceID, serviceName, serviceProviderID, bookingID, timestamp, shopName, shopImage;

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopImage() {
        return shopImage;
    }

    public void setShopImage(String shopImage) {
        this.shopImage = shopImage;
    }

    public RatingsModel(String raterName, String raterImage, String raterID, String ratingStar, String ratingComment, String dateRated, String ratingsID, String serviceID, String serviceName, String serviceProviderID, String bookingID, String timestamp, String shopName, String shopImage) {
        this.raterName = raterName;
        this.raterImage = raterImage;
        this.raterID = raterID;
        this.ratingStar = ratingStar;
        this.ratingComment = ratingComment;
        this.dateRated = dateRated;
        this.ratingsID = ratingsID;
        this.serviceID = serviceID;
        this.serviceName = serviceName;
        this.serviceProviderID = serviceProviderID;
        this.bookingID = bookingID;
        this.timestamp = timestamp;
        this.shopName = shopName;
        this.shopImage = shopImage;
    }

    public String getRaterName() {
        return raterName;
    }

    public void setRaterName(String raterName) {
        this.raterName = raterName;
    }

    public String getRaterImage() {
        return raterImage;
    }

    public void setRaterImage(String raterImage) {
        this.raterImage = raterImage;
    }

    public String getRaterID() {
        return raterID;
    }

    public void setRaterID(String raterID) {
        this.raterID = raterID;
    }

    public String getRatingStar() {
        return ratingStar;
    }

    public void setRatingStar(String ratingStar) {
        this.ratingStar = ratingStar;
    }

    public String getRatingComment() {
        return ratingComment;
    }

    public void setRatingComment(String ratingComment) {
        this.ratingComment = ratingComment;
    }

    public String getDateRated() {
        return dateRated;
    }

    public void setDateRated(String dateRated) {
        this.dateRated = dateRated;
    }

    public String getRatingsID() {
        return ratingsID;
    }

    public void setRatingsID(String ratingsID) {
        this.ratingsID = ratingsID;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceProviderID() {
        return serviceProviderID;
    }

    public void setServiceProviderID(String serviceProviderID) {
        this.serviceProviderID = serviceProviderID;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public RatingsModel() {

    }
}
