package com.example.ca_lalay.firebase;

import com.example.ca_lalay.firebase.callback.ShopOpeningDaysCallback;
import com.example.ca_lalay.firebase.callback.UpdateUserCallback;
import com.example.ca_lalay.model.ShopOpeningDaysModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseShopOpeningDaysManager {

    private static final String NODE = "shop_opening_days";

    /**
     * Fetches the opening-days config for a shop.
     * If the shop has never saved this setting, returns a default model
     * (all days true) so existing shops are never accidentally blocked.
     */
    public static void getOpeningDays(String shopId, ShopOpeningDaysCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(NODE)
                .child(shopId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onSuccess(new ShopOpeningDaysModel());
                    return;
                }

                ShopOpeningDaysModel model = snapshot.getValue(ShopOpeningDaysModel.class);
                if (model == null) {
                    model = new ShopOpeningDaysModel();
                }
                callback.onSuccess(model);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * Used later by the shop owner's edit profile page to save toggles.
     */
    public static void updateOpeningDays(String shopId, ShopOpeningDaysModel model, UpdateUserCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(NODE)
                .child(shopId);

        ref.setValue(model)
                .addOnSuccessListener(unused -> callback.onSuccess("Opening days updated successfully"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}