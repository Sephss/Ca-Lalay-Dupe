package com.example.ca_lalay.auth;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ca_lalay.R;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.callback.SignupCallback;

public class RegisterUserActivity extends AppCompatActivity {

    private String roleSelected;

    private LottieAnimationView loadingAnimation;
    private Button signupBtn;
    private TextView error, loginBtnLink;

    private LinearLayout passwordValidator;
    private TextView passwordHint;

    private EditText firstNameET, emailAddressET, phoneNumberET, passwordET, confirmPasswordET, lastNameET;
    private ImageView selectImage;

    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_user);



        // UI
        signupBtn = findViewById(R.id.signupBtn);
        firstNameET = findViewById(R.id.firstNameET);
        emailAddressET = findViewById(R.id.emailAddressET);
        phoneNumberET = findViewById(R.id.phoneNumberET);
        passwordET = findViewById(R.id.passwordET);
        confirmPasswordET = findViewById(R.id.confirmPasswordET);
        lastNameET = findViewById(R.id.lastNameET);

        passwordHint = findViewById(R.id.passwordHint);

        loadingAnimation = findViewById(R.id.loadingAnimation);
        error = findViewById(R.id.error);
        loginBtnLink = findViewById(R.id.loginBtnLink);
        selectImage = findViewById(R.id.selectImage);

        roleSelected = getIntent().getStringExtra("role");

        loadingAnimation.setVisibility(View.GONE);

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


        signupBtn.setOnClickListener(v -> {
            error.setVisibility(View.GONE);
            signupTheUser();
        });

        loginBtnLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        selectImage.setOnClickListener(v -> choosePhotoFromGallery());
        validatePassword();
    }

    private void signupTheUser() {


        String firstname = firstNameET.getText().toString().trim();
        String theLastname = lastNameET.getText().toString().trim();
        String email = emailAddressET.getText().toString().trim();
        String phone = phoneNumberET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();

        String timestamp = String.valueOf(System.currentTimeMillis());

        // ---------------- VALIDATION ----------------



        if (firstname.isEmpty()) {
            firstNameET.setError("Firstname is required");
            firstNameET.requestFocus();
            return;
        }

        if (theLastname.isEmpty()) {
            lastNameET.setError("lastname is required");
            lastNameET.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneNumberET.setError("Phone number is required");
            phoneNumberET.requestFocus();
            return;
        }

        // Check if input contains digits only
        if (!phone.matches("\\d+")) {
            phoneNumberET.setError("Phone number must contain digits only");
            phoneNumberET.requestFocus();
            return;
        }

        // Check if input exceeds 11 digits
        if (phone.length() > 11) {
            phoneNumberET.setError("Phone number cannot exceed 11 digits");
            phoneNumberET.requestFocus();
            return;
        }

        // Check if input has less than 11 digits
        if (phone.length() < 11) {
            phoneNumberET.setError("Please enter an 11-digit Philippine phone number (e.g., 0932342523)");
            phoneNumberET.requestFocus();
            return;
        }


        if (email.isEmpty()) {
            emailAddressET.setError("Email is required");
            emailAddressET.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailAddressET.setError("Enter valid email");
            emailAddressET.requestFocus();
            return;
        }


        if (password.isEmpty()) {
            passwordET.setError("Password is required");
            passwordET.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordET.setError("Confirm password is required");
            confirmPasswordET.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            error.setText("Passwords do not match");
            error.setVisibility(View.VISIBLE);
            return;
        }

        if (!isValidPassword(password)) {
            passwordET.setError(
                    "Use 8+ characters with uppercase, lowercase, number, and symbol."
            );
            passwordET.requestFocus();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // ---------------- START LOADING ----------------
        signupBtn.setEnabled(false);
        showLoading(true);

        // ---------------- UPLOAD IMAGE ----------------
        FirebaseStorageManager.uploadImageUser("users", imageUri,
                new FirebaseStorageManager.ImageUploadCallback() {

                    @Override
                    public void onSuccess(String imageUrl) {

                        // ---------------- CREATE USER ----------------
                        FirebaseAuthManager.signupUser(
                                email,
                                password,
                                firstname,
                                theLastname,
                                phone,
                                roleSelected,
                                imageUrl,
                                timestamp,
                                new SignupCallback() {

                                    @Override
                                    public void onSuccess(String success, String imageUrls) {
                                        showLoading(false);
                                        // removed logout so user can resend
                                      //  FirebaseAuthManager.logout();
                                        Intent intent = new Intent(
                                                RegisterUserActivity.this,
                                                SuccessRegistrationActivity.class
                                        );
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                        signupBtn.setEnabled(true);
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(String failureMessage) {
                                        showLoading(false);
                                        signupBtn.setEnabled(true);
                                        showError(failureMessage);
                                    }
                                });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        showLoading(false);
                        signupBtn.setEnabled(true);
                        showError(errorMessage);
                    }
                });
    }

    // ---------------- IMAGE PICKER ----------------

    public void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            imageUri = data.getData();
            selectImage.setImageURI(imageUri);
        }
    }

    // ---------------- UI HELPERS ----------------

    private void showLoading(boolean show) {
        if (show) {
            loadingAnimation.setVisibility(View.VISIBLE);
            loadingAnimation.playAnimation();
        } else {
            loadingAnimation.cancelAnimation();
            loadingAnimation.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        error.setText(message);
        error.setVisibility(View.VISIBLE);
    }
    private void updateRequirement(TextView textView, boolean passed, String message) {
        if (passed) {
            textView.setText("✓ " + message);
            textView.setTextColor(Color.GREEN);
        } else {
            textView.setText("✗ " + message);
            textView.setTextColor(Color.RED);
        }
    }
    private void validatePassword() {
        passwordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    passwordHint.setVisibility(View.VISIBLE);
                } else {
                    passwordHint.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private boolean isValidPassword(String password) {
        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
        );
    }
}