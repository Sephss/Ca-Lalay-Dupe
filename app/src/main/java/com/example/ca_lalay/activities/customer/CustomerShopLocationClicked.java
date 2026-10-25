package com.example.ca_lalay.activities.customer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseShopScheduleHelper;
import com.example.ca_lalay.firebase.callback.CustomerDataCallback;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.firebase.callback.ShopScheduleCallback;
import com.example.ca_lalay.model.ShopClosedDateModel;
import com.example.ca_lalay.model.ShopOpeningDaysModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.example.ca_lalay.model.ShopScheduleResult;
import com.example.ca_lalay.model.UserModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CustomerShopLocationClicked extends AppCompatActivity {
    private Button viewShopServicesAll;
    private ImageView shopImage, backBtn;
    private TextView theShopName, shopRating, shopOpenAt, shopContactNum, shopEmail, shopAbout;
    private String theShopid, theShopimage, theShopname, theShoprating, theShopopenat, theShopcontactnum, theShopemail, theShopabout;
    private LinearLayout messageLink, callLayout, shopViewLocation, shopRatings;
    private String shopID, TheShopUid, TheShopImage, TheShopName;
    private LinearLayout openingHoursLayout, shopLocationLayout;
    private TextView locationTV;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private boolean userLocationAvailable = false;
    private double userLatitude, userLongitude, shopLatitude, shopLongitude;
    private TextView shopDistance;

    private TextView providerBadge;
    private LinearLayout providerBadgeBG;
    private ImageView providerBadgeIcon;
    private String theProviderRole;

    private LinearLayout shopOpeningDaysLayout;
    private TextView shopOpeningDays;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_shop_location_clicked);

        shopImage = findViewById(R.id.shopImage);
        theShopName = findViewById(R.id.shopName);
        shopRating = findViewById(R.id.shopRating);
        shopOpenAt = findViewById(R.id.shopOpenAt);
        shopContactNum = findViewById(R.id.shopContactNum);
        shopEmail = findViewById(R.id.shopEmail);
        shopAbout = findViewById(R.id.shopAbout);
        backBtn = findViewById(R.id.backBtn);
        viewShopServicesAll = findViewById(R.id.viewShopServicesAll);
        callLayout = findViewById(R.id.callLayout);
        shopRatings = findViewById(R.id.shopRatings);
        messageLink = findViewById(R.id.messageLink);
        shopLocationLayout = findViewById(R.id.shopLocationLayout);
        locationTV = findViewById(R.id.locationTV);
        shopOpeningDaysLayout = findViewById(R.id.shopOpeningDaysLayout);
        shopOpeningDays = findViewById(R.id.shopOpeningDays);
        providerBadge = findViewById(R.id.providerBadge);
        providerBadgeBG = findViewById(R.id.providerBadgeBG);
        providerBadgeIcon = findViewById(R.id.providerBadgeIcon);


        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
        shopDistance = findViewById(R.id.shopDistance);

        openingHoursLayout = findViewById(R.id.openingHoursLayout);

        Intent intent = getIntent();
        shopID = intent.getStringExtra("shopUid");

        callLayout.setEnabled(false);
        messageLink.setEnabled(false);
        shopRatings.setEnabled(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        fetchData();

        shopOpeningDays.setOnClickListener(d -> {
            showShopScheduleBottomSheet(shopID);
        });

        shopOpeningDays.setPaintFlags(
                shopOpeningDays.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );

        backBtn.setOnClickListener(s -> {
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
        });
        shopRatings.setOnClickListener(d -> {
            Intent ds = new Intent(CustomerShopLocationClicked.this, CustomerReviewRatings.class);
            ds.putExtra("shopUid", TheShopUid);
            ds.putExtra("shopImage", TheShopImage);
            ds.putExtra("shopName", TheShopName);
            startActivity(ds);
        });
        callLayout.setOnClickListener(d -> {
            String phoneNumber = theShopcontactnum;

            Intent call = new Intent (Intent.ACTION_DIAL);
            call.setData(Uri.parse(("tel:" + phoneNumber)));
            startActivity(call);
        });
        viewShopServicesAll.setOnClickListener(s -> {
            Intent intent1 = new Intent(CustomerShopLocationClicked.this, CustomerViewShopAllServices.class);
            intent1.putExtra("shopId", shopID);
            startActivity(intent1);
            overridePendingTransition(
                    R.anim.slide_in_up,
                    R.anim.stay
            );
        });

        messageLink.setOnClickListener(v -> {

            String uid = FirebaseAuthManager.getCurrentUserUid();

            FirebaseDatabaseManager.getCustomerData(uid, new CustomerDataCallback() {
                @Override
                public void onSuccess(String message, UserModel customerData) {

                    Intent intent = new Intent(CustomerShopLocationClicked.this, CustomerMessageNow.class);

                    // Customer
                    intent.putExtra("customerId", uid);
                    intent.putExtra("customerName", customerData.getFirstname() + " " + customerData.getLastname());
                    intent.putExtra("customerImage", customerData.getImageUrl());

                    // Shop
                    intent.putExtra("shopId", shopID);
                    intent.putExtra("shopName", TheShopName);
                    intent.putExtra("shopImage", TheShopImage);

                    startActivity(intent);
                }

                @Override
                public void onFailure(String message) {

                }
            });
        });
    }
    private void fetchData() {
        FirebaseDatabaseManager.getShopOwnerData(shopID, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel data) {
                Picasso.get().load(data.getShopImage()).placeholder(R.drawable.sent_image_placeholder).into(shopImage);
                theShopName.setText(data.getShopName());
                shopRating.setText("⭐ " + data.getOverallRating());
                shopContactNum.setText(data.getContactNum());
                shopEmail.setText(data.getEmail());
                shopAbout.setText(data.getShopAbout());
                theProviderRole = data.getRole();

                if(data.getLocation().isEmpty() || data == null) {
                    shopLocationLayout.setVisibility(View.GONE);
                } else {
                    shopLocationLayout.setVisibility(View.VISIBLE);
                    locationTV.setText(data.getLocation());
                }

                if(data.getShopOpenAt().isEmpty() || data.getShopClosedAt().isEmpty() || data.getShopOpenAt() == null || data.getShopClosedAt() == null){
                    openingHoursLayout.setVisibility(View.GONE);
                } else {
                    openingHoursLayout.setVisibility(View.VISIBLE);
                    shopOpenAt.setText(data.getShopOpenAt() + " to " + data.getShopClosedAt());
                }


                shopLatitude = data.getLatitude();
                shopLongitude = data.getLongitude();


                if (shopLatitude == 0.0 || shopLongitude == 0.0) {
                    shopDistance.setText("Location unavailable");
                } else {
                    shopLatitude = data.getLatitude();
                    shopLongitude = data.getLongitude();
                    checkUserLocation();
                }


                TheShopUid = data.getUid();
                TheShopImage = data.getShopImage();
                TheShopName = data.getShopName();
                theShopcontactnum = data.getContactNum();

                // Enable actions after successful data loading
                callLayout.setEnabled(true);
                messageLink.setEnabled(true);
                shopRatings.setEnabled(true);
                displayTheBadge();

            }

            @Override
            public void onFailure(String failed) {

            }
        });
    }
    private void checkUserLocation() {

        // Check permission first
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            shopDistance.setText("Location access needed");

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );

            return;
        }

        // Permission exists, now check if device location is ON
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean locationEnabled =
                locationManager.isProviderEnabled(
                        LocationManager.GPS_PROVIDER
                )
                        || locationManager.isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER
                );

        if (!locationEnabled) {

            shopDistance.setText("Waiting for location...");
            userLocationAvailable = false;

            return;
        }

        // Location is enabled
        getUserCurrentLocation();
    }
    private void getUserCurrentLocation() {

        shopDistance.setText("Calculating distance...");

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {

            if (location != null) {

                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();

                userLocationAvailable = true;

                calculateShopDistance();

            } else {

                userLocationAvailable = false;

                shopDistance.setText("Location unavailable");
            }

        }).addOnFailureListener(e -> {

            userLocationAvailable = false;

            shopDistance.setText("Location unavailable");
        });
    }
    private void calculateShopDistance() {



        if (!userLocationAvailable) {
            return;
        }

        float[] distance = new float[1];

        Location.distanceBetween(
                userLatitude,
                userLongitude,
                shopLatitude,
                shopLongitude,
                distance
        );

        double distanceInMeters = distance[0];

        double distanceInKm = distanceInMeters / 1000.0;

        shopDistance.setText(
                String.format(
                        Locale.getDefault(),
                        "Approx. %.1f km away",
                        distanceInKm
                )
        );
    }
    private void showShopScheduleBottomSheet(String shopId) {

        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Loading shop schedule...")
                .setCancelable(false)
                .show();

        FirebaseShopScheduleHelper.getShopSchedule(shopId, new ShopScheduleCallback() {
            @Override
            public void onSuccess(ShopScheduleResult schedule) {
                loadingDialog.dismiss();

                com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                        new com.google.android.material.bottomsheet.BottomSheetDialog(CustomerShopLocationClicked.this);

                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_shop_schedule, null);
                bottomSheetDialog.setContentView(sheetView);

                LinearLayout dayChipsContainer = sheetView.findViewById(R.id.dayChipsContainer);
                LinearLayout closedDatesContainer = sheetView.findViewById(R.id.closedDatesContainer);
                TextView noClosedDatesText = sheetView.findViewById(R.id.noClosedDatesText);

                populateDayChips(dayChipsContainer, schedule.getOpeningDays());

                List<ShopClosedDateModel> closedDates = schedule.getClosedDates();
                if (closedDates == null || closedDates.isEmpty()) {
                    noClosedDatesText.setVisibility(View.VISIBLE);
                } else {
                    for (ShopClosedDateModel closedDate : closedDates) {
                        closedDatesContainer.addView(buildClosedDateCard(closedDate));
                    }
                }

                bottomSheetDialog.show();
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingDialog.dismiss();
                Toast.makeText(CustomerShopLocationClicked.this,
                        "Unable to load shop schedule: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateDayChips(LinearLayout container, ShopOpeningDaysModel days) {
        String[] labels = {"M", "T", "W", "T", "F", "S", "S"};
        boolean[] isOpen = {
                days.isMonday(), days.isTuesday(), days.isWednesday(),
                days.isThursday(), days.isFriday(), days.isSaturday(), days.isSunday()
        };

        int openColor = android.graphics.Color.parseColor("#2E7D32");   // muted green
        int openBg = android.graphics.Color.parseColor("#E8F5E9");
        int closedColor = android.graphics.Color.parseColor("#9E9E9E"); // muted gray
        int closedBg = android.graphics.Color.parseColor("#F2F2F2");

        for (int i = 0; i < labels.length; i++) {
            LinearLayout chip = new LinearLayout(this);
            chip.setOrientation(LinearLayout.VERTICAL);
            chip.setGravity(android.view.Gravity.CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            );
            int sideMargin = 4;
            params.setMargins(sideMargin, 0, sideMargin, 0);
            chip.setLayoutParams(params);
            chip.setPadding(0, 24, 0, 24);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(16f);
            bg.setColor(isOpen[i] ? openBg : closedBg);
            chip.setBackground(bg);

            TextView dayLabel = new TextView(this);
            dayLabel.setText(labels[i]);
            dayLabel.setTextSize(14f);
            dayLabel.setTextColor(isOpen[i] ? openColor : closedColor);
            dayLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            dayLabel.setGravity(android.view.Gravity.CENTER);

            TextView statusLabel = new TextView(this);
            statusLabel.setText(isOpen[i] ? "Open" : "Closed");
            statusLabel.setTextSize(9f);
            statusLabel.setTextColor(isOpen[i] ? openColor : closedColor);
            statusLabel.setGravity(android.view.Gravity.CENTER);
            statusLabel.setPadding(0, 4, 0, 0);

            chip.addView(dayLabel);
            chip.addView(statusLabel);
            container.addView(chip);
        }
    }

    private View buildClosedDateCard(ShopClosedDateModel closedDate) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.edittext_rounded);
        card.setPadding(28, 20, 28, 20);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 10);
        card.setLayoutParams(cardParams);

        String displayDate = closedDate.getDate();
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy (EEEE)", Locale.getDefault());
            displayDate = displayFormat.format(isoFormat.parse(closedDate.getDate()));
        } catch (Exception ignored) {
            // fall back to raw ISO string if parsing fails
        }

        TextView dateText = new TextView(this);
        dateText.setText(displayDate);
        dateText.setTextColor(getResources().getColor(R.color.red));
        dateText.setTextSize(13f);
        dateText.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView reasonText = new TextView(this);
        reasonText.setText(closedDate.getReason());
        reasonText.setTextColor(getResources().getColor(R.color.semiblack));
        reasonText.setTextSize(12, sp_as_float());
        reasonText.setPadding(0, 4, 0, 0);

        card.addView(dateText);
        card.addView(reasonText);
        return card;
    }

    private float sp_as_float() {
        return getResources().getDisplayMetrics().scaledDensity * 12f;
    }
    private void displayTheBadge() {
        if("freelancer".equals(theProviderRole)) {
            providerBadgeBG.setVisibility(View.VISIBLE);
            providerBadge.setText("FREELANCER");
            providerBadge.setTextColor(ContextCompat.getColor(this, R.color.orangedark));
            providerBadgeBG.setBackgroundResource(R.drawable.freelancer_badge_bg);
            providerBadgeIcon.setImageResource(R.drawable.freelancer_tag_icon);
        } else if ("shop_owner".equals(theProviderRole)) {
            providerBadgeBG.setVisibility(View.VISIBLE);
            providerBadge.setText("SHOP");
            providerBadge.setTextColor(ContextCompat.getColor(this, R.color.darkblue));
            providerBadgeBG.setBackgroundResource(R.drawable.provider_badge_bg);
            providerBadgeIcon.setImageResource(R.drawable.shop_tag_icon);
        }
    }
}