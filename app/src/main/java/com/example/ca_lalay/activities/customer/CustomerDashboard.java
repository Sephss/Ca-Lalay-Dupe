package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// for maps import
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.ca_lalay.Session.SessionManager;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.auth.AccountSuspended;
import com.example.ca_lalay.firebase.FirebaseVehicleManager;
import com.example.ca_lalay.firebase.callback.CountCallback;
import com.example.ca_lalay.firebase.callback.CustomerDataCallback;
import com.example.ca_lalay.model.UserModel;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.ShopAdapter;
import com.example.ca_lalay.auth.LoginActivity;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.callback.ShopListCallback;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CustomerDashboard extends AppCompatActivity implements OnMapReadyCallback {
    // Imageview
    private ImageView goToProfilePage, goToMessages, profileImage;
    private TextView customerName;
    private EditText searchShop;
    // Linear layouts
    private LinearLayout browseServicesLayout,  myBookings, nearbyMaps, viewShopsRated, nameAndProfileLayout;

    private RecyclerView carShopsRV;
    private SessionManager sessionManager;

    private List<ShopOwnerModel> shopList;
    private ShopAdapter adapter;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private LinearLayout actionRequiredLayout;
    private Button registerNowBtn;

    private TextView viewAllBtn;

    private String currentLoggedInUserID;
    @Override
    protected void onResume() {
        super.onResume();
        checkRegisteredVehicle();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_dashboard);

        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        finish();

                        overridePendingTransition(
                                R.anim.fast_fade_in,
                                R.anim.fast_fade_out
                        );
                    }
                });

        // imageviews
        goToProfilePage = findViewById(R.id.goToProfilePage);
        goToMessages = findViewById(R.id.goToMessages);

        currentLoggedInUserID = FirebaseAuthManager.getCurrentUserUid();
        actionRequiredLayout = findViewById(R.id.actionRequiredLayout);
        registerNowBtn = findViewById(R.id.registerNowBtn);

        searchShop = findViewById(R.id.searchShop);
        sessionManager = new SessionManager(this);

        // linearlayouts
        browseServicesLayout = findViewById(R.id.browseServicesLayout);
        myBookings = findViewById(R.id.myBookings);
        nearbyMaps = findViewById(R.id.nearbyMaps);
        viewShopsRated = findViewById(R.id.viewShopsRated);

        customerName = findViewById(R.id.customerName);
        profileImage = findViewById(R.id.profileImage);
        nameAndProfileLayout = findViewById(R.id.nameAndProfileLayout);
        viewAllBtn = findViewById(R.id.viewAllBtn);

        carShopsRV = findViewById(R.id.carShopsRV);
        shopList = new ArrayList<>();
        adapter = new ShopAdapter(this, shopList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        );
        carShopsRV.setLayoutManager(layoutManager);
        carShopsRV.setAdapter(adapter);

        // For google maps
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapPreview);

        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        browseServicesLayout.setAlpha(0.5f);
        browseServicesLayout.setEnabled(false);

        setNavigationEnabled(false);
        fetchShops();
        checkRegisteredVehicle();

        searchShop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        goToProfilePage.setOnClickListener(s -> {
            Intent intent = new Intent(CustomerDashboard.this, CustomerProfile.class);
            startActivity(intent);
            overridePendingTransition(
                    R.anim.fast_fade_in,
                    R.anim.fast_fade_out
            );
        });

        browseServicesLayout.setOnClickListener(s -> {
            navigateTo(CustomerBrowserServices.class);
        });

        viewAllBtn.setOnClickListener(d -> {
            navigateTo(CustomerViewAllShops.class);
        });

        viewAllBtn.setPaintFlags(
                viewAllBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );

        goToMessages.setOnClickListener(d -> {
            navigateTo(CustomerMessages.class);
            overridePendingTransition(
                    R.anim.fast_fade_in,
                    R.anim.fast_fade_out
            );
        });

        myBookings.setOnClickListener(s -> {
            navigateTo(CustomerMyBookings.class);
        });

        viewShopsRated.setOnClickListener(d -> {
            navigateTo(CustomerViewShopsRated.class);
        });

        nearbyMaps.setOnClickListener(l -> {
            navigateTo(CustomerViewMaps.class);

        });
        registerNowBtn.setOnClickListener(s -> {
            navigateTo(CustomerMyVehicles.class);
        });
        setUpData();

    }
    private void checkRegisteredVehicle() {
        FirebaseVehicleManager.getTotalVehiclesCount(currentLoggedInUserID, new CountCallback() {
            @Override
            public void onSuccess(int count) {
                if(count == 0) {
                    actionRequiredLayout.setVisibility(View.VISIBLE);
                    browseServicesLayout.setAlpha(0.5f);
                    browseServicesLayout.setEnabled(false);
                } else {
                    actionRequiredLayout.setVisibility(View.GONE);
                    browseServicesLayout.setAlpha(1.0f);
                    browseServicesLayout.setEnabled(true);
                }

            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CustomerDashboard.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setUpData() {
        String uid = FirebaseAuthManager.getCurrentUserUid();
        FirebaseDatabaseManager.getCustomerData(uid, new CustomerDataCallback() {
            @Override
            public void onSuccess(String message, UserModel customerData) {
                if(customerData.getStatus().equals("suspended")) {
                    FirebaseAuthManager.logout();
                    UserSession.getInstance().clearSession();
                    ShopOwnerSession.getInstance().clearSession();
                    sessionManager.logout();
                    Intent intent = new Intent(CustomerDashboard.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                UserSession.getInstance().setCurrentUser(customerData);
                customerName.setText("Hello! " + limitCustomerName(customerData.getFirstname()));
                setNavigationEnabled(true);
                Picasso.get().load(customerData.getImageUrl()).placeholder(R.drawable.sent_image_placeholder).into(profileImage);
                nameAndProfileLayout.setVisibility(View.VISIBLE);


            }

            @Override
            public void onFailure(String message) {

            }
        });
    }
    private void setNavigationEnabled(boolean enabled) {
        goToProfilePage.setEnabled(enabled);
        goToMessages.setEnabled(enabled);


        myBookings.setEnabled(enabled);
        nearbyMaps.setEnabled(enabled);

        // Optional: make it visually obvious that they are disabled
        float alpha = enabled ? 1.0f : 0.5f;

        goToProfilePage.setAlpha(alpha);
        goToMessages.setAlpha(alpha);


        myBookings.setAlpha(alpha);
        nearbyMaps.setAlpha(alpha);
    }
    // custom method for navigation
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
    }
    private void fetchShops() {

        FirebaseDatabaseManager.getAllShopOwners(new ShopListCallback() {
            @Override
            public void onSuccess(List<ShopOwnerModel> shops) {
                adapter.updateData(shops);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CustomerDashboard.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // The bottom content sheet covers 60% of the screen height, so pad the map
        // by that much on the bottom. This shifts the map's effective visual center
        // up into the visible strip instead of centering across the full (partially hidden) map.
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int bottomPadding = (int) (screenHeight * 0.60);
        mMap.setPadding(0, 0, 0, bottomPadding);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {

                            LatLng currentLocation = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("You are here"));

                            mMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(currentLocation, 16)
                            );

                        }
                    });

        } else {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }
    private String limitCustomerName(String name) {

        int maxLength = 20;

        if (name == null) {
            return "";
        }

        name = name.trim();

        if (name.length() > maxLength) {
            return name.substring(0, maxLength - 3) + "...";
        }

        return name;
    }
}