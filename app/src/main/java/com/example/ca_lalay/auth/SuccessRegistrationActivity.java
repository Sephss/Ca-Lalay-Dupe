package com.example.ca_lalay.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.SessionManager;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.callback.ResendEmailCallback;
import com.google.firebase.auth.FirebaseUser;

public class SuccessRegistrationActivity extends AppCompatActivity {

    private Button btnContinue;
    private SessionManager sessionManager;

    private TextView tvResend, tvEmail;

    private CountDownTimer resendTimer;
    private boolean canResend = false;

    @Override
    public void onBackPressed() {

        // Logout Firebase
        FirebaseAuthManager.logout();

        // Go to Login
        Intent intent = new Intent(
                SuccessRegistrationActivity.this,
                LoginActivity.class
        );

        startActivity(intent);
        finish();

        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_success_registration);

        // ---------------- FIND VIEWS ----------------

        btnContinue = findViewById(R.id.btnContinue);
        tvResend = findViewById(R.id.tvResend);
        tvEmail = findViewById(R.id.tvEmail);

        sessionManager = new SessionManager(this);

        // ---------------- GET EMAIL ----------------

        String email = getIntent().getStringExtra("email");

        // ---------------- WINDOW INSETS ----------------

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // ---------------- CLEAR APP SESSION ----------------

        UserSession.getInstance().clearSession();
        ShopOwnerSession.getInstance().clearSession();
        sessionManager.logout();

        // IMPORTANT:
        // DO NOT call FirebaseAuthManager.logout() here.
        //
        // The Firebase user must remain logged in so that
        // resendVerificationEmail() can access currentUser.

        // ---------------- DISPLAY EMAIL ----------------

        if (email != null) {
            tvEmail.setText(email);
        }



        // ---------------- INITIAL 30 SECOND COUNTDOWN ----------------

        startResendCountdown(50_000);



        // ---------------- CONTINUE BUTTON ----------------

        btnContinue.setOnClickListener(v -> {

            // User is done with verification screen.
            // Now we can logout Firebase.

            FirebaseAuthManager.logout();
            UserSession.getInstance().clearSession();
            ShopOwnerSession.getInstance().clearSession();
            sessionManager.logout();

            Intent intent = new Intent(
                    SuccessRegistrationActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        });

        // ---------------- RESEND BUTTON ----------------

        tvResend.setOnClickListener(v -> {

            // Prevent clicking while countdown is active
            if (!canResend) {
                return;
            }

            // Disable immediately
            canResend = false;
            tvResend.setEnabled(false);

            tvResend.setText("Sending...");

            FirebaseAuthManager.resendVerificationEmail(
                    new ResendEmailCallback() {

                        @Override
                        public void onSuccess(FirebaseUser user) {

                            Toast.makeText(
                                    SuccessRegistrationActivity.this,
                                    "Verification email sent again.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // ----------------
                            // START 30 SECOND COOLDOWN
                            // ----------------

                            startResendCountdown(30_000);
                        }

                        @Override
                        public void onFailure(String failureMessage) {

                            // Allow user to try again
                            canResend = true;
                            tvResend.setEnabled(true);

                            tvResend.setText(
                                    "Resend verification email"
                            );

                            Toast.makeText(
                                    SuccessRegistrationActivity.this,
                                    failureMessage,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        });
    }

    // ============================================================
    // RESEND COUNTDOWN
    // ============================================================

    private void startResendCountdown(long milliseconds) {

        // Stop previous timer if one exists
        if (resendTimer != null) {
            resendTimer.cancel();
        }

        canResend = false;
        tvResend.setEnabled(false);

        resendTimer = new CountDownTimer(
                milliseconds,
                1000
        ) {

            @Override
            public void onTick(long millisUntilFinished) {

                long seconds =
                        millisUntilFinished / 1000;

                tvResend.setText(
                        "Resend verification email (" +
                                seconds +
                                "s)"
                );
            }

            @Override
            public void onFinish() {

                canResend = true;
                tvResend.setEnabled(true);

                tvResend.setText(
                        "Resend verification email"
                );
            }

        }.start();
    }

    // ============================================================
    // CLEAN UP TIMER
    // ============================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (resendTimer != null) {
            resendTimer.cancel();
            resendTimer = null;
        }
    }
}