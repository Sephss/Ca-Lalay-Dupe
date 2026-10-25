package com.example.ca_lalay.firebase.callback;

public interface StatusCallback {
    void onSuccess(String status);
    void onFailure(String message);
}
