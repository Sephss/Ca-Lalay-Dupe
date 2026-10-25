package com.example.ca_lalay.firebase.callback;

public interface UpdateBookingCallback {
    void onSuccess();

    void onFailure(String errorMessage);
}
