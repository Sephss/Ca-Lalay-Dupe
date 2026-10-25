package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ShopOwnerServicesModel;

public interface ServiceDetailsCallback {
    void onSuccess(ShopOwnerServicesModel service);
    void onFailure(String failed);

}
