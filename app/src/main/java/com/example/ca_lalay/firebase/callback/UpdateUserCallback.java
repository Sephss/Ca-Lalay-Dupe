package com.example.ca_lalay.firebase.callback;

public interface UpdateUserCallback {
    void onSuccess(String message);
    void onFailure(String message);
}
