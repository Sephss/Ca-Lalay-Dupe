package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.BookingReportModel;

public interface OnReportCallback {
    void onSuccess(BookingReportModel report);
    void onFailure(String message);
}