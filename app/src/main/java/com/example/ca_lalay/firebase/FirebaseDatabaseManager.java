        package com.example.ca_lalay.firebase;
    
        import static android.content.ContentValues.TAG;

        import android.util.Log;
    
        import androidx.annotation.NonNull;
    
        import com.example.ca_lalay.firebase.callback.CustomerDataCallback;
        import com.example.ca_lalay.firebase.callback.DatabaseCallback;
        import com.example.ca_lalay.firebase.callback.RoleCalllback;
        import com.example.ca_lalay.firebase.callback.ShopListCallback;
        import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
        import com.example.ca_lalay.firebase.callback.SignupCallback;
        import com.example.ca_lalay.firebase.callback.StatusCallback;
        import com.example.ca_lalay.firebase.callback.UpdateShopStatusResubmitDocuments;
        import com.example.ca_lalay.firebase.callback.UpdateUserCallback;
        import com.example.ca_lalay.model.ShopOwnerModel;
        import com.example.ca_lalay.model.UserModel;
        import com.example.ca_lalay.utils.Role;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;

        public class FirebaseDatabaseManager {
            private static DatabaseReference getDatabase() {
                return FirebaseDatabase.getInstance().getReference("users");
            }

            public static void saveUser(String firstname, String lastname, String email, String contactNumber,
                                        String uid, String role, String imageUrl, String timestamp,
                                        SignupCallback callback) {

                UserModel userData = new UserModel(firstname, lastname, email, contactNumber, uid, role, imageUrl, "active", timestamp, "none");

                getDatabase().child(uid).setValue(userData)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "User saved successfully");
                            callback.onSuccess("User saved", "d");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save user: " + e.getMessage());
                            callback.onFailure(e.getMessage());
                        });
            }
            public static void deleteUserData(String uid, UpdateUserCallback callback) {

                if (uid == null || uid.trim().isEmpty()) {
                    callback.onFailure("Invalid user ID.");
                    return;
                }

                getDatabase().child(uid).removeValue()
                        .addOnSuccessListener(unused ->
                                callback.onSuccess("User data deleted successfully."))
                        .addOnFailureListener(e ->
                                callback.onFailure(e.getMessage()));
            }
            public static void saveShopOwnerOrFreelancer(
                    String shopName,
                    String imageUrl,      // verification image 1
                    String imageUrl2,  // verification image 2
                    String imageUrl3,  // verification image 3
                    String shopImage,     // shop image
                    String email,
                    String contactNumber,
                    String uid,
                    String role,
                    String location,
                    String timestamp,
                    double latitude,
                    double longitude,
                    SignupCallback callback) {

                //  Default values for missing fields
                String rating = "0";
                String status = "pending";
                String shopOpenAt = "";
                String shopClosedAt = "";
                String shopAbout = "";

                ShopOwnerModel data = new ShopOwnerModel(
                        shopName,
                        imageUrl,
                        imageUrl2,
                        imageUrl3,
                        email,
                        contactNumber,
                        uid,
                        role,
                        location,
                        rating,
                        status,
                        shopImage,
                        shopOpenAt,
                        shopClosedAt,
                        shopAbout,
                        "0",
                        "0",
                        "0",
                        "0",
                        "0",
                        "0",
                        "0",
                        timestamp,
                        "",
                        "",
                        "",
                        "none",
                        latitude,
                        longitude,
                        "0"
                );

                getDatabase().child(uid).setValue(data)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Shop saved successfully");
                            callback.onSuccess("Saved", "d");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to save: " + e.getMessage());
                            callback.onFailure(e.getMessage());
                        });
            }
            public static void getUserRole(String uid, RoleCalllback callback) {
    
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("role");
    
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String role = snapshot.getValue(String.class);
                            callback.onSuccess(role);
                        } else {
                            callback.onFailure("Role not found");
                        }
                    }
    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
            }
            public static void getUserStatus(String uid, StatusCallback callback) {

                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("status");

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = snapshot.getValue(String.class);
                            callback.onSuccess(status);
                        } else {
                            callback.onFailure("Status not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
            }
            public static void getShopOwnerData(String uid, ShopOwnerDataCallback callback) {
    
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid);
    
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ShopOwnerModel shopOwnerDetail = snapshot.getValue(ShopOwnerModel.class);
    
                        if(shopOwnerDetail != null) {
                            callback.onSuccess(shopOwnerDetail);
                        } else {
                            callback.onFailure("Shop owner data not found");
                        }
                    }
    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
            }
            public static void getCustomerData(String uid, CustomerDataCallback callback)
            {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid);
    
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel customerData = snapshot.getValue(UserModel.class);
    
                        if(customerData != null) {
                            callback.onSuccess("", customerData);
                        } else {
                            callback.onFailure("Customer data not found");
                        }
                    }
    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
            }
            public static void getAllShopOwners(ShopListCallback callback) {

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<ShopOwnerModel> shopList = new ArrayList<>();

                        for (DataSnapshot data : snapshot.getChildren()) {

                            ShopOwnerModel shop = data.getValue(ShopOwnerModel.class);

                            if (shop != null &&
                                    ("shop_owner".equals(shop.getRole()) ||
                                            "freelancer".equals(shop.getRole()))) {
                                shopList.add(shop);
                            }

                        }

                        callback.onSuccess(shopList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
            }
            public static void updateCustomerData(UserModel data, UpdateUserCallback callback) {

                Map<String, Object> updates = new HashMap<>();

                if(data.getFirstname() != null || !data.getFirstname().trim().isEmpty()) {
                    updates.put("firstname", data.getFirstname());
                }
                if(data.getLastname() != null || !data.getLastname().trim().isEmpty()) {
                    updates.put("lastname", data.getLastname());
                }
                if(data.getContactNum() != null || !data.getContactNum().trim().isEmpty()) {
                    updates.put("contactNum", data.getContactNum());
                }
                if(data.getImageUrl() != null || !data.getImageUrl().trim().isEmpty()) {
                    updates.put("imageUrl", data.getImageUrl());
                }

                getDatabase().child(data.getUid()).updateChildren(updates)   .addOnSuccessListener(unused ->
                                callback.onSuccess("Profile updated successfully."))
                        .addOnFailureListener(e ->
                                callback.onFailure(e.getMessage()));
            }
            public static void updateShopOwnerData(ShopOwnerModel data, UpdateUserCallback callback) {
                Map<String, Object> updates = new HashMap<>();

                if(data.getShopName() != null || !data.getShopName().trim().isEmpty()) {
                    updates.put("shopName", data.getShopName());
                }
                if(data.getContactNum() != null || !data.getContactNum().trim().isEmpty()) {
                    updates.put("contactNum", data.getContactNum());
                }
                if(data.getLocation() != null || !data.getLocation().trim().isEmpty()) {
                    updates.put("location", data.getLocation());
                }
                if(data.getShopOpenAt() != null || !data.getShopOpenAt().trim().isEmpty()) {
                    updates.put("shopOpenAt", data.getShopOpenAt());
                }
                if(data.getShopClosedAt() != null || !data.getShopClosedAt().trim().isEmpty()) {
                    updates.put("shopClosedAt", data.getShopClosedAt());
                }
                if(data.getShopSlot() != null || !data.getShopSlot().trim().isEmpty()) {
                    updates.put("shopSlot", data.getShopSlot());
                }
                if(data.getShopAbout() != null || !data.getShopAbout().trim().isEmpty()) {
                    updates.put("shopAbout", data.getShopAbout());
                }
                if(data.getShopImage() != null || !data.getShopImage().trim().isEmpty()) {
                    updates.put("shopImage", data.getShopImage());
                }

                updates.put("latitude", data.getLatitude());
                updates.put("longitude", data.getLongitude());


                getDatabase().child(data.getUid()).updateChildren(updates)   .addOnSuccessListener(unused ->
                                callback.onSuccess("Shop details updated successfully."))
                        .addOnFailureListener(e ->
                                callback.onFailure(e.getMessage()));

            }
            public static void updateShopStatusReuploadDocuments(String uid, String status, String image1, String image2, String image3, UpdateShopStatusResubmitDocuments callback) {
                Map<String, Object> updates = new HashMap<>();

                if(status != null || !status.trim().isEmpty()) {
                    updates.put("status", status);
                }
                if(image1 != null || !image1.trim().isEmpty()) {
                    updates.put("verificationImage1", image1);
                }
                if(image2 != null || !image2.trim().isEmpty()) {
                    updates.put("verificationImage2", image2);
                }
                if(image3 != null || !image3.trim().isEmpty()) {
                    updates.put("verificationImage3", image3);
                }

                getDatabase().child(uid).updateChildren(updates).addOnSuccessListener(completed -> {
                    callback.onSuccess("Documents resubmitted successfully");
                }).addOnFailureListener(failed -> {
                    callback.onFailure("Failed to resubmit documents");
                });
            }

        }
