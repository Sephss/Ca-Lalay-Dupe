package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ShopOpeningDaysModel;

public interface ShopOpeningDaysCallback {
    void onSuccess(ShopOpeningDaysModel openingDays);
    void onFailure(String errorMessage);
}