package com.example.ca_lalay.activities.customer;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.CustomerVehicleAdapter;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.FirebaseVehicleManager;
import com.example.ca_lalay.firebase.callback.CountCallback;
import com.example.ca_lalay.firebase.callback.CustomerVehicleCallback;
import com.example.ca_lalay.firebase.callback.CustomerVehicleListCallback;
import com.example.ca_lalay.model.CustomerVehicleModel;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.firebase.Firebase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CustomerMyVehicles extends AppCompatActivity {
    private Button addNewVehicleBtn, saveBtn, cancelBtn;
    private LinearLayout saveCancelLayout, vehicleForm, vehicleDisplay, serviceAddPhotoLayout;

    private TextView totalVehicleTV, totalCarsTV, totalMotorcycleTV;

    private EditText etVehicleName, etPlateNumber;
    private Spinner spinnerVehicleType, spinnerSize, spinnerWheels;

    private ConstraintLayout root;
    private MaterialFadeThrough fadeThrough;

    private RecyclerView myVehiclesRecyclerview;
    private CustomerVehicleAdapter adapter;
    private List<CustomerVehicleModel> vehicleList;

    private ImageView imgPhoto;

    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    private String userID;

    private int totalVehicleUI = 0;
    private int totalCarUI = 0;
    private int totalMotorcycleUI = 0;

    private ImageView backBtn;

    @Override
    public void onBackPressed() {

        if (vehicleForm.getVisibility() == View.VISIBLE) {

            TransitionManager.beginDelayedTransition(root, fadeThrough);

            saveCancelLayout.setVisibility(View.GONE);
            addNewVehicleBtn.setVisibility(View.VISIBLE);

            vehicleForm.setVisibility(View.GONE);
            vehicleDisplay.setVisibility(View.VISIBLE);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_my_vehicles);

        addNewVehicleBtn = findViewById(R.id.addNewVehicleBtn);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveCancelLayout = findViewById(R.id.saveCancelLayout);
        vehicleForm = findViewById(R.id.vehicleForm);
        vehicleDisplay = findViewById(R.id.vehicleDisplay);

        backBtn = findViewById(R.id.backBtn);

        userID = FirebaseAuthManager.getCurrentUserUid();

        totalVehicleTV = findViewById(R.id.totalVehicleTV);
        totalCarsTV = findViewById(R.id.totalCarsTV);
        totalMotorcycleTV = findViewById(R.id.totalMotorcycleTV);

        etVehicleName = findViewById(R.id.etVehicleName);
        etPlateNumber = findViewById(R.id.etPlateNumber);
        spinnerVehicleType = findViewById(R.id.spinnerVehicleType);
        spinnerSize = findViewById(R.id.spinnerSize);
        spinnerWheels = findViewById(R.id.spinnerWheels);
        imgPhoto = findViewById(R.id.imgPhoto);
        serviceAddPhotoLayout = findViewById(R.id.serviceAddPhotoLayout);

        root = findViewById(R.id.main);

        fadeThrough = new MaterialFadeThrough();
        fadeThrough.setDuration(300);

        myVehiclesRecyclerview = findViewById(R.id.myVehiclesRecyclerview);

        vehicleList = new ArrayList<>();

        adapter = new CustomerVehicleAdapter(
                this,
                vehicleList,
                (vehicle, position) -> {

                    new AlertDialog.Builder(CustomerMyVehicles.this)
                            .setTitle("Delete Vehicle")
                            .setMessage(
                                    "This action cannot be undone.\n\nVehicle: "
                                            + vehicle.getVehicleName()
                                            + "\nPlate No: "
                                            + vehicle.getVehiclePlateNumber()
                            )
                            .setPositiveButton("Delete", (dialog, which) -> {

                                FirebaseVehicleManager.deleteVehicle(
                                        userID,
                                        vehicle.getVehicleId(),
                                        new CustomerVehicleCallback() {

                                            @Override
                                            public void onSuccess(String message) {

                                                int index = vehicleList.indexOf(vehicle);

                                                if (index != -1) {
                                                    vehicleList.remove(index);
                                                    adapter.notifyItemRemoved(index);
                                                }

                                                Toast.makeText(
                                                        CustomerMyVehicles.this,
                                                        message,
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                setUpCountsUIDelete(
                                                        vehicle.getVehicleType()
                                                );
                                            }

                                            @Override
                                            public void onFailure(String error) {

                                                Toast.makeText(
                                                        CustomerMyVehicles.this,
                                                        error,
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        });
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                });

        myVehiclesRecyclerview.setLayoutManager(
                new LinearLayoutManager(this)
        );

        myVehiclesRecyclerview.setAdapter(adapter);
        myVehiclesRecyclerview.setItemAnimator(new DefaultItemAnimator());

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
        fetchVehicles();

        saveBtn.setOnClickListener(g -> {

            TransitionManager.beginDelayedTransition(root, fadeThrough);

            addNewVehicle();
        });

        backBtn.setOnClickListener(s -> {
            finish();
        });

        addNewVehicleBtn.setOnClickListener(v -> {

            TransitionManager.beginDelayedTransition(root, fadeThrough);

            saveCancelLayout.setVisibility(View.VISIBLE);
            addNewVehicleBtn.setVisibility(View.GONE);

            vehicleForm.setVisibility(View.VISIBLE);
            vehicleDisplay.setVisibility(View.GONE);

        });

        cancelBtn.setOnClickListener(v -> {

            TransitionManager.beginDelayedTransition(root, fadeThrough);

            saveCancelLayout.setVisibility(View.GONE);
            addNewVehicleBtn.setVisibility(View.VISIBLE);

            vehicleForm.setVisibility(View.GONE);
            vehicleDisplay.setVisibility(View.VISIBLE);
        });
        serviceAddPhotoLayout.setOnClickListener(g -> {
            choosePhotoFromGallery();

        });

        imgPhoto.setOnClickListener(f -> {
            choosePhotoFromGallery();
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

        if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            serviceAddPhotoLayout.setVisibility(View.GONE);
            imgPhoto.setVisibility(View.VISIBLE);

            imageUri = data.getData();
            Picasso.get().load(imageUri).fit().centerCrop().into(imgPhoto);

        }
    }

    private void addNewVehicle () {
        String id = FirebaseAuthManager.getCurrentUserUid();
        String vehicleName = etVehicleName.getText().toString().trim();
        String plateNumber = etPlateNumber.getText().toString().trim();
        String vehicleType = spinnerVehicleType.getSelectedItem().toString();
        String vehicleSize = spinnerSize.getSelectedItem().toString();
        String vehicleWheels =  "none";

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (vehicleName.isEmpty()) {
            etVehicleName.setError("Vehicle name is required");
            etVehicleName.requestFocus();
            return;
        }

        if (plateNumber.isEmpty()) {
            etPlateNumber.setError("Plate number is required");
            etPlateNumber.requestFocus();
            return;
        }

        if (vehicleType.equals("Select Type")) {
            Toast.makeText(this, "Please select a vehicle type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (vehicleSize.equals("Select Size")) {
            Toast.makeText(this, "Please select a vehicle size", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState();

        vehicleImageUpload(new FirebaseStorageManager.ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                CustomerVehicleModel vehicleModel = new CustomerVehicleModel(imageUrl, vehicleName, plateNumber, vehicleType, vehicleSize, vehicleWheels, "", id);

                FirebaseVehicleManager.createUserVehicle(vehicleModel, id, new CustomerVehicleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(CustomerMyVehicles.this, message, Toast.LENGTH_SHORT).show();

                        TransitionManager.beginDelayedTransition(root, fadeThrough);
                        saveCancelLayout.setVisibility(View.GONE);
                        addNewVehicleBtn.setVisibility(View.VISIBLE);
                        vehicleForm.setVisibility(View.GONE);
                        vehicleDisplay.setVisibility(View.VISIBLE);

                        vehicleList.add(0, vehicleModel);
                        adapter.notifyItemInserted(0);

                        myVehiclesRecyclerview.scrollToPosition(0);
                        clearFields();
                        setNormalState();
                        setUpCountsUIAdd(vehicleType);
                    }

                    @Override
                    public void onFailure(String failed) {
                        setNormalState();
                        Toast.makeText(CustomerMyVehicles.this, failed, Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onFailure(String errorMessage) {
                setNormalState();
                Toast.makeText(CustomerMyVehicles.this, "Failed to upload image " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });




    }
    private void vehicleImageUpload (FirebaseStorageManager.ImageUploadCallback callback) {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorageManager.uploadVehicleImage("vehicles", imageUri, callback);
    }
    private void fetchVehicles() {
        setupCounts();

        String uid = FirebaseAuthManager.getCurrentUserUid();

        FirebaseVehicleManager.getUserVehicles(
                uid,
                new CustomerVehicleListCallback() {

                    @Override
                    public void onSuccess(List<CustomerVehicleModel> vehicles) {

                        vehicleList.clear();

                        vehicleList.addAll(vehicles);

                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFailure(String errorMessage) {

                        Toast.makeText(
                                CustomerMyVehicles.this,
                                errorMessage,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
    private void clearFields() {
        etVehicleName.setText("");
        etPlateNumber.setText("");
        spinnerVehicleType.setSelection(0);
        spinnerSize.setSelection(0);
        spinnerWheels.setSelection(0);
        serviceAddPhotoLayout.setVisibility(View.VISIBLE);
        imgPhoto.setVisibility(View.GONE);
    }
    private void setupCounts() {
        String uid = FirebaseAuthManager.getCurrentUserUid();
        FirebaseVehicleManager.getTotalVehiclesCount(uid, new CountCallback() {
            @Override
            public void onSuccess(int count) {
                String theCount = String.valueOf(count);
                totalVehicleTV.setText(theCount);
                totalVehicleUI = count;
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CustomerMyVehicles.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseVehicleManager.getTotalCarsCount(uid, new CountCallback() {
            @Override
            public void onSuccess(int count) {
                String carCount = String.valueOf(count);
                totalCarsTV.setText(carCount);
                totalCarUI = count;
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CustomerMyVehicles.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseVehicleManager.getTotalMotorcyclesCount(uid, new CountCallback() {
            @Override
            public void onSuccess(int count) {
                String motorcycleCount = String.valueOf(count);
                totalMotorcycleTV.setText(motorcycleCount);
                totalMotorcycleUI = count;
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CustomerMyVehicles.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setUpCountsUIDelete(String type) {
        if(type.equals("Sedan") || type.equals("SUV") || type.equals("Hatchback") || type.equals("Pickup Truck")) {
            totalCarUI --;
            totalVehicleUI --;
            totalVehicleTV.setText(String.valueOf(totalVehicleUI));
            totalCarsTV.setText(String. valueOf(totalCarUI));
        } else if (type.equals("Motorcycle") || type.equals("Tricycle")) {
            totalMotorcycleUI --;
            totalVehicleUI --;
            totalVehicleTV.setText(String.valueOf(totalVehicleUI));
            totalMotorcycleTV.setText(String.valueOf(totalMotorcycleUI));
        }
    }
    private void setUpCountsUIAdd(String type) {
        if(type.equals("Sedan") || type.equals("SUV") || type.equals("Hatchback") || type.equals("Pickup Truck")) {
            totalCarUI ++;
            totalVehicleUI ++;
            totalVehicleTV.setText(String.valueOf(totalVehicleUI));
            totalCarsTV.setText(String. valueOf(totalCarUI));
        } else if (type.equals("Motorcycle") || type.equals("Tricycle")) {
            totalMotorcycleUI ++;
            totalVehicleUI ++;
            totalVehicleTV.setText(String.valueOf(totalVehicleUI));
            totalMotorcycleTV.setText(String.valueOf(totalMotorcycleUI));
        }
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