package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.UserModel;

public interface CustomerDataCallback {
    void onSuccess(String message, UserModel customerData);
    void onFailure(String message);

}
