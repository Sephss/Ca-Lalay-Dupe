package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ShopScheduleResult;

public interface ShopScheduleCallback {
    void onSuccess(ShopScheduleResult result);
    void onFailure(String errorMessage);
}