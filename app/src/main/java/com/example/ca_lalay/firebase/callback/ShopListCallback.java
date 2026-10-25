package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ShopOwnerModel;
import java.util.List;

public interface ShopListCallback {
    void onSuccess(List<ShopOwnerModel> shopList);
    void onFailure(String error);
}