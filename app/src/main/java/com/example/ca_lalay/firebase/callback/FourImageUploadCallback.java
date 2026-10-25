package com.example.ca_lalay.firebase.callback;

public interface FourImageUploadCallback {
    void onSuccess(String imageUrl1, String imageUrl2, String imageUrl3, String imageUrl4);
    void onFailure(String error);
}