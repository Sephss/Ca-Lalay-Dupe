package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.LocationsModel;

import java.util.List;

public interface GetLocationsCallback {
    void onSuccess(List<LocationsModel> locations);
    void onFailure(String error);
}