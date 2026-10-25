package com.example.ca_lalay.firebase;

import androidx.annotation.NonNull;

import com.example.ca_lalay.firebase.callback.CarBrowserServiceCallback;
import com.example.ca_lalay.firebase.callback.CarServicesCallback;
import com.example.ca_lalay.firebase.callback.CountServicesCallback;
import com.example.ca_lalay.firebase.callback.ServiceDetailsCallback;
import com.example.ca_lalay.model.ShopOwnerServicesModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseCarServicesManager {
    private static DatabaseReference getDatabase() {
        return FirebaseDatabase.getInstance().getReference("car_services");

    }
    public static void createService(String serviceName, String shopName, String shopID, String shopContactNum, String shopLocation, String shopRatings, String shopImageUrl, String description, String imageUrl, String serviceProviderId, String category, String price, String durationMinutes, String isAvailable, String createdAt, String status, String rating, String reviewCount, String mediumPercentage, String largePercentage, String shopOpenAt, String shopClosedAt, CarServicesCallback callback) {

        String serviceId = getDatabase().push().getKey();
        ShopOwnerServicesModel details = new ShopOwnerServicesModel(serviceName, shopName, shopID, shopContactNum, shopLocation, shopRatings, shopImageUrl, description, imageUrl, serviceId, serviceProviderId, category, price, durationMinutes, isAvailable, createdAt, status, rating, reviewCount, mediumPercentage, largePercentage, shopOpenAt, shopClosedAt);

        getDatabase().child(serviceProviderId).child(serviceId).setValue(details).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                callback.onSuccess("Service Created Successfully!", serviceId, serviceProviderId);
            } else {
                callback.onFailure("Failed to create service");
            }
        }).addOnFailureListener(failedTask -> {
            callback.onFailure("Failed to create service");
        });

    }
    public static void fetchAllServices(CarBrowserServiceCallback callback) {
        getDatabase().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<ShopOwnerServicesModel> serviceList = new ArrayList<>();

                for (DataSnapshot providerSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot serviceSnapshot : providerSnapshot.getChildren()) {

                        ShopOwnerServicesModel service =
                                serviceSnapshot.getValue(ShopOwnerServicesModel.class);

                        if (service != null) {
                            serviceList.add(service);
                        }
                    }
                }

                callback.onSuccessList(serviceList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }
    public static void fetchServicesByShop(String serviceProviderId, CarBrowserServiceCallback callback) {

        getDatabase()
                .child(serviceProviderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<ShopOwnerServicesModel> serviceList = new ArrayList<>();

                        for (DataSnapshot serviceSnapshot : snapshot.getChildren()) {

                            ShopOwnerServicesModel service =
                                    serviceSnapshot.getValue(ShopOwnerServicesModel.class);

                            if (service != null) {
                                serviceList.add(service);
                            }
                        }

                        callback.onSuccessList(serviceList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }
    public static void getServiceCount(String serviceProviderId, CountServicesCallback callback) {
        getDatabase()
                .child(serviceProviderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        callback.onSuccess((int) count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }
    public static void getActiveServiceCount(
            String serviceProviderId,
            CountServicesCallback callback
    ) {

        getDatabase()
                .child(serviceProviderId)
                .orderByChild("status")
                .equalTo("active")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long count = snapshot.getChildrenCount();

                        callback.onSuccess((int) count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public static void getServiceDetails(
            String serviceProviderId,
            String serviceId,
            ServiceDetailsCallback callback) {

        getDatabase()
                .child(serviceProviderId)
                .child(serviceId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            ShopOwnerServicesModel service =
                                    snapshot.getValue(ShopOwnerServicesModel.class);

                            if (service != null) {
                                callback.onSuccess(service);
                            } else {
                                callback.onFailure("Failed to load service details.");
                            }

                        } else {
                            callback.onFailure("Service not found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public static void updateServiceData(
            ShopOwnerServicesModel data,
            CarServicesCallback callback
    ) {

        getDatabase()
                .child(data.getServiceProviderId())
                .child(data.getServiceId())
                .setValue(data)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        callback.onSuccess(
                                "Service updated successfully!",
                                data.getServiceId(),
                                data.getServiceProviderId()
                        );
                    } else {
                        callback.onFailure("Failed to update service.");
                    }

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage())
                );
    }

    public static void updateServiceStatus(
            String serviceProviderId,
            String serviceId,
            String Status,
            CarServicesCallback callback
    ) {

        getDatabase()
                .child(serviceProviderId)
                .child(serviceId)
                .child("status")
                .setValue(Status)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        callback.onSuccess(
                                "Service deactivated successfully!",
                                serviceId,
                                serviceProviderId
                        );
                    } else {
                        callback.onFailure("Failed to deactivate service.");
                    }

                })
                .addOnFailureListener(e ->
                        callback.onFailure(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Failed to deactivate service."
                        )
                );
    }
    public static void getInactiveServiceCount(
            String serviceProviderId,
            CountServicesCallback callback
    ) {

        getDatabase()
                .child(serviceProviderId)
                .orderByChild("status")
                .equalTo("inactive")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long count = snapshot.getChildrenCount();

                        callback.onSuccess((int) count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        callback.onFailure(error.getMessage());
                    }
                });
    }
    public static void fetchAllActiveServices(CarBrowserServiceCallback callback) {

        getDatabase().addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<ShopOwnerServicesModel> serviceList = new ArrayList<>();

                for (DataSnapshot providerSnapshot : snapshot.getChildren()) {

                    for (DataSnapshot serviceSnapshot : providerSnapshot.getChildren()) {

                        ShopOwnerServicesModel service =
                                serviceSnapshot.getValue(ShopOwnerServicesModel.class);

                        if (service != null &&
                                "active".equalsIgnoreCase(service.getStatus())) {

                            serviceList.add(service);
                        }
                    }
                }

                // Sort newest created service first
                serviceList.sort((service1, service2) -> {

                    long time1 = parseCreatedAt(service1.getCreatedAt());
                    long time2 = parseCreatedAt(service2.getCreatedAt());

                    return Long.compare(time2, time1);
                });

                callback.onSuccessList(serviceList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }
    private static long parseCreatedAt(String createdAt) {

        if (createdAt == null || createdAt.isEmpty()) {
            return 0;
        }

        try {
            return Long.parseLong(createdAt);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public static void fetchActiveServicesByShop(
            String serviceProviderId,
            CarBrowserServiceCallback callback
    ) {

        getDatabase()
                .child(serviceProviderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<ShopOwnerServicesModel> serviceList =
                                new ArrayList<>();

                        for (DataSnapshot serviceSnapshot : snapshot.getChildren()) {

                            ShopOwnerServicesModel service =
                                    serviceSnapshot.getValue(
                                            ShopOwnerServicesModel.class
                                    );

                            // Only add ACTIVE services
                            if (service != null &&
                                    "active".equalsIgnoreCase(service.getStatus())) {

                                serviceList.add(service);
                            }
                        }

                        // Newest service first
                        serviceList.sort((service1, service2) -> {

                            long time1 = parseCreatedAt(
                                    service1.getCreatedAt()
                            );

                            long time2 = parseCreatedAt(
                                    service2.getCreatedAt()
                            );

                            return Long.compare(time2, time1);
                        });

                        callback.onSuccessList(serviceList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }



}
