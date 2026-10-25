package com.example.ca_lalay.firebase;

import static com.example.ca_lalay.firebase.FirebaseDatabaseManager.saveShopOwnerOrFreelancer;

import android.net.Uri;

import com.example.ca_lalay.firebase.callback.AuthResultCallback;
import com.example.ca_lalay.firebase.callback.ChangePasswordCallback;
import com.example.ca_lalay.firebase.callback.DeleteAccountCallback;
import com.example.ca_lalay.firebase.callback.ForgotPasswordCallback;
import com.example.ca_lalay.firebase.callback.ResendEmailCallback;
import com.example.ca_lalay.firebase.callback.RoleCalllback;
import com.example.ca_lalay.firebase.callback.SignupCallback;
import com.example.ca_lalay.firebase.callback.FourImageUploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {

    private static FirebaseAuth firebaseAuth;

    public static FirebaseAuth getAuth() {
        if(firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }

    // login
    public static void loginUser(String email, String password, AuthResultCallback callback) {

        getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = getAuth().getCurrentUser();

                        if (user == null) {
                            callback.onFailure("User not found");
                            return;
                        }

                        user.reload().addOnCompleteListener(reloadTask -> {

                            if (!user.isEmailVerified()) {
                                callback.onFailure("Please verify your email first");
                                return;
                            }

                            String uid = user.getUid();

                            // 🔥 FETCH ROLE FROM DATABASE


                            FirebaseDatabaseManager.getUserRole(uid, new RoleCalllback() {
                                @Override
                                public void onSuccess(String role) {
                                    callback.onSuccess(user, role);
                                }

                                @Override
                                public void onFailure(String message) {
                                    callback.onFailure(message);
                                }
                            });

                        });

                    } else {
                        callback.onFailure("Incorrect email or password");
                    }

                });
    }
    public static void deleteAccount(String currentPassword, DeleteAccountCallback callback) {

        FirebaseUser user = getCurrentUser();

        if (user == null) {
            callback.onFailure("No logged-in user found.");
            return;
        }

        String email = user.getEmail();

        if (email == null || email.isEmpty()) {
            callback.onFailure("Unable to get user email.");
            return;
        }

        // Firebase requires a recent login before allowing account deletion
        com.google.firebase.auth.AuthCredential credential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(
                        email,
                        currentPassword
                );

        user.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {

                    if (!reauthTask.isSuccessful()) {
                        callback.onFailure("Current password is incorrect.");
                        return;
                    }

                    user.delete()
                            .addOnCompleteListener(deleteTask -> {

                                if (deleteTask.isSuccessful()) {
                                    callback.onSuccess("Account deleted successfully.");
                                } else {
                                    String error = deleteTask.getException() != null
                                            ? deleteTask.getException().getMessage()
                                            : "Failed to delete account.";

                                    callback.onFailure(error);
                                }

                            });
                });
    }
    public static void signupUser(String email, String password, String firstname, String lastname, String contactNumber, String role, String imageUrl, String timestamp, SignupCallback callback) {
        getAuth().createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                FirebaseUser firebaseUser = getAuth().getCurrentUser();

                if(firebaseUser != null) {
                    String uid = firebaseUser.getUid();

                    firebaseUser.sendEmailVerification().addOnCompleteListener(sendEmail -> {
                        if (sendEmail.isSuccessful()) {

                            // 🔥 FORCE refresh auth state
                            firebaseUser.reload().addOnCompleteListener(reloadTask -> {

                                FirebaseDatabaseManager.saveUser(
                                        firstname, lastname, email, contactNumber, uid, role, imageUrl, timestamp,
                                        new SignupCallback() {
                                            @Override
                                            public void onSuccess(String success, String imageUrl) {
                                                callback.onSuccess("User saved & verification sent", "s");
                                            }

                                            @Override
                                            public void onFailure(String failureMessage) {
                                                callback.onFailure("DB Error: " + failureMessage);
                                            }
                                        }
                                );

                            });

                        } else {
                            callback.onFailure("Failed to send verification email.");
                        }
                    });
                } else {
                    callback.onFailure("Signup Failed");
                }
            } else {
                callback.onFailure("Signup failed");
            }
        }).addOnFailureListener(failedTask -> {
            if(failedTask instanceof FirebaseAuthUserCollisionException) {
                callback.onFailure("Account already exists with this email.");
            } else {
                callback.onFailure("Signup failed");
            }
        });
    }
    public static void signupShopOwnerOrFreelancer(
            String email,
            String password,
            String shopName,
            String contactNumber,
            String role,
            String location,
            Uri imageUri,
            Uri imageUri2,
            Uri imageUri3,
            Uri imageUri4,
            String timestamp,
            double latitude,
            double longitude,
            SignupCallback callback) {

        getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = getAuth().getCurrentUser();

                        if (firebaseUser != null) {

                            String uid = firebaseUser.getUid();

                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(sendEmail -> {

                                        if (sendEmail.isSuccessful()) {

                                            // ✅ Ensure auth is ready
                                            firebaseUser.reload().addOnCompleteListener(reload -> {

                                                FirebaseStorageManager.uploadFourImages(
                                                        "UserProfile",
                                                        imageUri,
                                                        imageUri2,
                                                        imageUri3,
                                                        imageUri4,
                                                        new FourImageUploadCallback() {

                                                            @Override
                                                            public void onSuccess(String imageUrl1, String imageUrl2, String imageUrl3, String imageUrl4) {



                                                                saveShopOwnerOrFreelancer(
                                                                        shopName,
                                                                        imageUrl1,
                                                                        imageUrl3,
                                                                        imageUrl4,
                                                                        imageUrl2,
                                                                        email,
                                                                        contactNumber,
                                                                        uid,
                                                                        role,
                                                                        location,
                                                                        timestamp,
                                                                        latitude,
                                                                        longitude,
                                                                        new SignupCallback() {

                                                                            @Override
                                                                            public void onSuccess(String success, String imageUrl) {
                                                                                callback.onSuccess("Signup complete", imageUrl2);
                                                                            }

                                                                            @Override
                                                                            public void onFailure(String failureMessage) {
                                                                                callback.onFailure("DB Error: " + failureMessage);
                                                                            }
                                                                        }
                                                                );
                                                            }

                                                            @Override
                                                            public void onFailure(String error) {
                                                                callback.onFailure("Image upload failed: " + error);
                                                            }
                                                        });

                                            });

                                        } else {
                                            callback.onFailure("Failed to send verification email.");
                                        }
                                    });

                        } else {
                            callback.onFailure("User is null after signup");
                        }

                    } else {
                        callback.onFailure(task.getException() != null
                                ? task.getException().getMessage()
                                : "Signup failed");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void resendVerificationEmail(ResendEmailCallback callback) {

        if (callback == null) return;

        FirebaseUser user = getAuth().getCurrentUser();

        if (user == null) {
            callback.onFailure("No logged-in user found.");
            return;
        }

        user.reload().addOnCompleteListener(reloadTask -> {

            if (!reloadTask.isSuccessful()) {
                callback.onFailure("Failed to refresh user.");
                return;
            }

            if (user.isEmailVerified()) {
                callback.onFailure("Email is already verified.");
                return;
            }

            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure("Failed to resend verification email.");
                        }

                    });

        });
    }
    public static void logout() {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
        }
    }
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    public static String getCurrentUserUid() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    public static void changePassword(
            String currentPassword,
            String newPassword,
            ChangePasswordCallback callback) {

        FirebaseUser user = getCurrentUser();

        if (user == null) {
            callback.onFailure("No logged-in user found.");
            return;
        }

        String email = user.getEmail();

        if (email == null || email.isEmpty()) {
            callback.onFailure("Unable to get user email.");
            return;
        }

        // Re-authenticate using the current password
        com.google.firebase.auth.AuthCredential credential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(
                        email,
                        currentPassword
                );

        user.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {

                    if (!reauthTask.isSuccessful()) {
                        callback.onFailure("Current password is incorrect.");
                        return;
                    }

                    // Current password is correct.
                    // Now update to the new password.
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(updateTask -> {

                                if (updateTask.isSuccessful()) {
                                    callback.onSuccess("Password changed successfully.");
                                } else {
                                    String error = updateTask.getException() != null
                                            ? updateTask.getException().getMessage()
                                            : "Failed to change password.";

                                    callback.onFailure(error);
                                }

                            });
                });
    }
    public static void sendForgotPasswordEmail(String email, ForgotPasswordCallback callback) {
        if (email == null || email.trim().isEmpty()) {
            callback.onFailure("Please enter your email address.");
            return;
        }

        getAuth().sendPasswordResetEmail(email.trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Password reset email sent. Please check your inbox.");
                    } else {
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to send reset email.";
                        callback.onFailure(errorMessage);
                    }
                });
    }

}
