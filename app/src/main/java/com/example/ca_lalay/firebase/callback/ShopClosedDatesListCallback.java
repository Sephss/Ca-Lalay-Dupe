package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ShopClosedDateModel;
import java.util.List;

public interface ShopClosedDatesListCallback {
    void onSuccess(List<ShopClosedDateModel> closedDates);
    void onFailure(String errorMessage);
}