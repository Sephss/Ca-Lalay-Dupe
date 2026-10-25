package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.CustomerBookingModel;

import java.util.List;

public interface CustomerBookingListCallback {
    void onSuccess(List<CustomerBookingModel> bookingList);
    void onFailure(String errorMessage);
}
