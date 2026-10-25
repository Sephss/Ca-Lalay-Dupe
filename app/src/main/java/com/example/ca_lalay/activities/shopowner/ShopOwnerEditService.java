package com.example.ca_lalay.activities.shopowner;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseCarServicesManager;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.callback.CarServicesCallback;
import com.example.ca_lalay.firebase.callback.ServiceDetailsCallback;
import com.example.ca_lalay.model.ShopOwnerServicesModel;
import com.squareup.picasso.Picasso;

public class ShopOwnerEditService extends AppCompatActivity {

    private EditText serviceNameET, serviceDescriptionET, serviceDurationET, servicePriceET, mediumPercentageET, largePercentageET;
    private ImageView imgPhoto, backBtn;

    private String uid;
    private String serviceId;

    private LinearLayout serviceInactiveWarningLayout;

    private Button saveBtn, deactivateBtn, reactivateBtn;
    private Uri imageUri;

    private ShopOwnerServicesModel currentService;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    imgPhoto.setImageURI(imageUri);
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
        setContentView(R.layout.activity_shop_owner_edit_service);

        serviceNameET = findViewById(R.id.serviceNameET);
        serviceDescriptionET = findViewById(R.id.serviceDescriptionET);
        serviceDurationET = findViewById(R.id.serviceDurationET);
        servicePriceET = findViewById(R.id.servicePriceET);
        imgPhoto = findViewById(R.id.imgPhoto);

        mediumPercentageET = findViewById(R.id.mediumPercentageET);
        largePercentageET = findViewById(R.id.largePercentageET);

        backBtn = findViewById(R.id.backBtn);

        saveBtn = findViewById(R.id.saveBtn);
        deactivateBtn = findViewById(R.id.inactiveBtn);
        reactivateBtn = findViewById(R.id.reactivateBtn);

        serviceInactiveWarningLayout = findViewById(R.id.serviceInactiveWarningLayout);


        uid = FirebaseAuthManager.getCurrentUserUid();

        Intent intent = getIntent();
        serviceId = intent.getStringExtra("serviceId");

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
        fetchData();

