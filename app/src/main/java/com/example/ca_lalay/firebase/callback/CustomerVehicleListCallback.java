package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.CustomerVehicleModel;

import java.util.List;

public interface CustomerVehicleListCallback {
    void onSuccess(List<CustomerVehicleModel> vehicles);
    void onFailure(String errorMessage);
}