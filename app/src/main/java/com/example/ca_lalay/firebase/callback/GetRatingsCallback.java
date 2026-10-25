package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.RatingsModel;

import java.util.ArrayList;

public interface GetRatingsCallback {
    void onSuccess(ArrayList<RatingsModel> ratings);
    void onFailure(String message);
}