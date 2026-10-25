package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ShopOwnerServicesModel;

import java.util.List;

public interface CarBrowserServiceCallback {
    void onSuccess(String success, String serviceId, String serviceProviderId);
    void onFailure(String failed);
    void onSuccessList(List<ShopOwnerServicesModel> services);
}
