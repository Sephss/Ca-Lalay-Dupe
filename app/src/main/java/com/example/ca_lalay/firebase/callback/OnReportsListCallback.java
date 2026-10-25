package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.BookingReportModel;

import java.util.ArrayList;

public interface OnReportsListCallback {
    void onSuccess(ArrayList<BookingReportModel> reports);

    void onFailure(String message);
}
