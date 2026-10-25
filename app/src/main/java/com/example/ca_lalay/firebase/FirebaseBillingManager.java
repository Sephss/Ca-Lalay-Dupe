package com.example.ca_lalay.firebase;

import androidx.annotation.NonNull;

import com.example.ca_lalay.firebase.callback.BillNumberCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseBillingManager {
    private static DatabaseReference getDatabase() {
        return FirebaseDatabase.getInstance().getReference("TicketCounter");
    }

    public static void generateTicketNumber(BillNumberCallback callback) {

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM",
                Locale.ENGLISH
        );

        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        String monthKey = sdf.format(new Date());

        DatabaseReference counterRef = getDatabase().child(monthKey);

        counterRef.runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(
                    @NonNull MutableData currentData
            ) {

                Integer count = currentData.getValue(Integer.class);

                if (count == null) {
                    count = 0;
                }

                count++;

                currentData.setValue(count);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(
                    DatabaseError error,
                    boolean committed,
                    DataSnapshot currentData
            ) {

                if (error != null || !committed) {
                    callback.onFailure("Failed to generate ticket number.");
                    return;
                }

                Integer count = currentData.getValue(Integer.class);

                if (count == null) {
                    callback.onFailure("Counter value is null.");
                    return;
                }

                String billingNumber =
                        "CAL-BILL-"
                                + monthKey
                                + "-"
                                + String.format(Locale.US, "%05d", count);

                callback.onSuccess(billingNumber);
            }
        });
    }
}
