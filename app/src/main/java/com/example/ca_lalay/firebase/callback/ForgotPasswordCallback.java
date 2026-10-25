package com.example.ca_lalay.firebase.callback;

public interface ForgotPasswordCallback {
    void onSuccess(String message);
    void onFailure(String errorMessage);
}