package com.example.ca_lalay.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.shopowner.ShopOwnerSelectLocation;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseLocationsManager;
import com.example.ca_lalay.firebase.callback.LocationCallback;
import com.example.ca_lalay.firebase.callback.SignupCallback;
import com.example.ca_lalay.model.LocationsModel;
import com.google.android.material.transition.platform.MaterialFadeThrough;

public class ShopOwnerOrFreelancerRegistration extends AppCompatActivity {
    private LinearLayout serviceAddPhotoLayout, selectLocation, retreivedLocationLayout;
    private TextView error, loginBtnLink;
    private EditText shopNameET, emailAddressET, phoneNumberET, passwordET, confirmPasswordET;
    private TextView passwordHint, retreivedLocationTV;
    private ImageView imgPhoto, uploadImage;
    private Button signupBtn;
    private String selectedRole;
    private Uri imageUri, imageUri2, imageUri3, imageUriProfile;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int PICK_IMAGE_REQUEST2 = 1003;
    private static final int PICK_IMAGE_REQUEST3 = 1004;
    private static final int PICK_IMAGE_REQUEST_PROFILE = 1002;
    private LottieAnimationView loadingAnimation;

    private TextView addProfileLabel, businessNameLabel, businessLocationLabel;

    private TextView viewVerificationDocumentsLabel;

    private double selectedLatitude;
    private double selectedLongitude;
    private String selectedAddress = "";

