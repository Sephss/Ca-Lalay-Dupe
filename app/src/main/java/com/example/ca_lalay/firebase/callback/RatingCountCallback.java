package com.example.ca_lalay.firebase.callback;
public interface RatingCountCallback {
    void onSuccess(int totalReviews);
    void onFailure(String message);
}