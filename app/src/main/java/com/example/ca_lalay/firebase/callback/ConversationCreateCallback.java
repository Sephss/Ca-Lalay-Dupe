package com.example.ca_lalay.firebase.callback;

public interface ConversationCreateCallback {
    void onSuccess(String conversationId);
    void onFailure(String message);
}