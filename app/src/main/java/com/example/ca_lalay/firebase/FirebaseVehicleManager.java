package com.example.ca_lalay.firebase;

import com.example.ca_lalay.firebase.callback.CountCallback;
import com.example.ca_lalay.firebase.callback.CustomerVehicleCallback;
import com.example.ca_lalay.firebase.callback.CustomerVehicleListCallback;
import com.example.ca_lalay.model.CustomerVehicleModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FirebaseVehicleManager {

    private static DatabaseReference getDatabase() {
        return FirebaseDatabase.getInstance().getReference("Vehicles");
    }

    public static void createUserVehicle(CustomerVehicleModel vehicle, String uid, CustomerVehicleCallback callback) {
        String vehicleId = getDatabase().push().getKey();
        vehicle.setVehicleId(vehicleId);
        getDatabase().child(uid).child(vehicleId).setValue(vehicle).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                callback.onSuccess("Vehicle Created Successfully!");
            } else {
                callback.onFailure("Failed to create vehicle");
            }
        }).addOnFailureListener(failedTask -> {
            callback.onFailure("Failed to create vehicle");

        });
    }
    public static void getUserVehicles(String uid,
                                       CustomerVehicleListCallback callback) {

        getDatabase()
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<CustomerVehicleModel> vehicleList = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        CustomerVehicleModel vehicle =
                                dataSnapshot.getValue(CustomerVehicleModel.class);

                        if (vehicle != null) {
                            vehicleList.add(0, vehicle);
                        }
                    }

                    callback.onSuccess(vehicleList);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
    public static void getTotalVehiclesCount(String uid, CountCallback callback) {

        getDatabase()
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    int count = (int) snapshot.getChildrenCount();
                    callback.onSuccess(count);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
    public static void getTotalCarsCount(String uid, CountCallback callback) {

        getDatabase()
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    int count = 0;

                    for (DataSnapshot data : snapshot.getChildren()) {

                        CustomerVehicleModel vehicle =
                                data.getValue(CustomerVehicleModel.class);

                        if (vehicle != null &&
                                vehicle.getVehicleType() != null) {

                            String type = vehicle.getVehicleType();

                            if (type.equalsIgnoreCase("Sedan") ||
                                    type.equalsIgnoreCase("SUV") ||
                                    type.equalsIgnoreCase("Hatchback") ||
                                    type.equalsIgnoreCase("Pickup Truck")) {

                                count++;
                            }
                        }
                    }

                    callback.onSuccess(count);
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
    public static void getTotalMotorcyclesCount(String uid, CountCallback callback) {

        getDatabase()
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    int count = 0;

                    for (DataSnapshot data : snapshot.getChildren()) {

                        CustomerVehicleModel vehicle =
                                data.getValue(CustomerVehicleModel.class);

                        if (vehicle != null &&
                                vehicle.getVehicleType() != null) {

                            String type = vehicle.getVehicleType();

                            if (type.equalsIgnoreCase("Motorcycle") ||
                                    type.equalsIgnoreCase("Tricycle")) {

                                count++;
                            }
                        }
                    }

                    callback.onSuccess(count);
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
    public static void deleteVehicle(String uid,
                                     String vehicleId,
                                     CustomerVehicleCallback callback) {

        getDatabase()
                .child(uid)
                .child(vehicleId)
                .removeValue()
                .addOnSuccessListener(unused ->
                        callback.onSuccess("Vehicle deleted successfully"))
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
}
