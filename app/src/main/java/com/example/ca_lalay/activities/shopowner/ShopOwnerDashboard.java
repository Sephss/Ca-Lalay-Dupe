package com.example.ca_lalay.activities.shopowner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.SessionManager;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.activities.customer.CustomerDashboard;
import com.example.ca_lalay.activities.customer.CustomerMessages;
import com.example.ca_lalay.activities.customer.CustomerProfile;
import com.example.ca_lalay.activities.customer.CustomerViewMaps;
import com.example.ca_lalay.auth.LoginActivity;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseReportsManager;
import com.example.ca_lalay.firebase.callback.OnReportCallback;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.model.BookingReportModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

public class ShopOwnerDashboard extends AppCompatActivity {
    private LinearLayout browseServicesLayout, nearbyMaps, myBookings, reportsBtn,actionRequiredLayout, myRatings;
    private TextView shopName, noBookingsCompletedTodayNote, totalBookingsToday, shopOpenStatus, shopOpeningHours, totalCancelledToday, totalCompletedToday, totalIncomeToday, shopRatings;
    private ImageView shopImage, goToMessages, goToProfile;
    private String theShopName;
    private Button completeNowBtn;
    private SessionManager sessionManager;

    @Override
    protected void onResume() {
        super.onResume();
        refreshActionRequiredVisibility();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_dashboard);

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

        totalBookingsToday = findViewById(R.id.totalBookingsToday);
        totalCancelledToday = findViewById(R.id.totalCancelledToday);
        totalCompletedToday = findViewById(R.id.totalCompletedToday);
        totalIncomeToday = findViewById(R.id.totalIncomeToday);
        actionRequiredLayout = findViewById(R.id.actionRequiredLayout);
        myRatings = findViewById(R.id.myRatings);

        completeNowBtn = findViewById(R.id.completeNowBtn);

        shopRatings = findViewById(R.id.shopRatings);
        shopOpeningHours = findViewById(R.id.shopOpeningHours);
        shopOpenStatus = findViewById(R.id.shopStatus);
        noBookingsCompletedTodayNote = findViewById(R.id.noBookingsCompletedTodayNote);

        browseServicesLayout = findViewById(R.id.browseServicesLayout);
        shopName = findViewById(R.id.shopName);
        shopImage = findViewById(R.id.shopImage);
        nearbyMaps = findViewById(R.id.nearbyMaps);
        reportsBtn = findViewById(R.id.reportsBtn);
        myBookings = findViewById(R.id.myBookings);
        goToMessages = findViewById(R.id.goToMessages);
        goToProfile = findViewById(R.id.goToProfile);
        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        browseServicesLayout.setAlpha(0.5f);
        browseServicesLayout.setEnabled(false);
        setupTodaysReport();
        browseServicesLayout.setOnClickListener(s -> {
            Intent intent = new Intent(ShopOwnerDashboard.this, ShopOwnerServices.class);
            intent.putExtra("shopName", theShopName);
            startActivity(intent);
        });

        completeNowBtn.setOnClickListener(s -> {
            navigateTo(ShopOwnerEditProfilePage.class);
        });

        myBookings.setOnClickListener(s -> {
navigateTo(ShopOwnerViewBookings.class);
        });
        nearbyMaps.setOnClickListener(d -> {
            navigateTo(ShopOwnerViewMaps.class);
        });
        setUpShopOwnerDetails();

        goToMessages.setOnClickListener(g -> {
            navigateTo(ShopOwnerMessages.class);
            overridePendingTransition(
                    R.anim.fast_fade_in,
                    R.anim.fast_fade_out
            );
        });

        goToProfile.setOnClickListener(d -> {
            navigateTo(ShopOwnerGoToProfile.class);
            overridePendingTransition(
                    R.anim.fast_fade_in,
                    R.anim.fast_fade_out
            );
        });

        reportsBtn.setOnClickListener(s -> {
            navigateTo(ShopOwnerReportPage.class);
        });

