package com.example.ca_lalay.firebase.callback;

public interface CustomerBookingCallback {
    void onSuccess(String message, String bookingId);
    void onFailure(String message);

}