    // pre final defense changes
    private LinearLayout stepOneLayout, stepTwoLayout, serviceAddPhotoLayout2, serviceAddPhotoLayout3;
    private Button nextBtn;
    private ImageView backBtn, imgPhoto2, imgPhoto3;
    private ConstraintLayout root;
    private MaterialFadeThrough fadeThrough;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_or_freelancer_registration);

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

        serviceAddPhotoLayout = findViewById(R.id.serviceAddPhotoLayout);
        retreivedLocationLayout = findViewById(R.id.retreivedLocationLayout);
        // Edit texts
        shopNameET = findViewById(R.id.fullNameET);
        emailAddressET = findViewById(R.id.emailAddressET);
        phoneNumberET = findViewById(R.id.phoneNumberET);
        passwordET = findViewById(R.id.passwordET);
        confirmPasswordET = findViewById(R.id.confirmPasswordET);
        loginBtnLink = findViewById(R.id.loginBtnLink);
        signupBtn = findViewById(R.id.signupBtn);

        // pre final defense new changes
        stepOneLayout = findViewById(R.id.stepOneLayout);
        stepTwoLayout = findViewById(R.id.stepTwoLayout);
        nextBtn = findViewById(R.id.nextBtn);
        backBtn = findViewById(R.id.backBtn);
        root = findViewById(R.id.main);
        fadeThrough = new MaterialFadeThrough();
        fadeThrough.setDuration(300);
        serviceAddPhotoLayout2 = findViewById(R.id.serviceAddPhotoLayout2);
        serviceAddPhotoLayout3 = findViewById(R.id.serviceAddPhotoLayout3);
        imgPhoto2 = findViewById(R.id.imgPhoto2);
        imgPhoto3 = findViewById(R.id.imgPhoto3);


        addProfileLabel = findViewById(R.id.addProfileLabel);
        businessNameLabel = findViewById(R.id.businessNameLabel);
        businessLocationLabel = findViewById(R.id.businessLocationLabel);

        passwordHint = findViewById(R.id.passwordHint);
        selectLocation = findViewById(R.id.selectLocation);

        viewVerificationDocumentsLabel = findViewById(R.id.viewVerificationDocumentsLabel);

        retreivedLocationTV  = findViewById(R.id.retreivedLocationTV);

        imgPhoto = findViewById(R.id.imgPhoto);
        uploadImage = findViewById(R.id.uploadImage);
        // animation lottie
        loadingAnimation = findViewById(R.id.loadingAnimation);

        // Textviews
        error = findViewById(R.id.error);

        Intent intent = getIntent();
        selectedRole = intent.getStringExtra("role");

        setUpTextDisplays();
        validatePassword();

        viewVerificationDocumentsLabel.setPaintFlags(
                viewVerificationDocumentsLabel.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );

        viewVerificationDocumentsLabel.setOnClickListener(s -> {
            viewApprovedDocuments();
        });

        serviceAddPhotoLayout.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });

        imgPhoto.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });

        serviceAddPhotoLayout2.setOnClickListener(s -> {
            choosePhotoFromGallery2();
        });

        imgPhoto2.setOnClickListener(s -> {
            choosePhotoFromGallery2();
        });

        serviceAddPhotoLayout3.setOnClickListener(s -> {
            choosePhotoFromGallery3();
        });

        imgPhoto3.setOnClickListener(s -> {
            choosePhotoFromGallery3();
        });

        uploadImage.setOnClickListener(d -> {
            choosePhotoFromGalleryForImage();
        });

        signupBtn.setOnClickListener(s -> {

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Registration")
                    .setMessage("Are you sure the provided details are correct and ready for registration?")
                    .setNegativeButton("Review", null)
                    .setPositiveButton("Confirm Sign Up", (dialog, which) -> {
                        signupTheUser();
                    })
                    .show();
        });

        nextBtn.setOnClickListener(s -> {
            checkStepOneBeforeStepTwo();
        });

        backBtn.setOnClickListener(s -> {
            TransitionManager.beginDelayedTransition(root, fadeThrough);
            stepOneLayout.setVisibility(View.VISIBLE);
            stepTwoLayout.setVisibility(View.GONE);
        });

        loginBtnLink.setOnClickListener(s -> {
            Intent intents = new Intent(ShopOwnerOrFreelancerRegistration.this, LoginActivity.class);
            startActivity(intents);
            finish();
        });

        selectLocation.setOnClickListener(f -> {

            Intent ent =
                    new Intent(
                            ShopOwnerOrFreelancerRegistration.this,
                            ShopOwnerSelectLocation.class
                    );

            locationPickerLauncher.launch(ent);
        });
    }

    private final androidx.activity.result.ActivityResultLauncher<Intent>
            locationPickerLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            Intent data = result.getData();

                            selectedLatitude =
                                    data.getDoubleExtra(
                                            "latitude",
                                            0
                                    );

                            selectedLongitude =
                                    data.getDoubleExtra(
                                            "longitude",
                                            0
                                    );

                            selectedAddress =
                                    data.getStringExtra(
                                            "address"
                                    );

                            if (selectedLatitude != 0
                                    && selectedLongitude != 0
                                    && selectedAddress != null
                                    && !selectedAddress.isEmpty()) {
                                retreivedLocationLayout.setVisibility(View.VISIBLE);
                                retreivedLocationTV.setText(selectedAddress);
                            } else {
                                retreivedLocationLayout.setVisibility(View.GONE);

                            }

                            Toast.makeText(
                                    this,
                                    "Location Selected",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Optional logs
                            android.util.Log.d(
                                    "RETREIVED LAT",
                                    selectedLatitude + ""
                            );

                            android.util.Log.d(
                                    "RETREIVED LON",
                                    selectedLongitude + ""
                            );

                            android.util.Log.d(
                                    "RETREIVED LOC",
                                    selectedAddress
                            );
                        }
                    });
    public void choosePhotoFromGalleryForImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST_PROFILE);
    }


    public void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    public void choosePhotoFromGallery2() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST2);
    }
    public void choosePhotoFromGallery3() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST3);
    }

    private void viewApprovedDocuments() {

        StringBuilder message = new StringBuilder();

        // =========================
        // BUSINESS / SHOP OWNER
        // =========================
        message.append("SHOP OWNER\n\n");

        message.append("Required: 3 primary business verification documents\n\n");

        message.append("1. DTI Certificate of Business Name Registration ")
                .append("or SEC Certificate of Incorporation\n\n");

        message.append("2. Mayor's / Business Permit\n\n");

        message.append("3. BIR Certificate of Registration (Form 2303)\n\n");

        message.append("Supporting Documents:\n")
                .append("• Barangay Business Clearance\n")
                .append("• Fire Safety Inspection Certificate (FSIC)\n")
                .append("• Occupancy Permit\n")
                .append("• Other valid government-issued business document\n\n");

        // =========================
        // FREELANCER
        // =========================
        message.append("FREELANCER\n\n");

        message.append("Required: 3 verification documents\n\n");

        message.append("1. One (1) Valid Government-Issued ID\n\n");

        message.append("2. One (1) Valid Certificate or Registration Document\n")
                .append("   Examples: Barangay Certificate, Barangay Clearance, ")
                .append("DTI Certificate, or other applicable government-issued certificate\n\n");

        message.append("3. One (1) Proof of Work or Service Experience\n")
                .append("   Examples: Previous service records, client receipt, ")
                .append("service/work portfolio, certificate of employment, ")
                .append("training certificate, or other document demonstrating ")
                .append("relevant vehicle-service work\n\n");

        // =========================
        // SUPPORTING DOCUMENTS
        // =========================
        message.append("ADDITIONAL SUPPORTING DOCUMENTS\n\n");

        message.append("If a primary document is unavailable, you may provide ")
                .append("additional supporting documents for review. These may include:\n")
                .append("• Barangay Clearance or Certificate\n")
                .append("• Business-related certificates\n")
                .append("• Fire Safety Inspection Certificate (FSIC)\n")
                .append("• Occupancy Permit\n")
                .append("• Training or skills certificates\n")
                .append("• Service records or work receipts\n")
                .append("• Proof of previous vehicle-service work\n")
                .append("• Other valid government-issued documents\n\n");

        message.append("Note: All submitted documents must be valid, readable, ")
                .append("and consistent with the information provided during registration. ")
                .append("Supporting documents may be requested when additional verification ")
                .append("is needed.");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Accepted Verification Documents");

        builder.setMessage(message.toString());

        builder.setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_PROFILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUriProfile = data.getData();
            uploadImage.setImageURI(imageUriProfile);
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            serviceAddPhotoLayout.setVisibility(View.GONE);
            imgPhoto.setVisibility(View.VISIBLE);
            imgPhoto.setImageURI(imageUri);
        }

        if (requestCode == PICK_IMAGE_REQUEST2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri2 = data.getData();
            serviceAddPhotoLayout2.setVisibility(View.GONE);
            imgPhoto2.setVisibility(View.VISIBLE);
            imgPhoto2.setImageURI(imageUri2);
        }

        if (requestCode == PICK_IMAGE_REQUEST3 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri3 = data.getData();
            serviceAddPhotoLayout3.setVisibility(View.GONE);
            imgPhoto3.setVisibility(View.VISIBLE);
            imgPhoto3.setImageURI(imageUri3);
        }

    }
    private void signupTheUser() {

        String shopName = shopNameET.getText().toString().trim();
        String email = emailAddressET.getText().toString().trim();
        String phonenumber = phoneNumberET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();

        String timestamp = String.valueOf(System.currentTimeMillis());


        error.setVisibility(View.GONE);

        // -------- VALIDATION --------

        if (shopName.isEmpty()) {
            shopNameET.setError("Shop name is required");
            shopNameET.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailAddressET.setError("Email is required");
            emailAddressET.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailAddressET.setError("Enter a valid email address");
            emailAddressET.requestFocus();
            return;
        }

        if (phonenumber.isEmpty()) {
            phoneNumberET.setError("Phone number is required");
            phoneNumberET.requestFocus();
            return;
        }

        // Check if input contains digits only
        if (!phonenumber.matches("\\d+")) {
            phoneNumberET.setError("Phone number must contain digits only");
            phoneNumberET.requestFocus();
            return;
        }

        // Check if input exceeds 11 digits
        if (phonenumber.length() > 11) {
            phoneNumberET.setError("Phone number cannot exceed 11 digits");
            phoneNumberET.requestFocus();
            return;
        }

        // Check if input has less than 11 digits
        if (phonenumber.length() < 11) {
            phoneNumberET.setError("Please enter an 11-digit Philippine phone number (e.g., 0932342523)");
            phoneNumberET.requestFocus();
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

        if (imageUri == null || imageUriProfile == null || imageUri2 == null || imageUri3 == null) {
            Toast.makeText(this, "Please select required images", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedLatitude == 0
                || selectedLongitude == 0
                || selectedAddress == null
                || selectedAddress.isEmpty()) {

            Toast.makeText(this, "Please select your location", Toast.LENGTH_SHORT).show();
            return;
        }

        // -------- START LOADING --------
        signupBtn.setEnabled(false);
        showLoading(true);


        FirebaseAuthManager.signupShopOwnerOrFreelancer(
                email,
                password,
                shopName,
                phonenumber,
                selectedRole,
                selectedAddress,
                imageUri,
                imageUriProfile,
                imageUri2,
                imageUri3,
                timestamp,
                selectedLatitude,
                selectedLongitude,
                new SignupCallback() {

                    @Override
                    public void onSuccess(String success, String imageUrls) {
                        String theUid = FirebaseAuthManager.getCurrentUserUid();
                        Log.d("UID_CHECK", theUid);
                        LocationsModel locationsModel = new LocationsModel(selectedLatitude, selectedLongitude, selectedAddress, theUid, imageUrls, shopName, selectedRole);
                        saveTheLocation(locationsModel, theUid);
                        showLoading(false);
                        signupBtn.setEnabled(true);

                        Intent intent = new Intent(
                                ShopOwnerOrFreelancerRegistration.this,
                                SuccessRegistrationActivity.class
                        );
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String failureMessage) {
                        showLoading(false);
                        signupBtn.setEnabled(true);
                        error.setText(failureMessage);
                        error.setVisibility(View.VISIBLE);
                    }
                }
        );
    }
    private void showLoading(boolean show) {
        if (show) {
            loadingAnimation.setVisibility(View.VISIBLE);
            loadingAnimation.playAnimation();
        } else {
            loadingAnimation.cancelAnimation();
            loadingAnimation.setVisibility(View.GONE);
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
    private void saveTheLocation(LocationsModel locationsModel, String uid) {
        FirebaseLocationsManager.saveLocation(locationsModel, uid, new LocationCallback() {
            @Override
            public void onSuccess(String success) {
                Toast.makeText(ShopOwnerOrFreelancerRegistration.this, success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String failed) {
                Toast.makeText(ShopOwnerOrFreelancerRegistration.this, failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setUpTextDisplays() {
        switch (selectedRole) {
            case "shop_owner":
                addProfileLabel.setText("Add shop photo");
                businessNameLabel.setText("Shop Name");
                businessLocationLabel.setText("Shop Location");
                shopNameET.setHint("Enter shop name");
                break;

                case "freelancer":
                addProfileLabel.setText("Add profile photo");
                businessNameLabel.setText("Business name");
                businessLocationLabel.setText("Location");
                shopNameET.setHint("Enter business name");
                break;

            default:
                addProfileLabel.setText("Add photo");
                businessNameLabel.setText("Name");
                businessLocationLabel.setText("Location");
                shopNameET.setHint("Enter name");
                break;
        }
    }
    private void checkStepOneBeforeStepTwo() {
        String shopName = shopNameET.getText().toString().trim();
        String email = emailAddressET.getText().toString().trim();
        String phonenumber = phoneNumberET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();

        String timestamp = String.valueOf(System.currentTimeMillis());


        error.setVisibility(View.GONE);

        // -------- VALIDATION --------

        if (shopName.isEmpty()) {
            shopNameET.setError("Shop name is required");
            shopNameET.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailAddressET.setError("Email is required");
            emailAddressET.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailAddressET.setError("Enter a valid email address");
            emailAddressET.requestFocus();
            return;
        }

        if (phonenumber.isEmpty()) {
            phoneNumberET.setError("Phone number is required");
            phoneNumberET.requestFocus();
            return;
        }

        // Check if input contains digits only
        if (!phonenumber.matches("\\d+")) {
            phoneNumberET.setError("Phone number must contain digits only");
            phoneNumberET.requestFocus();
            return;
        }

        // Check if input exceeds 11 digits
        if (phonenumber.length() > 11) {
            phoneNumberET.setError("Phone number cannot exceed 11 digits");
            phoneNumberET.requestFocus();
            return;
        }

        // Check if input has less than 11 digits
        if (phonenumber.length() < 11) {
            phoneNumberET.setError("Please enter an 11-digit Philippine phone number (e.g., 0932342523)");
            phoneNumberET.requestFocus();
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

        if (imageUriProfile == null) {
            Toast.makeText(this, "Please upload your photo", Toast.LENGTH_SHORT).show();
            return;
        }

        TransitionManager.beginDelayedTransition(root, fadeThrough);
        stepOneLayout.setVisibility(View.GONE);
        stepTwoLayout.setVisibility(View.VISIBLE);
    }
}