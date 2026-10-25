package com.example.ca_lalay.firebase.callback;

public interface SignupCallback {
    void onSuccess(String success, String imageUrl);
    void onFailure (String failureMessage);
}
