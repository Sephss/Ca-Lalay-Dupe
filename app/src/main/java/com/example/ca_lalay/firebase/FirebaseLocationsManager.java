package com.example.ca_lalay.firebase;

import com.example.ca_lalay.firebase.callback.DeleteLocationCallback;
import com.example.ca_lalay.firebase.callback.GetLocationsCallback;
import com.example.ca_lalay.firebase.callback.GetSingleLocationCallback;
import com.example.ca_lalay.firebase.callback.LocationCallback;
import com.example.ca_lalay.model.LocationsModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseLocationsManager {
    private static DatabaseReference getDatabase() {
        return FirebaseDatabase.getInstance().getReference("Locations");
    }
    public static void saveLocation(LocationsModel locationsModel, String uid, LocationCallback callback) {
        getDatabase().child(uid).setValue(locationsModel).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                callback.onSuccess("Location saved successfully");
            } else {
                callback.onFailure("Failed to save location");
            }
        }).addOnFailureListener(failed -> {
            callback.onFailure("Failed to save location" + failed);
        });
    }
    public static void updateLocation(
            String uid,
            double latitude,
            double longitude,
            String locationName,
            LocationCallback callback
    ) {
        Map<String, Object> updates = new HashMap<>();

        updates.put("latitude", latitude);
        updates.put("longitude", longitude);
        updates.put("locationName", locationName);

        getDatabase().child(uid).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Location updated successfully");
                    } else {
                        callback.onFailure("Failed to update location");
                    }
                })
                .addOnFailureListener(failed -> {
                    callback.onFailure("Failed to update location: " + failed.getMessage());
                });
    }
    public static void getAllLocations(
            GetLocationsCallback callback
    ) {

        getDatabase()
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<LocationsModel> locations =
                            new ArrayList<>();

                    for(DataSnapshot dataSnapshot :
                            snapshot.getChildren()) {

                        LocationsModel location =
                                dataSnapshot.getValue(
                                        LocationsModel.class
                                );

                        if(location != null) {
                            locations.add(location);
                        }
                    }

                    callback.onSuccess(locations);

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage()
                    );

                });
    }
    public static void deleteLocation(String uid, DeleteLocationCallback callback) {
        getDatabase().child(uid).removeValue().addOnSuccessListener(task -> {
            callback.onSuccess("Location deleted successfully");
        }).addOnFailureListener(failed -> {
            callback.onFailure("Failed to delete location");
        });
    }
    public static void getLocation(
            String uid,
            GetSingleLocationCallback callback
    ) {

        getDatabase()
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    LocationsModel location =
                            snapshot.getValue(
                                    LocationsModel.class
                            );

                    if(location != null){
                        callback.onSuccess(location);
                    }else{
                        callback.onFailure("Location not found.");
                    }

                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public static void getAllActiveLocations(
            GetLocationsCallback callback
    ) {

        getDatabase()
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<LocationsModel> activeLocations =
                            new ArrayList<>();

                    int totalLocations =
                            (int) snapshot.getChildrenCount();

                    if (totalLocations == 0) {
                        callback.onSuccess(activeLocations);
                        return;
                    }

                    int[] completed = {0};

                    for (DataSnapshot locationSnapshot :
                            snapshot.getChildren()) {

                        // This is your shopID
                        String shopID = locationSnapshot.getKey();

                        LocationsModel location =
                                locationSnapshot.getValue(
                                        LocationsModel.class
                                );

                        if (location == null || shopID == null) {

                            completed[0]++;

                            if (completed[0] == totalLocations) {
                                callback.onSuccess(activeLocations);
                            }

                            continue;
                        }

                        // Look for the same shopID under "users"
                        FirebaseDatabase
                                .getInstance()
                                .getReference("users")
                                .child(shopID)
                                .get()
                                .addOnSuccessListener(userSnapshot -> {

                                    ShopOwnerModel shopOwner =
                                            userSnapshot.getValue(
                                                    ShopOwnerModel.class
                                            );

                                    // Only add ACTIVE shops
                                    if (shopOwner != null
                                            && "active".equalsIgnoreCase(
                                            shopOwner.getStatus()
                                    )) {

                                        activeLocations.add(location);
                                    }

                                    completed[0]++;

                                    if (completed[0] == totalLocations) {
                                        callback.onSuccess(activeLocations);
                                    }

                                })
                                .addOnFailureListener(e -> {

                                    completed[0]++;

                                    if (completed[0] == totalLocations) {
                                        callback.onSuccess(activeLocations);
                                    }
                                });
                    }

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(e.getMessage());

                });
    }


}
