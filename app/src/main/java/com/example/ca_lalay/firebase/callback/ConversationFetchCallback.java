package com.example.ca_lalay.firebase.callback;

import com.example.ca_lalay.model.ConversationModel;

import java.util.List;

public interface ConversationFetchCallback {
    void onSuccess(List<ConversationModel> list);
    void onFailure(String error);
}