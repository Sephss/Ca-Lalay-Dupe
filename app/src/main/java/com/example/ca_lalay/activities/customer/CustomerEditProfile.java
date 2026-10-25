package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.callback.UpdateUserCallback;
import com.example.ca_lalay.model.UserModel;
import com.squareup.picasso.Picasso;

public class CustomerEditProfile extends AppCompatActivity {

    private ImageView selectImage;
    private EditText firstnameET, lastnameET, contactnumET;
    private Button saveBtn;
    private ImageView backBtn;

    private UserModel currentUser;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    selectImage.setImageURI(imageUri);
                }
            });
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_edit_profile);

        selectImage = findViewById(R.id.selectImage);
        firstnameET = findViewById(R.id.firstNameET);
        lastnameET = findViewById(R.id.lastNameET);
        contactnumET = findViewById(R.id.phoneNumberET);
        saveBtn = findViewById(R.id.saveBtn);

        currentUser = UserSession.getInstance().getCurrentUser();

        backBtn = findViewById(R.id.backBtn);

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

        setupDatas();

        selectImage.setOnClickListener(v -> choosePhotoFromGallery());
        saveBtn.setOnClickListener(v -> updateData());

        backBtn.setOnClickListener(s -> {
            finish();
        });
    }

    private void setupDatas() {
        if (currentUser == null) {
            Toast.makeText(this, "Unable to load profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firstnameET.setText(currentUser.getFirstname());
        lastnameET.setText(currentUser.getLastname());
        contactnumET.setText(currentUser.getContactNum());

        if (!TextUtils.isEmpty(currentUser.getImageUrl())) {
            Picasso.get().load(currentUser.getImageUrl()).placeholder(R.drawable.sent_image_placeholder).into(selectImage);
        }
    }

    private void updateData() {
        String firstname = firstnameET.getText().toString().trim();
        String lastname = lastnameET.getText().toString().trim();
        String phoneNumber = contactnumET.getText().toString().trim();

        // Validate first name
        if (firstname.isEmpty()) {
            firstnameET.setError("First name is required");
            firstnameET.requestFocus();
            return;
        }

        // Validate last name
        if (lastname.isEmpty()) {
            lastnameET.setError("Last name is required");
            lastnameET.requestFocus();
            return;
        }

        // Validate contact number
        if (phoneNumber.isEmpty()) {
            contactnumET.setError("Contact number is required");
            contactnumET.requestFocus();
            return;
        }

        // Contact number must contain digits only
        if (!phoneNumber.matches("\\d+")) {
            contactnumET.setError("Contact number must contain digits only");
            contactnumET.requestFocus();
            return;
        }

        // Contact number must be exactly 11 digits
        if (phoneNumber.length() != 11) {
            contactnumET.setError("Contact number must be exactly 11 digits");
            contactnumET.requestFocus();
            return;
        }

        String uid = FirebaseAuthManager.getCurrentUserUid();

        UserModel data = new UserModel();
        data.setUid(uid);
        data.setFirstname(firstnameET.getText().toString().trim());
        data.setLastname(lastnameET.getText().toString().trim());
        data.setContactNum(contactnumET.getText().toString().trim());


        String oldImage = currentUser.getImageUrl();

        setLoadingState();

        if (imageUri != null) {

            FirebaseStorageManager.uploadImage(
                    "CustomerNewProfile",
                    imageUri,
                    new FirebaseStorageManager.ImageUploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            data.setImageUrl(imageUrl);
                            updateTheData(data);
                            setNormalState();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(
                                    CustomerEditProfile.this,
                                    errorMessage,
                                    Toast.LENGTH_SHORT
                            ).show();

                            setNormalState();
                        }
                    }
            );

        } else {

            data.setImageUrl(oldImage);
            updateTheData(data);
        }
    }

    private void updateTheData(UserModel data) {
        FirebaseDatabaseManager.updateCustomerData(data, new UpdateUserCallback() {
            @Override
            public void onSuccess(String message) {
                currentUser.setImageUrl(data.getImageUrl());
                currentUser.setFirstname(data.getFirstname());
                currentUser.setLastname(data.getLastname());
                currentUser.setContactNum(data.getContactNum());
                Toast.makeText(CustomerEditProfile.this, message, Toast.LENGTH_SHORT).show();
                setNormalState();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(CustomerEditProfile.this, message, Toast.LENGTH_SHORT).show();
                setNormalState();
            }
        });
    }

    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void setLoadingState() {
        saveBtn.setEnabled(false);
        saveBtn.setAlpha(0.5f);
        saveBtn.setText("SAVING...");
    }

    private void setNormalState() {
        saveBtn.setEnabled(true);
        saveBtn.setAlpha(1f);
        saveBtn.setText("SAVE");
    }
}