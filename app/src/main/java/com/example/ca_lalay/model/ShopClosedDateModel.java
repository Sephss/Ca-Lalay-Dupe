package com.example.ca_lalay.model;

public class ShopClosedDateModel {
    private String date; // yyyy-MM-dd, set manually from the Firebase key after fetch
    private String reason;
    private String createdAt;

    public ShopClosedDateModel() {
        // Required empty constructor for Firebase
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}