package com.example.ca_lalay.firebase.callback;

public interface CountCallback {
    void onSuccess(int count);
    void onFailure(String error);
}