        imgPhoto.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });

        backBtn.setOnClickListener(s -> {
            finish();
        });

        deactivateBtn.setOnClickListener(s -> {

            new AlertDialog.Builder(this)
                    .setTitle("Deactivate Service")
                    .setMessage("Are you sure you want to deactivate this service?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Deactivate", (dialog, which) -> {
                        deactivateTheService();
                    })
                    .show();
        });


        reactivateBtn.setOnClickListener(d -> {

            new AlertDialog.Builder(this)
                    .setTitle("Reactivate Service")
                    .setMessage("Are you sure you want to reactivate this service?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reactivate", (dialog, which) -> {
                        activateTheService();
                    })
                    .show();
        });


        saveBtn.setOnClickListener(v -> updateData());
    }

    private void fetchData() {

        FirebaseCarServicesManager.getServiceDetails(uid, serviceId,
                new ServiceDetailsCallback() {

                    @Override
                    public void onSuccess(ShopOwnerServicesModel service) {

                        currentService = service;

                        if ("active".equals(service.getStatus())) {
                            deactivateBtn.setVisibility(View.VISIBLE);
                            reactivateBtn.setVisibility(View.GONE);
                            serviceInactiveWarningLayout.setVisibility(View.GONE);
                        } else {
                            deactivateBtn.setVisibility(View.GONE);
                            reactivateBtn.setVisibility(View.VISIBLE);
                            serviceInactiveWarningLayout.setVisibility(View.VISIBLE);
                        }

                        serviceNameET.setText(service.getName());
                        serviceDescriptionET.setText(service.getDescription());
                        serviceDurationET.setText(service.getDurationMinutes());
                        servicePriceET.setText(service.getPrice());

                        mediumPercentageET.setText(service.getMediumPercentage());
                        largePercentageET.setText(service.getLargePercentage());


                        if (!TextUtils.isEmpty(service.getImageUrl())) {
                            Picasso.get()
                                    .load(service.getImageUrl())
                                    .into(imgPhoto);
                        }
                    }

                    @Override
                    public void onFailure(String failed) {
                        Toast.makeText(
                                ShopOwnerEditService.this,
                                failed,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void updateData() {

        if (currentService == null) {
            Toast.makeText(this, "Service data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get field values
        String name = serviceNameET.getText().toString().trim();
        String description = serviceDescriptionET.getText().toString().trim();
        String duration = serviceDurationET.getText().toString().trim();
        String price = servicePriceET.getText().toString().trim();
        String mediumPercentage = mediumPercentageET.getText().toString().trim();
        String largePercentage = largePercentageET.getText().toString().trim();

        // Validate required fields
        if (name.isEmpty()) {
            serviceNameET.setError("Service name is required");
            serviceNameET.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            serviceDescriptionET.setError("Description is required");
            serviceDescriptionET.requestFocus();
            return;
        }

        if (duration.isEmpty()) {
            serviceDurationET.setError("Duration is required");
            serviceDurationET.requestFocus();
            return;
        }

        if (price.isEmpty()) {
            servicePriceET.setError("Price is required");
            servicePriceET.requestFocus();
            return;
        }

        if (mediumPercentage.isEmpty()) {
            mediumPercentageET.setError("Medium percentage is required");
            mediumPercentageET.requestFocus();
            return;
        }

        if (largePercentage.isEmpty()) {
            largePercentageET.setError("Large percentage is required");
            largePercentageET.requestFocus();
            return;
        }

        // Update model only after validation passes
        currentService.setName(name);
        currentService.setDescription(description);
        currentService.setDurationMinutes(duration);
        currentService.setPrice(price);
        currentService.setMediumPercentage(mediumPercentage);
        currentService.setLargePercentage(largePercentage);

        setLoadingState();

        if (imageUri != null) {

            FirebaseStorageManager.updateProfileImage(
                    "ServiceImage",
                    currentService.getImageUrl(),
                    imageUri,
                    new FirebaseStorageManager.ImageUploadCallback() {

                        @Override
                        public void onSuccess(String imageUrl) {
                            currentService.setImageUrl(imageUrl);
                            updateTheData();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(
                                    ShopOwnerEditService.this,
                                    errorMessage,
                                    Toast.LENGTH_SHORT
                            ).show();

                            setNormalState();
                        }
                    });

        } else {
            // Keep the existing image
            updateTheData();
        }
    }

    private void updateTheData() {

        FirebaseCarServicesManager.updateServiceData(
                currentService,
                new CarServicesCallback() {

                    @Override
                    public void onSuccess(
                            String message,
                            String serviceId,
                            String serviceProviderId
                    ) {

                        Toast.makeText(
                                ShopOwnerEditService.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                        setNormalState();
                        finish();
                    }

                    @Override
                    public void onFailure(String message) {

                        Toast.makeText(
                                ShopOwnerEditService.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                        setNormalState();
                    }
                });
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
    private void activateTheService() {
        FirebaseCarServicesManager.updateServiceStatus(uid, serviceId, "active", new CarServicesCallback() {
            @Override
            public void onSuccess(String success, String serviceId, String serviceProviderId) {
                serviceInactiveWarningLayout.setVisibility(View.GONE);
                deactivateBtn.setVisibility(View.VISIBLE);
                reactivateBtn.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String failed) {

            }
        });
    }
    private void    deactivateTheService() {
        FirebaseCarServicesManager.updateServiceStatus(uid, serviceId, "inactive", new CarServicesCallback() {
            @Override
            public void onSuccess(String success, String serviceId, String serviceProviderId) {
                serviceInactiveWarningLayout.setVisibility(View.VISIBLE);
                deactivateBtn.setVisibility(View.GONE);
                reactivateBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String failed) {

            }
        });
    }


    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }
}