package com.example.ca_lalay.activities.shopowner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ShopOwnerSelectLocation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageView backBtn;
    private Button confirmBtn;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_select_location);

        backBtn = findViewById(R.id.backBtn);
        confirmBtn = findViewById(R.id.confirmBtn);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapPreview);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        backBtn.setOnClickListener(v -> finish());

        confirmBtn.setOnClickListener(v -> {

            if (mMap == null) return;

            LatLng center = mMap.getCameraPosition().target;
            double latitude = center.latitude;
            double longitude = center.longitude;

            String address = "";
            String municipality = "";

            try {
                Geocoder geocoder = new Geocoder(ShopOwnerSelectLocation.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address result = addresses.get(0);
                    address = result.getAddressLine(0);
                    municipality = result.getLocality(); // usually city/municipality
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean isValidLocation =
                    municipality != null
                            && BATAAN_MUNICIPALITIES.contains(municipality.trim().toLowerCase(Locale.ROOT));

            if (!isValidLocation) {
                Toast.makeText(
                        ShopOwnerSelectLocation.this,
                        "Please select a location within Bataan (Balanga, Morong, Orani, etc.)",
                        Toast.LENGTH_LONG
                ).show();
                return; // block confirm, don't finish()
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", latitude);
            resultIntent.putExtra("longitude", longitude);
            resultIntent.putExtra("address", address);

            setResult(RESULT_OK, resultIntent);
            finish();
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

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {

                        if (location != null) {

                            LatLng currentLocation =
                                    new LatLng(
                                            location.getLatitude(),
                                            location.getLongitude()
                                    );

                            mMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                            currentLocation,
                                            16f
                                    )
                            );
                        }
                    });

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == 1
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {

                            if (location != null) {

                                LatLng currentLocation =
                                        new LatLng(
                                                location.getLatitude(),
                                                location.getLongitude()
                                        );

                                mMap.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                                currentLocation,
                                                16f
                                        )
                                );
                            }
                        });
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
    private static final Set<String> BATAAN_MUNICIPALITIES = new HashSet<>(Arrays.asList(
            "balanga city", "abucay", "bagac", "dinalupihan", "hermosa",
            "limay", "mariveles", "morong", "orani", "orion",
            "pilar", "samal"
    ));
}