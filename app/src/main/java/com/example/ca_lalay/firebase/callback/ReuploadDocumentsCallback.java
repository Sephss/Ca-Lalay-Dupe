package com.example.ca_lalay.firebase.callback;

public interface ReuploadDocumentsCallback {
    void onSuccess(String image1, String image2, String image3);
    void onFailure(String errorMessage);
}
