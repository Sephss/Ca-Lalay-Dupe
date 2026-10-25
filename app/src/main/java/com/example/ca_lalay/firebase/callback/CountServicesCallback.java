package com.example.ca_lalay.firebase.callback;

public interface CountServicesCallback {
    void onSuccess(int count);
    void onFailure(String message);
}
