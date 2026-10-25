package com.example.ca_lalay.activities.customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.firebase.FirebaseLocationsManager;
import com.example.ca_lalay.firebase.callback.GetLocationsCallback;
import com.example.ca_lalay.model.LocationsModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class CustomerViewMaps extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ImageView backBtn;
    private FusedLocationProviderClient fusedLocationClient;

    // NEW: user's current coordinates (used to compute distance to each shop)
    private double userLatitude;
    private double userLongitude;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_view_maps);

        backBtn = findViewById(R.id.backBtn);

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
        backBtn.setOnClickListener(d -> {
            finish();
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

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

                            // NEW: save the user's coordinates for distance calculation
                            userLatitude = location.getLatitude();
                            userLongitude = location.getLongitude();

                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("You are here"));

                            mMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(currentLocation, 16)
                            );

                            fetchAllLocations();

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
        mMap.setOnMarkerClickListener(marker -> {

            LocationsModel location =
                    (LocationsModel) marker.getTag();

            if (location != null) {

                Intent intent = new Intent(
                        CustomerViewMaps.this,
                        CustomerShopLocationClicked.class
                );

                intent.putExtra("shopUid", location.getUid());

                startActivity(intent);
            }

            return true;
        });
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

    private void fetchAllLocations() {

        FirebaseLocationsManager.getAllActiveLocations(
                new GetLocationsCallback() {

                    @Override
                    public void onSuccess(
                            List<LocationsModel> locations
                    ) {

                        for (LocationsModel location : locations) {

                            LatLng shopLocation =
                                    new LatLng(
                                            location.getLatitude(),
                                            location.getLongitude()
                                    );

                            // NEW: estimate distance between user and this shop
                            String distanceText = getDistanceText(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

                            Marker marker = mMap.addMarker(
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

                            marker.setTag(location);

                            Log.d(
                                    "MAP_LOCATION",
                                    "Active shop: "
                                            + location.getShopName()
                            );
                        }
                    }

                    @Override
                    public void onFailure(String error) {

                        Log.e(
                                "MAP_LOCATION",
                                error
                        );
                    }
                }
        );
    }

    // NEW: straight-line distance between the user and a shop, formatted for display
    private String getDistanceText(double shopLatitude, double shopLongitude) {

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
        tvDistance.setText(distanceText);

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