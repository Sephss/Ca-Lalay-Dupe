package com.example.ca_lalay.firebase.callback;

public interface OnStatusUpdateCallback {
    void onSuccess(String message);
    void onFailure(String error);
}