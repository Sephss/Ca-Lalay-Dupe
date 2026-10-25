package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.TimeSlotOption;

import java.util.List;

public interface AvailableTimesCallback {
    void onSuccess(List<TimeSlotOption> slots);
    void onFailure(String errorMessage);
}