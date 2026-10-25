package com.example.ca_lalay.firebase;

import com.example.ca_lalay.firebase.callback.BookingRatingCallback;
import com.example.ca_lalay.firebase.callback.GetRatingsCallback;
import com.example.ca_lalay.firebase.callback.RatingCountCallback;
import com.example.ca_lalay.firebase.callback.RatingsCallback;
import com.example.ca_lalay.firebase.callback.ShopSummaryCallback;
import com.example.ca_lalay.model.RatingsModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class FirebaseReviewRatingsManager {
    private static DatabaseReference getDatabase() {
        return FirebaseDatabase.getInstance().getReference("ratings");
    }

    public static void saveRatingsReview(RatingsModel data, RatingsCallback callback) {
        String ratingsID = getDatabase().push().getKey();
        if (ratingsID == null) {
            callback.onFailure("Failed to generate rating ID.");
            return;
        }

        data.setRatingsID(ratingsID);

        getDatabase().child(data.getServiceProviderID()).child(ratingsID).setValue(data).addOnSuccessListener(task -> {
            FirebaseBookingManager.updateBookingRating(data.getBookingID(), data.getRatingStar(), data.getRatingsID(), data.getRatingComment(), data.getDateRated(), new BookingRatingCallback() {
                @Override
                public void onSuccess(String success) {
                    updateShopRatingSummary(
                            data.getServiceProviderID(),
                            Float.parseFloat(data.getRatingStar()),
                            new RatingsCallback() {

                                @Override
                                public void onSuccess(String message) {
                                    callback.onSuccess(message);
                                }

                                @Override
                                public void onFailure(String message) {

                                    // Optional: rollback here if desired
                                    callback.onFailure(message);
                                }
                            });

                }

                @Override
                public void onFailure(String message) {
                    getDatabase()
                            .child(data.getServiceProviderID())
                            .child(ratingsID)
                            .removeValue()
                            .addOnCompleteListener(task -> {
                                callback.onFailure(message);
                            });
                }
            });
        }).addOnFailureListener(failed -> {
            callback.onFailure(failed.getMessage());
        });
    }
    public static void getAllRatings(
            String serviceProviderID,
            GetRatingsCallback callback
    ) {

        getDatabase()
                .child(serviceProviderID)
                .get()
                .addOnSuccessListener(snapshot -> {

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

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
    public static void getTotalRatingsCount(
            String serviceProviderID,
            RatingCountCallback callback
    ) {

        getDatabase()
                .child(serviceProviderID)
                .get()
                .addOnSuccessListener(snapshot -> {

                    int totalReviews = (int) snapshot.getChildrenCount();

                    callback.onSuccess(totalReviews);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

    }

    public static void updateShopRatingSummary(
            String shopUid,
            float newRating,
            RatingsCallback callback
    ) {

        DatabaseReference shopRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(shopUid);

        shopRef.get().addOnSuccessListener(snapshot -> {

            ShopOwnerModel shop = snapshot.getValue(ShopOwnerModel.class);

            if (shop == null) {
                callback.onFailure("Shop not found.");
                return;
            }

            // Convert Strings to numbers
            float overallRating = Float.parseFloat(shop.getOverallRating());
            int totalReviews = Integer.parseInt(shop.getTotalReviews());

            int fiveStarCount = Integer.parseInt(shop.getFiveStarCount());
            int fourStarCount = Integer.parseInt(shop.getFourStarCount());
            int threeStarCount = Integer.parseInt(shop.getThreeStarCount());
            int twoStarCount = Integer.parseInt(shop.getTwoStarCount());
            int oneStarCount = Integer.parseInt(shop.getOneStarCount());

            // Calculate new average
            if (totalReviews == 0) {

                overallRating = newRating;
                totalReviews = 1;

            } else {

                overallRating =
                        ((overallRating * totalReviews) + newRating)
                                / (totalReviews + 1);

                totalReviews++;

            }

            // Increment the proper star counter
            switch ((int) newRating) {

                case 5:
                    fiveStarCount++;
                    break;

                case 4:
                    fourStarCount++;
                    break;

                case 3:
                    threeStarCount++;
                    break;

                case 2:
                    twoStarCount++;
                    break;

                case 1:
                    oneStarCount++;
                    break;
            }

            // Save back as Strings
            HashMap<String, Object> updates = new HashMap<>();

            updates.put("overallRating", String.format(Locale.ENGLISH, "%.1f", overallRating));
            updates.put("totalReviews", String.valueOf(totalReviews));

            updates.put("fiveStarCount", String.valueOf(fiveStarCount));
            updates.put("fourStarCount", String.valueOf(fourStarCount));
            updates.put("threeStarCount", String.valueOf(threeStarCount));
            updates.put("twoStarCount", String.valueOf(twoStarCount));
            updates.put("oneStarCount", String.valueOf(oneStarCount));

            shopRef.updateChildren(updates)
                    .addOnSuccessListener(unused ->
                            callback.onSuccess("Shop rating updated successfully."))
                    .addOnFailureListener(e ->
                            callback.onFailure(e.getMessage()));

        }).addOnFailureListener(e ->
                callback.onFailure(e.getMessage()));

    }
    public static void getShopSummary(
            String shopUid,
            ShopSummaryCallback callback
    ) {

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(shopUid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    ShopOwnerModel shop =
                            snapshot.getValue(ShopOwnerModel.class);

                    if (shop != null) {
                        callback.onSuccess(shop);
                    } else {
                        callback.onFailure("Shop not found.");
                    }

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
}
