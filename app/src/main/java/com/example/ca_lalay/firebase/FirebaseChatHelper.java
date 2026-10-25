package com.example.ca_lalay.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ca_lalay.firebase.callback.ChatCallback;
import com.example.ca_lalay.firebase.callback.ConversationCreateCallback;
import com.example.ca_lalay.firebase.callback.ConversationFetchCallback;
import com.example.ca_lalay.firebase.callback.MessageFetchCallback;
import com.example.ca_lalay.model.ChatModel;
import com.example.ca_lalay.model.ConversationModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class  FirebaseChatHelper {

    // Messages node
    public static DatabaseReference getMessagesDatabase() {
        return FirebaseDatabase.getInstance().getReference("messages");
    }

    // Conversations node
    public static DatabaseReference getConversationsDatabase() {
        return FirebaseDatabase.getInstance().getReference("conversations");
    }
    public static String getConversationId(String user1, String user2) {

        if (user1.compareTo(user2) < 0) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }
    public static void createConversationIfNotExists(

            String customerId,
            String customerName,
            String customerImage,

            String shopId,
            String shopName,
            String shopImage,

            ConversationCreateCallback callback
    ) {

        String conversationId = getConversationId(customerId, shopId);

        getConversationsDatabase()
                .child(conversationId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    // Conversation already exists
                    if (snapshot.exists()) {
                        callback.onSuccess(conversationId);
                        return;
                    }

                    // Create a new conversation
                    ConversationModel conversation = new ConversationModel(
                            conversationId,

                            customerId,
                            customerName,
                            customerImage,

                            shopId,
                            shopName,
                            shopImage,

                            "",     // lastMessage
                            "",     // lastTimestamp
                            false,
                            false
                    );

                    getConversationsDatabase()
                            .child(conversationId)
                            .setValue(conversation)
                            .addOnSuccessListener(unused -> {

                                callback.onSuccess(conversationId);

                            })
                            .addOnFailureListener(e -> {

                                callback.onFailure(e.getMessage());

                            });

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(e.getMessage());

                });

    }
    public static void sendMessage(

            String conversationId,
            String senderId,
            String receiverId,
            String message,
            String messageType,

            ChatCallback callback
    ) {

        String messageId = getMessagesDatabase().child(conversationId).push().getKey();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String sentDate = new SimpleDateFormat(
                "MMM dd, yyyy",
                Locale.getDefault()
        ).format(new Date());

        String sentTime = new SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
        ).format(new Date());

        ChatModel chat = new ChatModel(
                messageId,
                senderId,
                receiverId,
                message,
                timestamp,
                messageType,
                sentDate,
                sentTime
        );

        getMessagesDatabase()
                .child(conversationId)
                .child(messageId)
                .setValue(chat)
                .addOnSuccessListener(unused -> {

                    callback.onSuccess("Message Sent");

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(e.getMessage());

                });

    }
    public static void updateLastMessage(
            String conversationId,
            String lastMessage,
            boolean isSenderCustomer

    ) {

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> updates = new HashMap<>();

        updates.put("lastMessage", lastMessage);
        updates.put("lastTimestamp", timestamp);

        updates.put("readByCustomer", isSenderCustomer);
        updates.put("readByShop", !isSenderCustomer);

        getConversationsDatabase()
                .child(conversationId)
                .updateChildren(updates);
    }
    public static void markConversationAsRead(
            String conversationId,
            boolean isCustomer
    ) {

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(isCustomer ? "readByCustomer" : "readByShop", true);

        getConversationsDatabase()
                .child(conversationId)
                .updateChildren(updates);
    }
    public static ValueEventListener fetchMessages(
            String conversationId,
            MessageFetchCallback callback
    ) {

        ValueEventListener listener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<ChatModel> list = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    ChatModel chat = data.getValue(ChatModel.class);
                    if (chat != null) {
                        list.add(chat);
                    }
                }

                callback.onSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        };

        getMessagesDatabase()
                .child(conversationId)
                .orderByChild("timestamp")
                .addValueEventListener(listener);

        return listener;
    }
    public static void fetchConversations(
            String currentUserId,
            ConversationFetchCallback callback
    ) {

        getConversationsDatabase()
                .orderByChild("lastTimestamp")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<ConversationModel> list = new ArrayList<>();

                        for (DataSnapshot data : snapshot.getChildren()) {

                            ConversationModel conversation =
                                    data.getValue(ConversationModel.class);

                            if (conversation == null)
                                continue;

                            if (currentUserId.equals(conversation.getCustomerId())
                                    || currentUserId.equals(conversation.getShopId())) {

                                list.add(0, conversation);
                            }
                        }

                        callback.onSuccess(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });

    }
}


