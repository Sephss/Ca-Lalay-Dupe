package com.example.ca_lalay.activities.customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.firebase.FirebaseLocationsManager;
import com.example.ca_lalay.firebase.callback.GetSingleLocationCallback;
import com.example.ca_lalay.model.LocationsModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class CustomerLocateShop extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;

    private String shopUid;

    private ImageView backBtn;
    private TextView theShopName;

    // NEW: used to get the user's current location for the distance estimate
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(
                R.anim.stay,
                R.anim.slide_down
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_locate_shop);

        Intent intent = getIntent();
        shopUid = intent.getStringExtra("shopUid");

        backBtn = findViewById(R.id.backBtn);

        // NEW
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapPreview);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        backBtn.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
        }

        fetchShopLocation();
    }

    private void fetchShopLocation() {

        FirebaseLocationsManager.getLocation(
                shopUid,
                new GetSingleLocationCallback() {

                    @Override
                    public void onSuccess(LocationsModel location) {

                        LatLng shopLocation = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        // NEW: get the user's location first, then show the marker
                        // with the distance. If the location isn't available, the
                        // marker is shown without a distance (same as before).
                        if (ActivityCompat.checkSelfPermission(
                                CustomerLocateShop.this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                                || ActivityCompat.checkSelfPermission(
                                CustomerLocateShop.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED) {

                            fusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(userLocation -> {

                                        String distanceText = "";

                                        if (userLocation != null) {
                                            distanceText = getDistanceText(
                                                    userLocation.getLatitude(),
                                                    userLocation.getLongitude(),
                                                    location.getLatitude(),
                                                    location.getLongitude()
                                            );
                                        }

                                        showShopMarker(location, shopLocation, distanceText);
                                    })
                                    .addOnFailureListener(e ->
                                            showShopMarker(location, shopLocation, "")
                                    );

                        } else {

                            showShopMarker(location, shopLocation, "");
                        }
                    }

                    @Override
                    public void onFailure(String error) {

                        Toast.makeText(
                                CustomerLocateShop.this,
                                error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // MOVED (unchanged logic): this is the marker/camera code that used to sit
    // directly inside onSuccess. It now also receives the distance text.
    private void showShopMarker(
            LocationsModel location,
            LatLng shopLocation,
            String distanceText
    ) {

        mMap.clear();

        mMap.addMarker(
                new MarkerOptions()
                        .position(shopLocation)
                        .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                        createCustomMarker(
                                                location.getShopName(),
                                                distanceText
                                        )
                                )
                        )
        );

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                        shopLocation,
                        17f
                )
        );
    }

    // NEW: straight-line distance between the user and the shop, formatted for display
    private String getDistanceText(
            double userLatitude,
            double userLongitude,
            double shopLatitude,
            double shopLongitude
    ) {

        float[] results = new float[1];

        Location.distanceBetween(
                userLatitude,
                userLongitude,
                shopLatitude,
                shopLongitude,
                results
        );

        float distanceInMeters = results[0];

        if (distanceInMeters < 1000) {
            return String.format(
                    Locale.getDefault(),
                    "%d m",
                    Math.round(distanceInMeters)
            );
        } else {
            return String.format(
                    Locale.getDefault(),
                    "%.1f km",
                    distanceInMeters / 1000.0
            );
        }
    }

    // CHANGED: now also receives the distance text
    private Bitmap createCustomMarker(String shopName, String distanceText) {

        View markerView = getLayoutInflater()
                .inflate(R.layout.custom_marker, null);

        TextView tvShopName =
                markerView.findViewById(R.id.tvShopName);

        // NEW
        TextView tvDistance =
                markerView.findViewById(R.id.tvDistance);

        tvShopName.setText(shopName);

        // NEW: hide the distance if the user's location isn't available
        if (distanceText == null || distanceText.isEmpty()) {
            tvDistance.setVisibility(View.GONE);
        } else {
            tvDistance.setText(distanceText);
        }

        markerView.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
        );

        markerView.layout(
                0,
                0,
                markerView.getMeasuredWidth(),
                markerView.getMeasuredHeight()
        );

        Bitmap bitmap = Bitmap.createBitmap(
                markerView.getMeasuredWidth(),
                markerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);

        markerView.draw(canvas);

        return bitmap;
    }
}