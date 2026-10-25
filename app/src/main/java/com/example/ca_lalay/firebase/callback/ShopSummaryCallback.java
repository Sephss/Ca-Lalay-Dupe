package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ShopOwnerModel;

public interface ShopSummaryCallback {
    void onSuccess(ShopOwnerModel shop);
    void onFailure(String message);
}