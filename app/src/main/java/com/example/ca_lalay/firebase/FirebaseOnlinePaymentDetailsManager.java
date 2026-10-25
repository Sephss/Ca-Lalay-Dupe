package com.example.ca_lalay.firebase;

import androidx.annotation.NonNull;

import com.example.ca_lalay.model.OnlinePaymentDetailsModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseOnlinePaymentDetailsManager {

    private static DatabaseReference getDatabase() {
        return FirebaseDatabase.getInstance()
                .getReference("shop_payment_details");
    }

    public static void saveOnlinePaymentDetails(
            String uid,
            String details,
            OnSaveOnlinePaymentDetailsListener listener) {

        String updatedAt = String.valueOf(System.currentTimeMillis());

        OnlinePaymentDetailsModel model =
                new OnlinePaymentDetailsModel(
                        uid,
                        updatedAt,
                        details
                );

        getDatabase()
                .child(uid)
                .setValue(model)
                .addOnSuccessListener(unused -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public static void getOnlinePaymentDetails(
            String uid,
            OnGetOnlinePaymentDetailsListener listener) {

        getDatabase()
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

                        OnlinePaymentDetailsModel model =
                                snapshot.getValue(OnlinePaymentDetailsModel.class);

                        if (listener != null) {
                            listener.onSuccess(model);
                        }

                    } else {

                        if (listener != null) {
                            listener.onNotFound();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public interface OnGetOnlinePaymentDetailsListener {

        void onSuccess(OnlinePaymentDetailsModel model);

        void onNotFound();

        void onFailure(String error);
    }

    public interface OnSaveOnlinePaymentDetailsListener {
        void onSuccess();
        void onFailure(String error);
    }
}