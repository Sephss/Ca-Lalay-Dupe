package com.example.ca_lalay.Session;

import com.example.ca_lalay.model.UserModel;

public class UserSession {
    private static UserSession instance;
    private UserModel currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if(instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(UserModel user) {
        this.currentUser = user;
    }

    public UserModel getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        currentUser = null;
    }
}