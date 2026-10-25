package com.example.ca_lalay.firebase.callback;

import com.google.firebase.auth.FirebaseUser;

public interface AuthResultCallback {
    void onSuccess(FirebaseUser user, String role);
    void onFailure (String failureMessage);

}
