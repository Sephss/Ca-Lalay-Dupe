package com.example.ca_lalay.Session;

import com.example.ca_lalay.model.ShopOwnerModel;

public class ShopOwnerSession {
    private static ShopOwnerSession instance;
    private ShopOwnerModel currentShopOwner;

    private ShopOwnerSession () {

    }

    public static ShopOwnerSession getInstance() {
        if (instance == null) {
            instance = new ShopOwnerSession();
        }
        return instance;
    }

    public void setCurrentShopOwner(ShopOwnerModel shopOwner) {
        this.currentShopOwner = shopOwner;
    }

    public ShopOwnerModel getCurrentShopOwner() {
        return currentShopOwner;
    }

    public void clearSession() {
        currentShopOwner = null;
    }
}