        myRatings.setOnClickListener(s -> {
            navigateTo(ShopOwnerViewShopRatings.class);
        });
    }
    private void refreshActionRequiredVisibility() {
        setupTodaysReport();

        ShopOwnerModel data =
                ShopOwnerSession.getInstance().getCurrentShopOwner();

        if (data == null) return;

        // =========================
        // SHOP BASIC DETAILS
        // =========================
        String shopImageUrl = data.getShopImage();
        String shopNameValue = data.getShopName();

        if (shopImageUrl != null && !shopImageUrl.trim().isEmpty()) {
            Picasso.get()
                    .load(shopImageUrl)
                    .into(shopImage);
        }

        if (shopNameValue != null) {
            shopName.setText(shopNameValue);
        }

        // =========================
        // SHOP HOURS
        // =========================
        String openAt = data.getShopOpenAt();       // e.g. "08:00 am"
        String closedAt = data.getShopClosedAt();   // e.g. "06:00 pm"

        if (openAt == null || openAt.trim().isEmpty()
                || closedAt == null || closedAt.trim().isEmpty()) {

            shopOpeningHours.setText("Hours not set");
            shopOpenStatus.setText("Closed");

        } else {

            try {

                /*
                 * Firebase may contain:
                 *
                 * 08:00 am
                 * 06:00 pm
                 *
                 * Normalize to:
                 *
                 * 08:00 AM
                 * 06:00 PM
                 */
                String normalizedOpenAt =
                        openAt.trim().toUpperCase(Locale.ENGLISH);

                String normalizedClosedAt =
                        closedAt.trim().toUpperCase(Locale.ENGLISH);

                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern(
                                "hh:mm a",
                                Locale.ENGLISH
                        );

                LocalTime openingTime =
                        LocalTime.parse(normalizedOpenAt, formatter);

                LocalTime closingTime =
                        LocalTime.parse(normalizedClosedAt, formatter);

                LocalTime currentTime = LocalTime.now();

                // Display hours
                shopOpeningHours.setText(
                        "Open " + openAt + " to " + closedAt
                );

                // Check if currently open
                if (!currentTime.isBefore(openingTime)
                        && !currentTime.isAfter(closingTime)) {

                    shopOpenStatus.setText("Open");

                } else {

                    shopOpenStatus.setText("Closed");
                }

            } catch (DateTimeParseException e) {

                // Prevent invalid Firebase data from crashing the app
                shopOpeningHours.setText("Hours not set");
                shopOpenStatus.setText("Closed");

                Log.e(
                        "SHOP_HOURS",
                        "Invalid shop hours: "
                                + openAt + " - " + closedAt,
                        e
                );
            }
        }

            int theShopSlot = Integer.parseInt(data.getShopSlot());

            // =========================
            // CHECK REQUIRED INFORMATION
            // =========================
            boolean missingInfo =
                    data.getShopAbout() == null
                            || data.getShopAbout().trim().isEmpty()

                            || data.getLocation() == null
                            || data.getLocation().trim().isEmpty()

                            || data.getShopOpenAt() == null
                            || data.getShopOpenAt().trim().isEmpty()

                            || data.getShopClosedAt() == null
                            || data.getShopClosedAt().trim().isEmpty()
                            || theShopSlot == 0;



        if (missingInfo) {

            actionRequiredLayout.setVisibility(View.VISIBLE);

            browseServicesLayout.setEnabled(false);
            browseServicesLayout.setAlpha(0.5f);

        } else {

            actionRequiredLayout.setVisibility(View.GONE);

            browseServicesLayout.setEnabled(true);
            browseServicesLayout.setAlpha(1.0f);
        }
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
    }
    private void setUpShopOwnerDetails() {
        String uid = FirebaseAuthManager.getCurrentUserUid();

        FirebaseDatabaseManager.getShopOwnerData(uid, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel data) {
                ShopOwnerSession.getInstance().setCurrentShopOwner(data);

                if(data.getStatus().equals("suspended")) {
                    FirebaseAuthManager.logout();
                    UserSession.getInstance().clearSession();
                    ShopOwnerSession.getInstance().clearSession();
                    sessionManager.logout();
                    Intent intent = new Intent(ShopOwnerDashboard.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                if (data.getShopAbout() == null || data.getShopAbout().trim().isEmpty()
                        || data.getLocation() == null || data.getLocation().trim().isEmpty()
                        || data.getShopOpenAt() == null || data.getShopOpenAt().trim().isEmpty()) {

                    actionRequiredLayout.setVisibility(View.VISIBLE);
                    browseServicesLayout.setEnabled(false);
                    browseServicesLayout.setAlpha(0.5f);
                } else {
                    actionRequiredLayout.setVisibility(View.GONE);
                    browseServicesLayout.setEnabled(true);
                    browseServicesLayout.setAlpha(1.0f);
                }

                String openAt = data.getShopOpenAt();       // e.g. "08:00 am"
                String closedAt = data.getShopClosedAt();   // e.g. "06:00 pm"

                if (openAt == null || openAt.trim().isEmpty()
                        || closedAt == null || closedAt.trim().isEmpty()) {

                    shopOpeningHours.setText("Hours not set");
                    shopOpenStatus.setText("Closed");

                } else {

                    try {

                        DateTimeFormatter formatter =
                                DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

                        // Convert "08:00 am" -> "08:00 AM"
                        String normalizedOpenAt =
                                openAt.trim().toUpperCase(Locale.ENGLISH);

                        String normalizedClosedAt =
                                closedAt.trim().toUpperCase(Locale.ENGLISH);

                        LocalTime openingTime =
                                LocalTime.parse(normalizedOpenAt, formatter);

                        LocalTime closingTime =
                                LocalTime.parse(normalizedClosedAt, formatter);

                        LocalTime currentTime = LocalTime.now();

                        shopOpeningHours.setText(
                                "Open " + openAt + " to " + closedAt
                        );

                        if (!currentTime.isBefore(openingTime)
                                && !currentTime.isAfter(closingTime)) {

                            shopOpenStatus.setText("Open");

                        } else {

                            shopOpenStatus.setText("Closed");
                        }

                    } catch (DateTimeParseException e) {

                        shopOpeningHours.setText("Hours not set");
                        shopOpenStatus.setText("Closed");

                        Log.e(
                                "SHOP_HOURS",
                                "Invalid time: " + openAt + " - " + closedAt,
                                e
                        );
                    }
                }


                ShopOwnerDashboard.this.shopName.setText(data.getShopName());
                shopRatings.setText(data.getOverallRating());
                Picasso.get().load(data.getShopImage()).placeholder(R.drawable.sent_image_placeholder).into(ShopOwnerDashboard.this.shopImage);
                theShopName = data.getShopName();
            }

            @Override
            public void onFailure(String failed) {
                Toast.makeText(ShopOwnerDashboard.this, failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupTodaysReport() {
        String uid = FirebaseAuthManager.getCurrentUserUid();
        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        FirebaseReportsManager.getDailyReport(uid, today, new OnReportCallback() {
            @Override
            public void onSuccess(BookingReportModel report) {
                String cancelledBookings = String.valueOf(report.getCancelledBookings());
                String completedBookings = String.valueOf(report.getCompletedBookings());
                String totalBookings = String.valueOf(report.getTotalBookings());
                String totalIncome = String.valueOf(report.getTotalIncome());

                totalBookingsToday.setText(totalBookings);
                totalCancelledToday.setText(cancelledBookings);
                totalCompletedToday.setText(completedBookings);
                totalIncomeToday.setText("₱" + totalIncome);

                if (report.getCompletedBookings() == 0) {
                    noBookingsCompletedTodayNote.setVisibility(View.VISIBLE);
                } else {
                    noBookingsCompletedTodayNote.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String message) {

            }
        });
    }
}