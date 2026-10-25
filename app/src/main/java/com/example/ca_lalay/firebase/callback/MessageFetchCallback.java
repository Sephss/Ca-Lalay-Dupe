package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ChatModel;

import java.util.List;

public interface MessageFetchCallback {
    void onSuccess(List<ChatModel> messages);
    void onFailure(String error);
}