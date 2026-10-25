package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ShopClosedDateModel;

public interface ShopClosedDateCallback {
    void onFound(ShopClosedDateModel closedDate);
    void onNotFound();
    void onFailure(String errorMessage);
}