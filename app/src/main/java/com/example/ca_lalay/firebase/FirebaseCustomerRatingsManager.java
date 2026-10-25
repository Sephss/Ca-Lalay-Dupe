package com.example.ca_lalay.firebase;

import com.example.ca_lalay.firebase.callback.BookingRatingCallback;
import com.example.ca_lalay.firebase.callback.GetRatingsCallback;
import com.example.ca_lalay.firebase.callback.RatingsCallback;
import com.example.ca_lalay.model.RatingsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;

public class FirebaseCustomerRatingsManager {
    private static DatabaseReference getDatabase() {
        return FirebaseDatabase.getInstance().getReference("customer_ratings");
    }
    public static void saveRatingsReview(RatingsModel data, RatingsCallback callback) {
        String ratingsID = getDatabase().push().getKey();
        if (ratingsID == null) {
            callback.onFailure("Failed to generate rating ID.");
            return;
        }

        data.setRatingsID(ratingsID);

        getDatabase().child(data.getRaterID()).child(ratingsID).setValue(data).addOnSuccessListener(task -> {
            callback.onSuccess("Ratings saved successfully");
        }).addOnFailureListener(failed -> {
            callback.onFailure(failed.getMessage());
        });
    }
    public static void getAllMyRatings(String customerId, GetRatingsCallback callback) {
        getDatabase().child(customerId).get().addOnSuccessListener(snapshot -> {

            ArrayList<RatingsModel> ratingsList = new ArrayList<>();
            for (DataSnapshot data : snapshot.getChildren()) {

                RatingsModel rating =
                        data.getValue(RatingsModel.class);

                if (rating != null) {
                    ratingsList.add(rating);
                }
            }

            // Sort latest first
            Collections.sort(ratingsList, (r1, r2) -> {
                long t1 = Long.parseLong(r1.getTimestamp());
                long t2 = Long.parseLong(r2.getTimestamp());

                return Long.compare(t2, t1); // Descending (Newest → Oldest)
            });

            callback.onSuccess(ratingsList);

        })  .addOnFailureListener(e ->
                callback.onFailure(e.getMessage()));
    }
}
