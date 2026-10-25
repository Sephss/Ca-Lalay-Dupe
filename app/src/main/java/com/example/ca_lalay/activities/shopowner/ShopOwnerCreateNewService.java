package com.example.ca_lalay.activities.shopowner;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseCarServicesManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.callback.CarServicesCallback;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.model.ShopOwnerModel;

import java.util.Locale;

public class ShopOwnerCreateNewService extends AppCompatActivity {
    private Spinner serviceTypeSpinner;
    private EditText serviceNameET, serviceDescriptionET, serviceDurationET, servicePriceET;
    private LinearLayout serviceAddPhotoLayout;
    private ImageView imgPhoto, backBtn;
    private Button createBtn;

    private ShopOwnerModel currentShop;

    private LinearLayout setPercentageSizeLayout, percentageAdditionsLayout;
    private SwitchCompat sizeBasedSwitch;
    private Button setPercentageBtn;
    private String theMediumPercentage = "0";
    private String theLargePercentage = "0";
    private TextView theLargePricePreview, theMediumPricePreview;

    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    private String shopImageUrl, shopLocation, shopRating, shopContactNum;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_create_new_service);

        serviceTypeSpinner = findViewById(R.id.serviceTypeSpinner);
        serviceNameET = findViewById(R.id.serviceNameET);
        serviceDescriptionET = findViewById(R.id.serviceDescriptionET);
        serviceDurationET = findViewById(R.id.serviceDurationET);
        servicePriceET = findViewById(R.id.servicePriceET);
        serviceAddPhotoLayout = findViewById(R.id.serviceAddPhotoLayout);
        percentageAdditionsLayout = findViewById(R.id.percentageAdditionsLayout);
        imgPhoto = findViewById(R.id.imgPhoto);
        createBtn = findViewById(R.id.createBtn);

        backBtn = findViewById(R.id.backBtn);

        setPercentageSizeLayout = findViewById(R.id.setPercentageSizeLayout);
        sizeBasedSwitch = findViewById(R.id.sizeBasedSwitch);
        setPercentageBtn = findViewById(R.id.setPercentageBtn);
        theMediumPricePreview = findViewById(R.id.mediumPricePreview);
        theLargePricePreview = findViewById(R.id.largePricePreview);

        currentShop = ShopOwnerSession.getInstance().getCurrentShopOwner();

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
        setupSpinner();
        setPercentageBtn.setOnClickListener(s-> {
            openInputPercentageDialog();
        });
        sizeBasedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                setPercentageSizeLayout.setVisibility(View.VISIBLE);
            } else {
                setPercentageSizeLayout.setVisibility(View.GONE);
                percentageAdditionsLayout.setVisibility(View.GONE);
                theMediumPercentage = "0";
                theLargePercentage = "0";
            }

        });

        backBtn.setOnClickListener(s -> {
            finish();
        });

        createBtn.setOnClickListener(task -> {
            saveNewServiceToDatabase();
        });

        serviceAddPhotoLayout.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });

        imgPhoto.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });
    }
    private void saveNewServiceToDatabase() {
        String serviceType = serviceTypeSpinner.getSelectedItem().toString();
        String serviceName = serviceNameET.getText().toString();
        String serviceDescription = serviceDescriptionET.getText().toString();
        String serviceDuration = serviceDurationET.getText().toString();
        String servicePrice = servicePriceET.getText().toString();


        if(serviceType.equals("Select Service Type")) {
            Toast.makeText(this, "Please select a service type", Toast.LENGTH_SHORT).show();
            return;
        }
        if(serviceName.isEmpty()) {
            serviceNameET.setError("Service name is required");
            serviceNameET.requestFocus();
            return;
        }
        if(serviceDescription.isEmpty()) {
            serviceDescriptionET.setError("Service Description is required");
            serviceDescriptionET.requestFocus();
            return;
        }

        if(serviceDuration.isEmpty()) {
            serviceDurationET.setError("Service Duration is required");
            serviceDurationET.requestFocus();
            return;
        }

        if(servicePrice.isEmpty()) {
            servicePriceET.setError("Service Price is required");
            servicePriceET.requestFocus();
            return;
        }

        if(imageUri == null) {
            Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoadingState();

        String serviceProviderId = FirebaseAuthManager.getCurrentUserUid();
        String imageUrl = imageUri.toString();
        String createdAt = String.valueOf(System.currentTimeMillis());
        String status = "active";
        String rating = "0";
        String reviewCount = "0";
        String isAvailable = "true";

        FirebaseDatabaseManager.getShopOwnerData(serviceProviderId, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel data) {


                FirebaseStorageManager.uploadImage("ServiceImage", imageUri, new FirebaseStorageManager.ImageUploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        FirebaseCarServicesManager.createService(serviceName, data.getShopName(), data.getUid(), data.getContactNum(), data.getLocation(), data.getOverallRating(), currentShop.getShopImage(), serviceDescription, imageUrl, serviceProviderId, serviceType, servicePrice, serviceDuration, isAvailable, createdAt, status, rating, reviewCount, theMediumPercentage, theLargePercentage, currentShop.getShopOpenAt(), currentShop.getShopClosedAt(), new CarServicesCallback() {
                            @Override
                            public void onSuccess(String success, String serviceId, String serviceProviderId) {
                                Toast.makeText(ShopOwnerCreateNewService.this, success, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ShopOwnerCreateNewService.this, ShopOwnerDashboard.class);
                                startActivity(intent);
                                setNormalState();
                                finish();
                            }

                            @Override
                            public void onFailure(String failed) {
                                setNormalState();
                                Toast.makeText(ShopOwnerCreateNewService.this, failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        setNormalState();
                        Toast.makeText(ShopOwnerCreateNewService.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String failed) {
                setNormalState();
            }
        });

    }
    private void setupSpinner() {
        String[] serviceCategories = {
                "Select Service Type",
                "Oil Change",
                "Car Wash",
                "Tire Replacement",
                "Engine Repair",
                "Brake Service",
                "Battery Replacement",
                "Car Detailing",
                "Others"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_selected_item,
                serviceCategories
        );

        adapter.setDropDownViewResource(
                R.layout.spinner_dropdown_item
        );

        serviceTypeSpinner.setAdapter(adapter);

        serviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedService = parent.getItemAtPosition(position).toString();

                if (position != 0) {
                    // Do something with selected value
                    Toast.makeText(ShopOwnerCreateNewService.this,
                            "Selected: " + selectedService,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    public void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            serviceAddPhotoLayout.setVisibility(View.GONE);
            imgPhoto.setVisibility(View.VISIBLE);
            imgPhoto.setImageURI(imageUri);
        }
    }
    private void openInputPercentageDialog() {

        // Get base price from servicePriceET
        String priceText = servicePriceET.getText().toString().trim();

        if (priceText.isEmpty()) {
            servicePriceET.setError("Enter the base price first");
            servicePriceET.requestFocus();
            return;
        }

        double basePrice;

        try {
            basePrice = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            servicePriceET.setError("Invalid price");
            servicePriceET.requestFocus();
            return;
        }

        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_size_percentages, null);

        EditText mediumPercentageInput =
                dialogView.findViewById(R.id.mediumPercentageInput);

        EditText largePercentageInput =
                dialogView.findViewById(R.id.largePercentageInput);

        TextView basePricePreview =
                dialogView.findViewById(R.id.basePricePreview);

        TextView mediumPricePreview =
                dialogView.findViewById(R.id.mediumPricePreview);

        TextView largePricePreview =
                dialogView.findViewById(R.id.largePricePreview);

        // Show base price
        basePricePreview.setText(
                String.format(Locale.getDefault(),
                        "Base price: ₱%.2f", basePrice)
        );

        // Live preview for Medium
        mediumPercentageInput.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        updatePricePreview(
                                basePrice,
                                s.toString(),
                                mediumPricePreview
                        );
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        // Live preview for Large
        largePercentageInput.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        updatePricePreview(
                                basePrice,
                                s.toString(),
                                largePricePreview
                        );
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(view -> {

                        String mediumValue =
                                mediumPercentageInput
                                        .getText()
                                        .toString()
                                        .trim();


                        String largeValue =
                                largePercentageInput
                                        .getText()
                                        .toString()
                                        .trim();

                        if (mediumValue.isEmpty()) {
                            mediumPercentageInput.setError(
                                    "Enter a percentage"
                            );
                            mediumPercentageInput.requestFocus();
                            return;
                        }

                        if (largeValue.isEmpty()) {
                            largePercentageInput.setError(
                                    "Enter a percentage"
                            );
                            largePercentageInput.requestFocus();
                            return;
                        }

                        theMediumPercentage = mediumValue;
                        theLargePercentage = largeValue;
                        theMediumPricePreview.setText("Medium +" + mediumValue + "%");
                        theLargePricePreview.setText("Large +" + largeValue + "%");
                        percentageAdditionsLayout.setVisibility(View.VISIBLE);



                        double mediumPercentage;
                        double largePercentage;

                        try {
                            mediumPercentage =
                                    Double.parseDouble(mediumValue);

                            largePercentage =
                                    Double.parseDouble(largeValue);

                        } catch (NumberFormatException e) {

                            Toast.makeText(
                                    this,
                                    "Enter valid percentages",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        // Optional: prevent percentages over 100
                        if (mediumPercentage < 0 ||
                                mediumPercentage > 100) {

                            mediumPercentageInput.setError(
                                    "Percentage must be 0-100"
                            );
                            mediumPercentageInput.requestFocus();
                            return;
                        }

                        if (largePercentage < 0 ||
                                largePercentage > 100) {

                            largePercentageInput.setError(
                                    "Percentage must be 0-100"
                            );
                            largePercentageInput.requestFocus();
                            return;
                        }

                        // Calculate final prices
                        double mediumPrice =
                                basePrice +
                                        (basePrice * mediumPercentage / 100);

                        double largePrice =
                                basePrice +
                                        (basePrice * largePercentage / 100);

                        Log.d(
                                "SIZE_PRICING",
                                "Base: " + basePrice +
                                        ", Medium: " + mediumPrice +
                                        ", Large: " + largePrice
                        );

                        // Use these values here
                        // mediumPercentage
                        // largePercentage
                        // mediumPrice
                        // largePrice

                        dialog.dismiss();
                    });
        });

        dialog.show();
    }
    private void updatePricePreview(
            double basePrice,
            String percentageText,
            TextView pricePreview) {

        if (percentageText.isEmpty()) {
            pricePreview.setText("₱0.00");
            return;
        }

        try {

            double percentage =
                    Double.parseDouble(percentageText);

            double additional =
                    basePrice * percentage / 100;

            double finalPrice =
                    basePrice + additional;

            pricePreview.setText(
                    String.format(
                            Locale.getDefault(),
                            "₱%.2f",
                            finalPrice
                    )
            );

        } catch (NumberFormatException e) {

            pricePreview.setText("₱0.00");
        }
    }   private void setLoadingState() {
        createBtn.setEnabled(false);
        createBtn.setAlpha(0.5f);
        createBtn.setText("CREATING...");
    }

    private void setNormalState() {
        createBtn.setEnabled(true);
        createBtn.setAlpha(1f);
        createBtn.setText("CREATE");
    }
}