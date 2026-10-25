package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.LocationsModel;

public interface GetSingleLocationCallback {

    void onSuccess(LocationsModel location);

    void onFailure(String error);

}