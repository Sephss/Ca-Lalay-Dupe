package com.example.ca_lalay.firebase.callback;

public interface CarServicesCallback {
    void onSuccess(String success, String serviceId, String serviceProviderId);
    void onFailure(String failed);

}
