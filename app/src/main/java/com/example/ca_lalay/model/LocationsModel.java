package com.example.ca_lalay.model;

public class LocationsModel {
    double latitude;
    double longitude;
    String locationName, uid, shopImage, shopName, role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocationsModel (double latitude, double longitude, String locationName, String uid, String shopImage, String shopName, String role) {
        this.latitude  = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.uid = uid;
        this.shopImage = shopImage;
        this.shopName = shopName;
        this.role = role;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getShopImage() {
        return shopImage;
    }

    public void setShopImage(String shopImage) {
        this.shopImage = shopImage;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public LocationsModel() {

    }
}
