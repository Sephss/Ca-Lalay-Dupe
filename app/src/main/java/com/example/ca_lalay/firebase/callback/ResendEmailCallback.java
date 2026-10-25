package com.example.ca_lalay.firebase.callback;

import com.google.firebase.auth.FirebaseUser;

public interface ResendEmailCallback {
    void onSuccess(FirebaseUser user);
    void onFailure (String failureMessage);
}
