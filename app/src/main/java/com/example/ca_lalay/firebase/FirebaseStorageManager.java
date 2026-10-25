package com.example.ca_lalay.firebase;

import android.net.Uri;
import android.text.TextUtils;

import com.example.ca_lalay.firebase.callback.FourImageUploadCallback;
import com.example.ca_lalay.firebase.callback.ReuploadDocumentsCallback;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageManager {
    public FirebaseStorageManager() {

    }
    private static final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    public static void uploadImage(String folder, Uri imageUri, ImageUploadCallback callback) {
        String fileName = System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(folder + "/" + fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()))
                )
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static void uploadVehicleImage(String folder, Uri imageUri, ImageUploadCallback callback) {
        String fileName = System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(folder + "/" + fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()))
                )
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static void uploadImageUser(String folder, Uri imageUri, ImageUploadCallback callback) {
        String fileName = System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(folder + "/" + fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()))
                )
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void uploadFourImages(
            String folder,
            Uri imageUri1,
            Uri imageUri2,
            Uri verificationImage2,
            Uri verificationImage3,
            FourImageUploadCallback callback) {

        String timestamp = String.valueOf(System.currentTimeMillis());

        String fileName1 = timestamp + "_1.jpg";
        String fileName2 = timestamp + "_2.jpg";
        String fileName3 = timestamp + "_3.jpg";
        String fileName4 = timestamp + "_4.jpg";

        StorageReference imageRef1 = storageRef.child(folder + "/" + fileName1);
        StorageReference imageRef2 = storageRef.child(folder + "/" + fileName2);
        StorageReference imageRef3 = storageRef.child(folder + "/" + fileName3);
        StorageReference imageRef4 = storageRef.child(folder + "/" + fileName4);

        // IMAGE 1
        imageRef1.putFile(imageUri1)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef1.getDownloadUrl()
                                .addOnSuccessListener(uri1 -> {

                                    // IMAGE 2
                                    imageRef2.putFile(imageUri2)
                                            .addOnSuccessListener(taskSnapshot2 ->
                                                    imageRef2.getDownloadUrl()
                                                            .addOnSuccessListener(uri2 -> {

                                                                // IMAGE 3
                                                                imageRef3.putFile(verificationImage2)
                                                                        .addOnSuccessListener(taskSnapshot3 ->
                                                                                imageRef3.getDownloadUrl()
                                                                                        .addOnSuccessListener(uri3 -> {

                                                                                            // IMAGE 4
                                                                                            imageRef4.putFile(verificationImage3)
                                                                                                    .addOnSuccessListener(taskSnapshot4 ->
                                                                                                            imageRef4.getDownloadUrl()
                                                                                                                    .addOnSuccessListener(uri4 -> {

                                                                                                                        callback.onSuccess(
                                                                                                                                uri1.toString(),
                                                                                                                                uri2.toString(),
                                                                                                                                uri3.toString(),
                                                                                                                                uri4.toString()
                                                                                                                        );

                                                                                                                    })
                                                                                                                    .addOnFailureListener(e ->
                                                                                                                            callback.onFailure(e.getMessage())
                                                                                                                    )
                                                                                                    )
                                                                                                    .addOnFailureListener(e ->
                                                                                                            callback.onFailure(e.getMessage())
                                                                                                    );

                                                                                        })
                                                                                        .addOnFailureListener(e ->
                                                                                                callback.onFailure(e.getMessage())
                                                                                        )
                                                                        )
                                                                        .addOnFailureListener(e ->
                                                                                callback.onFailure(e.getMessage())
                                                                        );

                                                            })
                                                            .addOnFailureListener(e ->
                                                                    callback.onFailure(e.getMessage())
                                                            )
                                            )
                                            .addOnFailureListener(e ->
                                                    callback.onFailure(e.getMessage())
                                            );

                                })
                                .addOnFailureListener(e ->
                                        callback.onFailure(e.getMessage())
                                )
                )
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage())
                );
    }

    public static void reuploadDocumentsImage(
            String folder,
            Uri imageUri1,
            Uri imageUri2,
            Uri imageUri3,
            ReuploadDocumentsCallback callback) {

        String timestamp = String.valueOf(System.currentTimeMillis());

        String fileName1 = timestamp + "_1.jpg";
        String fileName2 = timestamp + "_2.jpg";
        String fileName3 = timestamp + "_3.jpg";


        StorageReference imageRef1 = storageRef.child(folder + "/" + fileName1);
        StorageReference imageRef2 = storageRef.child(folder + "/" + fileName2);
        StorageReference imageRef3 = storageRef.child(folder + "/" + fileName3);

        // IMAGE 1
        imageRef1.putFile(imageUri1)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef1.getDownloadUrl()
                                .addOnSuccessListener(uri1 -> {

                                    // IMAGE 2
                                    imageRef2.putFile(imageUri2)
                                            .addOnSuccessListener(taskSnapshot2 ->
                                                    imageRef2.getDownloadUrl()
                                                            .addOnSuccessListener(uri2 -> {

                                                                // IMAGE 3
                                                                imageRef3.putFile(imageUri3)
                                                                        .addOnSuccessListener(taskSnapshot3 ->
                                                                                imageRef3.getDownloadUrl()
                                                                                        .addOnSuccessListener(uri3 -> {

                                                                                            callback.onSuccess(
                                                                                                    uri1.toString(),
                                                                                                    uri2.toString(),
                                                                                                    uri3.toString()
                                                                                            );

                                                                                        })
                                                                                        .addOnFailureListener(e ->
                                                                                                callback.onFailure(e.getMessage())
                                                                                        )
                                                                        )
                                                                        .addOnFailureListener(e ->
                                                                                callback.onFailure(e.getMessage())
                                                                        );

                                                            })
                                                            .addOnFailureListener(e ->
                                                                    callback.onFailure(e.getMessage())
                                                            )
                                            )
                                            .addOnFailureListener(e ->
                                                    callback.onFailure(e.getMessage())
                                            );

                                })
                                .addOnFailureListener(e ->
                                        callback.onFailure(e.getMessage())
                                )
                )
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage())
                );
    }
    public static void updateProfileImage(
            String folder,
            String oldImageUrl,
            Uri newImageUri,
            ImageUploadCallback callback
    ) {
        if (isValidStorageUrl(oldImageUrl)) {
            FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl)
                    .delete()
                    .addOnCompleteListener(task -> {
                        // Whether delete succeeded or failed, still upload the new image.
                        uploadNewProfileImage(folder, newImageUri, callback);
                    });
        } else {
            // No valid old image to delete — just upload the new one.
            uploadNewProfileImage(folder, newImageUri, callback);
        }
    }

    private static void uploadNewProfileImage(String folder, Uri newImageUri, ImageUploadCallback callback) {
        String fileName = System.currentTimeMillis() + ".jpg";
        StorageReference newImageRef = storageRef.child(folder + "/" + fileName);

        newImageRef.putFile(newImageUri)
                .addOnSuccessListener(taskSnapshot -> newImageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()))
                )
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private static boolean isValidStorageUrl(String url) {
        return !TextUtils.isEmpty(url)
                && (url.startsWith("gs://") || url.startsWith("https://firebasestorage.googleapis.com"));
    }



    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }
}
