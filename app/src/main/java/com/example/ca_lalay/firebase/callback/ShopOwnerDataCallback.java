package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ShopOwnerModel;

public interface ShopOwnerDataCallback {
    void onSuccess(ShopOwnerModel data);
    void onFailure (String failed);
}
