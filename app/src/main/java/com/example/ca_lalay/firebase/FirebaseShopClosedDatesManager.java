package com.example.ca_lalay.firebase;

import com.example.ca_lalay.firebase.callback.ShopClosedDateCallback;
import com.example.ca_lalay.firebase.callback.ShopClosedDatesListCallback;
import com.example.ca_lalay.firebase.callback.UpdateUserCallback;
import com.example.ca_lalay.model.ShopClosedDateModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FirebaseShopClosedDatesManager {

    private static final String NODE = "shop_closed_dates";

    public static void addClosedDate(String shopId, String dateISO, String reason, UpdateUserCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(NODE)
                .child(shopId)
                .child(dateISO);

        ShopClosedDateModel model = new ShopClosedDateModel();
        model.setReason(reason);
        model.setCreatedAt(String.valueOf(System.currentTimeMillis()));

        ref.setValue(model)
                .addOnSuccessListener(unused -> callback.onSuccess("Closed date added"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static void removeClosedDate(String shopId, String dateISO, UpdateUserCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(NODE)
                .child(shopId)
                .child(dateISO);

        ref.removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess("Closed date removed"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Single-date lookup used by the customer's date picker.
     */
    public static void getClosedDate(String shopId, String dateISO, ShopClosedDateCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(NODE)
                .child(shopId)
                .child(dateISO);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onNotFound();
                    return;
                }
                ShopClosedDateModel model = snapshot.getValue(ShopClosedDateModel.class);
                if (model == null) {
                    callback.onNotFound();
                    return;
                }
                model.setDate(snapshot.getKey());
                callback.onFound(model);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * Full list for the shop owner's management UI.
     * Lazily cleans up (deletes) any dates already in the past before returning the list,
     * so the owner's list never shows stale closures and the node doesn't grow forever.
     */
    public static void getAllClosedDates(String shopId, ShopClosedDatesListCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(NODE)
                .child(shopId);

        String todayISO = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<ShopClosedDateModel> result = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String dateKey = child.getKey();
                    if (dateKey == null) continue;

                    // yyyy-MM-dd strings compare lexicographically same as chronologically
                    if (dateKey.compareTo(todayISO) < 0) {
                        // Past date — lazily clean it up, don't include in the list
                        child.getRef().removeValue();
                        continue;
                    }

                    ShopClosedDateModel model = child.getValue(ShopClosedDateModel.class);
                    if (model != null) {
                        model.setDate(dateKey);
                        result.add(model);
                    }
                }

                callback.onSuccess(result);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }
}