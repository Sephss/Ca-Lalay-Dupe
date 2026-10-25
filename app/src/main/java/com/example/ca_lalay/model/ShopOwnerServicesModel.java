package com.example.ca_lalay.model;

public class ShopOwnerServicesModel {
    String name;
    String shopName;
    String shopID;
    String shopContactNum;
    String shopLocation;
    String shopRatings;
    String shopImageUrl;
    String description;
    String imageUrl;
    String serviceId;
    String serviceProviderId;
    String category;
    String price;
    String durationMinutes;
    String isAvailable;
    String createdAt;
    String status;
    String rating;
    String reviewCount;
    String mediumPercentage;
    String largePercentage;
    String shopOpenAt;
    String shopClosedAt;

    public String getShopOpenAt() {
        return shopOpenAt;
    }

    public void setShopOpenAt(String shopOpenAt) {
        this.shopOpenAt = shopOpenAt;
    }

    public String getShopClosedAt() {
        return shopClosedAt;
    }

    public void setShopClosedAt(String shopClosedAt) {
        this.shopClosedAt = shopClosedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(String reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(String serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(String durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(String isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopContactNum() {
        return shopContactNum;
    }

    public void setShopContactNum(String shopContactNum) {
        this.shopContactNum = shopContactNum;
    }

    public String getShopLocation() {
        return shopLocation;
    }

    public void setShopLocation(String shopLocation) {
        this.shopLocation = shopLocation;
    }

    public String getShopRatings() {
        return shopRatings;
    }

    public void setShopRatings(String shopRatings) {
        this.shopRatings = shopRatings;
    }

    public String getShopImageUrl() {
        return shopImageUrl;
    }

    public void setShopImageUrl(String shopImageUrl) {
        this.shopImageUrl = shopImageUrl;
    }

    public String getMediumPercentage() {
        return mediumPercentage;
    }

    public void setMediumPercentage(String mediumPercentage) {
        this.mediumPercentage = mediumPercentage;
    }

    public String getLargePercentage() {
        return largePercentage;
    }

    public void setLargePercentage(String largePercentage) {
        this.largePercentage = largePercentage;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    public ShopOwnerServicesModel(String name, String shopName, String shopID, String shopContactNum, String shopLocation, String shopRatings, String shopImageUrl, String description, String imageUrl, String serviceId, String serviceProviderId, String category, String price, String durationMinutes, String isAvailable, String createdAt, String status, String rating, String reviewCount, String mediumPercentage, String largePercentage, String shopOpenAt, String shopClosedAt) {
        this.name = name;
        this.shopName = shopName;
        this.shopID = shopID;
        this.shopContactNum = shopContactNum;
        this.shopLocation = shopLocation;
        this.shopRatings = shopRatings;
        this.shopImageUrl = shopImageUrl;
        this.description = description;
        this.imageUrl = imageUrl;
        this.serviceId = serviceId;
        this.serviceProviderId = serviceProviderId;
        this.category = category;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
        this.status = status;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.mediumPercentage = mediumPercentage;
        this.largePercentage = largePercentage;
        this.shopOpenAt = shopOpenAt;
        this.shopClosedAt = shopClosedAt;
    }
    public ShopOwnerServicesModel() {

    }
}
