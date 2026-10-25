package com.example.ca_lalay.firebase.callback;

public interface CancelBookingCallback {
    void onSuccess(String message);
    void onFailure(String failed);
}
