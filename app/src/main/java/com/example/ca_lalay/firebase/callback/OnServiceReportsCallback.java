package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ServiceReportModel;

import java.util.ArrayList;

public interface OnServiceReportsCallback {
    void onSuccess(ArrayList<ServiceReportModel> reports);
    void onFailure(String message);
}