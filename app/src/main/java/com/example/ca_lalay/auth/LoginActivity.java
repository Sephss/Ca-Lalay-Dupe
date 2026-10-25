package com.example.ca_lalay.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.SessionManager;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.activities.customer.CustomerDashboard;
import com.example.ca_lalay.activities.customer.CustomerProfile;
import com.example.ca_lalay.activities.shopowner.ShopOwnerDashboard;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseLocationsManager;
import com.example.ca_lalay.firebase.callback.AuthResultCallback;
import com.example.ca_lalay.firebase.callback.DeleteAccountCallback;
import com.example.ca_lalay.firebase.callback.DeleteLocationCallback;
import com.example.ca_lalay.firebase.callback.ForgotPasswordCallback;
import com.example.ca_lalay.firebase.callback.StatusCallback;
import com.example.ca_lalay.firebase.callback.UpdateUserCallback;
import com.example.ca_lalay.model.UserModel;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private LottieAnimationView loader;
    private Button loginBtn;
    private ImageView imageView;

    private TextView errorPrompt, signupLink, forgotPasswordLink;
    private EditText emailET, passwordET;

    private SessionManager sessionManager;

    private Handler cooldownHandler;
    private Runnable cooldownRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        cooldownHandler = new Handler(Looper.getMainLooper());

        loader = findViewById(R.id.loadingAnimation);
        loginBtn = findViewById(R.id.loginBtn);

        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);

        errorPrompt = findViewById(R.id.errorPrompt);
        imageView = findViewById(R.id.imageView);
        signupLink = findViewById(R.id.signupLink);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            Insets ime = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            int bottom = Math.max(
                    systemBars.bottom,
                    ime.bottom
            );

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    bottom
            );

            return insets;
        });

        loginBtn.setOnClickListener(v -> loginTheUser());

        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelection.class));
        });

        forgotPasswordLink.setOnClickListener(d -> {
            String email = emailET.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailET.setError("Enter a valid email address");
                emailET.requestFocus();
                return;
            }

            // Step 1: Ask for confirmation
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Reset Password")
                    .setMessage("Are you sure you want to send a password reset link to " + email + "?")
                    .setPositiveButton("Send", (dialog, which) -> {

                        // Step 2: Disable link/button to prevent multiple clicks during network request
                        forgotPasswordLink.setEnabled(false);

                        sendForgotPassword(email);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
    private void sendForgotPassword(String email) {
        FirebaseAuthManager.sendForgotPasswordEmail(email, new ForgotPasswordCallback() {
            @Override
            public void onSuccess(String message) {
                showSuccessDialog(email);

                // Step 3: Start 10-second cooldown timer on success
                startCooldownTimer(30);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Re-enable immediately on failure so they can try again
                forgotPasswordLink.setEnabled(true);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCooldownTimer(int seconds) {
        cooldownRunnable = new Runnable() {
            int secondsLeft = seconds;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    forgotPasswordLink.setEnabled(false);
                    forgotPasswordLink.setText("Resend in " + secondsLeft + "s");
                    secondsLeft--;
                    cooldownHandler.postDelayed(this, 1000);
                } else {
                    // Timer finished: Re-enable link & restore text
                    forgotPasswordLink.setEnabled(true);
                    forgotPasswordLink.setText("Forgot Password?");
                }
            }
        };

        cooldownHandler.post(cooldownRunnable);
    }

    private void showSuccessDialog(String email) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Email Sent")
                .setMessage("Password reset email has been sent to " + email + ". Please check your inbox.")
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler callbacks to prevent memory leaks when Activity dies
        if (cooldownHandler != null && cooldownRunnable != null) {
            cooldownHandler.removeCallbacks(cooldownRunnable);
        }
    }

    private void loginTheUser() {
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        if (email.isEmpty()) {
            emailET.setError("Email is required");
            emailET.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.setError("Enter a valid email address");
            emailET.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordET.setError("Password is required");
            passwordET.requestFocus();
            return;
        }

        loader.setVisibility(View.VISIBLE);
        loader.playAnimation();
        errorPrompt.setVisibility(View.GONE);

        FirebaseAuthManager.loginUser(email, password, new AuthResultCallback() {
            @Override
            public void onSuccess(FirebaseUser user, String role) {

                String uid = user.getUid();



                FirebaseDatabaseManager.getUserStatus(uid, new StatusCallback() {
                    @Override
                    public void onSuccess(String status) {

                        loader.setVisibility(View.GONE);
                        loader.cancelAnimation();

                        if ("rejected".equals(status)) {
                            UserSession.getInstance().clearSession();
                             ShopOwnerSession.getInstance().clearSession();
                              sessionManager.logout();
                              Intent intent = new Intent(LoginActivity.this, RejectedRegistration.class);
                              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                              startActivity(intent);
                               return;

                          //  FirebaseLocationsManager.deleteLocation(uid, new DeleteLocationCallback() {
                             //   @Override
                              //  public void onSuccess(String message) {
                                  //  FirebaseAuthManager.deleteAccount(password, new DeleteAccountCallback() {
                                   //     @Override
                                     //   public void onSuccess(String message) {
                                        //    FirebaseAuthManager.logout();
                                         //   UserSession.getInstance().clearSession();
                                           // ShopOwnerSession.getInstance().clearSession();
                                          //  sessionManager.logout();
                                          //  Intent intent = new Intent(LoginActivity.this, RejectedRegistration.class);
                                          //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                          //  startActivity(intent);
                                         //   return;
                                      //  }

                                     //   @Override
                                      //  public void onFailure(String message) {
                                     //       Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                     //   }
                                 //   });
                              //  }

                            //    @Override
                             //   public void onFailure(String message) {
                               //     Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                             //   }
                        //    });
                        //    return;
                        }
                        if ("suspended".equals(status)) {

                            FirebaseAuthManager.logout();
                            UserSession.getInstance().clearSession();
                            ShopOwnerSession.getInstance().clearSession();
                            sessionManager.logout();

                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    AccountSuspended.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);

                            return;
                        }


                        if ("customer".equals(role) && "active".equals(status)) {

                            // ✅ SAVE SESSION
                            sessionManager.saveLoginSession(uid, role);

                            navigateTo(CustomerDashboard.class);
                            finish();

                        } else if ("pending".equals(status)
                                && "shop_owner".equals(role)) {

                            FirebaseAuthManager.logout();
                            UserSession.getInstance().clearSession();
                            ShopOwnerSession.getInstance().clearSession();
                            sessionManager.logout();

                            navigateTo(PendingRegistration.class);
                            finish();
                            return;

                        } else if ("pending".equals(status)
                                && "freelancer".equals(role)) {
                            FirebaseAuthManager.logout();
                            UserSession.getInstance().clearSession();
                            ShopOwnerSession.getInstance().clearSession();
                            sessionManager.logout();

                            navigateTo(PendingRegistration.class);
                            finish();
                            return;

                        } else if ("active".equals(status)
                                && "freelancer".equals(role)) {

                            // ✅ SAVE SESSION
                            sessionManager.saveLoginSession(uid, role);

                            navigateTo(ShopOwnerDashboard.class);
                            finish();
                            return;

                        } else if ("active".equals(status)
                                && "shop_owner".equals(role)) {

                            // ✅ SAVE SESSION
                            sessionManager.saveLoginSession(uid, role);

                            navigateTo(ShopOwnerDashboard.class);
                            finish();
                            return;

                        } else {

                            // Unexpected role/status combination
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Invalid account status or role.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            FirebaseAuthManager.logout();
                            UserSession.getInstance().clearSession();
                            ShopOwnerSession.getInstance().clearSession();
                            sessionManager.logout();
                            return;
                        }


                    }

                    @Override
                    public void onFailure(String message) {
                        loader.setVisibility(View.GONE);
                        loader.cancelAnimation();
                        errorPrompt.setText(message);
                        errorPrompt.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(String failureMessage) {
                loader.setVisibility(View.GONE);
                loader.cancelAnimation();
                errorPrompt.setText(failureMessage);
                errorPrompt.setVisibility(View.VISIBLE);
            }
        });
    }



    private void navigateTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        startActivity(intent);
    }
}