package com.example.ca_lalay.firebase.callback;

public interface BillNumberCallback {
    void onSuccess(String billNumber);
    void onFailure(String error);
}
