package com.example.ca_lalay.activities.customer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.callback.ChangePasswordCallback;

public class CustomerChangePassword extends AppCompatActivity {
    private ImageView backBtn;
    private EditText currentPassword, newPassword, confirmNewPassword;
    private Button confirmBtn;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_change_password);

        backBtn = findViewById(R.id.backBtn);
        currentPassword = findViewById(R.id.currentPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);

        confirmBtn = findViewById(R.id.confirmBtn);

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
        backBtn.setOnClickListener(s -> {
            finish();
        });

        confirmBtn.setOnClickListener(g -> {
            changePassword();
        });
    }
    private void changePassword() {
        String currentPass = currentPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String confirmPass = confirmNewPassword.getText().toString().trim();

        // Check empty fields
        if (currentPass.isEmpty()) {
            currentPassword.setError("Enter your current password");
            currentPassword.requestFocus();
            return;
        }

        if (newPass.isEmpty()) {
            newPassword.setError("Enter your new password");
            newPassword.requestFocus();
            return;
        }

        if (confirmPass.isEmpty()) {
            confirmNewPassword.setError("Confirm your new password");
            confirmNewPassword.requestFocus();
            return;
        }

        // Check new password confirmation
        if (!newPass.equals(confirmPass)) {
            confirmNewPassword.setError("Passwords do not match");
            confirmNewPassword.requestFocus();
            return;
        }

        // Optional: prevent using the same password
        if (currentPass.equals(newPass)) {
            newPassword.setError("New password must be different from your current password");
            newPassword.requestFocus();
            return;
        }

        if (!isValidPassword(newPass)) {
            newPassword.setError(
                    "Use 8+ characters with uppercase, lowercase, number, and symbol."
            );
            newPassword.requestFocus();
            return;
        }
        setLoadingState();

        // Firebase password update
        FirebaseAuthManager.changePassword(
                currentPass,
                newPass,
                new ChangePasswordCallback() {

                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(
                                CustomerChangePassword.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                        setNormalState();
                        finish();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(
                                CustomerChangePassword.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                        setNormalState();
                    }
                }
        );

    }
    private boolean isValidPassword(String password) {
        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
        );
    }
    private void setLoadingState() {
        confirmBtn.setEnabled(false);
        confirmBtn.setAlpha(0.5f);
        confirmBtn.setText("CHANGING...");
    }

    private void setNormalState() {
        confirmBtn.setEnabled(true);
        confirmBtn.setAlpha(1f);
        confirmBtn.setText("CONFIRM");
    }
}