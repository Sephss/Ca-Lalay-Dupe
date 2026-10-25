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
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseReportAccountHelper;
import com.example.ca_lalay.firebase.FirebaseReviewRatingsManager;
import com.example.ca_lalay.firebase.FirebaseShopScheduleHelper;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.callback.CustomerDataCallback;
import com.example.ca_lalay.firebase.callback.ReportAccountCallback;
import com.example.ca_lalay.firebase.callback.ShopScheduleCallback;
import com.example.ca_lalay.firebase.callback.ShopSummaryCallback;
import com.example.ca_lalay.model.ReportAccountModel;
import com.example.ca_lalay.model.ShopClosedDateModel;
import com.example.ca_lalay.model.ShopOpeningDaysModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.example.ca_lalay.model.ShopScheduleResult;
import com.example.ca_lalay.model.UserModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CustomerShopClicked extends AppCompatActivity {
    private Button viewShopServicesAll;
    private ImageView shopImage, backBtn, reportBtn;
    private TextView shopName, shopRating, shopOpenAt, shopContactNum, shopEmail, shopAbout;
    private String theShopid, theShopimage, theShopname, theShoprating, theShopopenat, theShopcontactnum, theShopemail, theShopabout;
    private LinearLayout messageLink, callLayout, shopViewLocation, shopRatings, openingHoursLayout;
    private  Button submitReportBtn;
    private Uri imageUri;
    private ImageView reportImagePreview;
    private TextView attachImageText;
    private TextView shopLocationTV;
    private String theShopLocation;
    private LinearLayout locationCardLayout;
    private LinearLayout shopOpeningDaysLayout;
    private TextView shopOpeningDays;

    private TextView providerBadge;
    private LinearLayout providerBadgeBG;
    private ImageView providerBadgeIcon;
    private String theProviderRole;


    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private boolean userLocationAvailable = false;
    private double userLatitude, userLongitude, shopLatitude, shopLongitude;
    private TextView shopDistance;

    private UserModel currentUser;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {

                            imageUri = result.getData().getData();

                            // Display selected image
                            reportImagePreview.setImageURI(imageUri);

                            // Show preview
                            reportImagePreview.setVisibility(View.VISIBLE);

                            // Change attachment text
                            attachImageText.setText("Change Image");
                        }
                    }
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_shop_clicked);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
        shopDistance = findViewById(R.id.shopDistance);


        shopImage = findViewById(R.id.shopImage);
        shopName = findViewById(R.id.shopName);
        shopRating = findViewById(R.id.shopRating);
        shopOpenAt = findViewById(R.id.shopOpenAt);
        shopContactNum = findViewById(R.id.shopContactNum);
        shopEmail = findViewById(R.id.shopEmail);
        shopAbout = findViewById(R.id.shopAbout);
        backBtn = findViewById(R.id.backBtn);
        viewShopServicesAll = findViewById(R.id.viewShopServicesAll);
        callLayout = findViewById(R.id.callLayout);
        shopViewLocation = findViewById(R.id.shopViewLocation);
        shopRatings = findViewById(R.id.shopRatings);
        openingHoursLayout = findViewById(R.id.openingHoursLayout);
        shopLocationTV = findViewById(R.id.shopLocationTV);
        locationCardLayout = findViewById(R.id.locationCardLayout);
        shopOpeningDaysLayout = findViewById(R.id.shopOpeningDaysLayout);
        shopOpeningDays = findViewById(R.id.shopOpeningDays);

        providerBadge = findViewById(R.id.providerBadge);
        providerBadgeBG = findViewById(R.id.providerBadgeBG);
        providerBadgeIcon = findViewById(R.id.providerBadgeIcon);

        reportBtn = findViewById(R.id.reportBtn);
        currentUser = UserSession.getInstance().getCurrentUser();

        Intent intent = getIntent();
        theShopimage = intent.getStringExtra("shopImage");
        theShopname = intent.getStringExtra("shopName");
        theShoprating = intent.getStringExtra("shopRating");
        theShopopenat = intent.getStringExtra("shopOpenAt");
        theShopcontactnum = intent.getStringExtra("shopContactNum");
        theShopemail = intent.getStringExtra("shopEmail");
        theShopabout = intent.getStringExtra("shopAbout");
        theShopid = intent.getStringExtra("shopId");
        shopLatitude = intent.getDoubleExtra("shopLat", 0.0);
        shopLongitude = intent.getDoubleExtra("shopLong", 0.0);
        theShopLocation = intent.getStringExtra("theShopLocation");
        theProviderRole = intent.getStringExtra("theProviderRole");


        if (shopLatitude == 0.0 || shopLongitude == 0.0) {
            shopDistance.setText("Location unavailable");
        } else {
            checkUserLocation();
        }


        messageLink = findViewById(R.id.messageLink);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupDetails();
        displayTheBadge();

        backBtn.setOnClickListener(s -> {
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
        });

        reportBtn.setOnClickListener(s -> {
            showReportDialog();
        });

        shopOpeningDays.setOnClickListener(d -> {
            showShopScheduleBottomSheet(theShopid);
        });

        shopOpeningDays.setPaintFlags(
                shopOpeningDays.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );


        shopRatings.setOnClickListener(d -> {
            Intent ds = new Intent(CustomerShopClicked.this, CustomerReviewRatings.class);
            ds.putExtra("shopUid", theShopid);
            ds.putExtra("shopImage", theShopimage);
            ds.putExtra("shopName", theShopname);
            startActivity(ds);
        });

        callLayout.setOnClickListener(d -> {
            String phoneNumber = theShopcontactnum;

            Intent call = new Intent (Intent.ACTION_DIAL);
            call.setData(Uri.parse(("tel:" + phoneNumber)));
            startActivity(call);
        });

        shopViewLocation.setOnClickListener(h -> {

            Intent pass = new Intent(
                    CustomerShopClicked.this,
                    CustomerLocateShop.class
            );

            pass.putExtra("shopUid", theShopid);

            startActivity(pass);

            overridePendingTransition(
                    R.anim.slide_up,
                    R.anim.stay
            );
        });

        messageLink.setOnClickListener(v -> {

            String uid = FirebaseAuthManager.getCurrentUserUid();

            FirebaseDatabaseManager.getCustomerData(uid, new CustomerDataCallback() {
                @Override
                public void onSuccess(String message,
                                      UserModel customerData) {

                    Intent intent = new Intent(CustomerShopClicked.this, CustomerMessageNow.class);

                    // Customer
                    intent.putExtra("customerId", uid);
                    intent.putExtra("customerName", customerData.getFirstname() + " " + customerData.getLastname());
                    intent.putExtra("customerImage", customerData.getImageUrl());

                    // Shop
                    intent.putExtra("shopId", theShopid);
                    intent.putExtra("shopName", theShopname);
                    intent.putExtra("shopImage", theShopimage);

                    startActivity(intent);
                }

                @Override
                public void onFailure(String message) {

                }
            });
        });

        viewShopServicesAll.setOnClickListener(s -> {
            Intent intent1 = new Intent(CustomerShopClicked.this, CustomerViewShopAllServices.class);
            intent1.putExtra("shopId", theShopid);
            startActivity(intent1);
            overridePendingTransition(
                    R.anim.slide_in_up,
                    R.anim.stay
            );
        });

    }
    @Override
    public void finish() {
        super.finish();
        // Slide down animation when closing
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
    }
    private void setupDetails() {
        Picasso.get().load(theShopimage).placeholder(R.drawable.sent_image_placeholder).into(shopImage);

        shopName.setText(theShopname);
        shopRating.setText(theShoprating);
        shopContactNum.setText(theShopcontactnum);
        shopEmail.setText(theShopemail);
        shopAbout.setText(theShopabout);

        if(theShopLocation.isEmpty() || theShopLocation == null) {
            locationCardLayout.setVisibility(View.GONE);
        } else {
            locationCardLayout.setVisibility(View.VISIBLE);
            shopLocationTV.setText(theShopLocation);
        }

        if(theShopopenat.isEmpty() || theShopopenat == null) {
            openingHoursLayout.setVisibility(View.GONE);
        } else {
            openingHoursLayout.setVisibility(View.VISIBLE);
            shopOpenAt.setText(theShopopenat);
        }

        FirebaseReviewRatingsManager.getShopSummary(theShopid, new ShopSummaryCallback() {
            @Override
            public void onSuccess(ShopOwnerModel shop) {
                shopRating.setText("⭐ " + shop.getOverallRating() );
            }

            @Override
            public void onFailure(String message) {

            }
        });
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

    private void showReportDialog() {

        View view = getLayoutInflater().inflate(
                R.layout.dialog_report_shop,
                null
        );

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(view)
                        .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }
        LinearLayout attachImageLayout =
                view.findViewById(R.id.attachImageLayout);

         attachImageText =
                view.findViewById(R.id.attachImageText);

         reportImagePreview =
                view.findViewById(R.id.reportImagePreview);

        EditText reportTitle =
                view.findViewById(R.id.reportTitle);

        EditText reportDescription =
                view.findViewById(R.id.reportDescription);

        ImageView closeReportBtn = view.findViewById(R.id.closeReportBtn);
        Button cancelReportBtn = view.findViewById(R.id.cancelReportBtn);
         submitReportBtn = view.findViewById(R.id.submitReportBtn);
        Spinner reportCategorySpinner = view.findViewById(R.id.reportCategorySpinner);

        String[] reportCategories = {
                "Select Category",
                "Inappropriate Behavior",
                "Fraud or Scam",
                "Incorrect Service Information",
                "Poor Service",
                "Safety Concern",
                "Shop Issue",
                "Other"
        };

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                reportCategories
        );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        reportCategorySpinner.setAdapter(categoryAdapter);

        closeReportBtn.setOnClickListener(v -> {

            clearReportForm();

            dialog.dismiss();
        });

        cancelReportBtn.setOnClickListener(v -> {

            clearReportForm();

            dialog.dismiss();
        });

        submitReportBtn.setOnClickListener(v -> {
            String category =
                    reportCategorySpinner.getSelectedItem().toString().trim();

            String title =
                    reportTitle.getText().toString().trim();

            String description =
                    reportDescription.getText().toString().trim();

            Uri evidenceImage =
                    imageUri;

            if (category.equals("Select Category")) {
                Toast.makeText(
                        CustomerShopClicked.this,
                        "Please select a report category.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (title.isEmpty()) {
                reportTitle.setError("Title is required");
                reportTitle.requestFocus();
                return;
            }

            if (description.isEmpty()) {
                reportDescription.setError("Description is required");
                reportDescription.requestFocus();
                return;
            }

            submitReport(title, description, category, "pending", dialog);

        });

        attachImageLayout.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.85)
            );
        }
    }
    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }
    private void submitReport(String title, String description, String category, String status, androidx.appcompat.app.AlertDialog reportDialog) {
        setLoadingState();
        if(imageUri == null) {
            saveTheReport(title, description, category, "none", status, reportDialog);
        } else {
            FirebaseStorageManager.uploadImage("ReportedAccountImages", imageUri, new FirebaseStorageManager.ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    saveTheReport(title, description, category, imageUrl, status, reportDialog);
                }

                @Override
                public void onFailure(String errorMessage) {
                    setNormalState();
                }
            });
        }
    }
    private void saveTheReport(String title, String description, String category, String image, String status, androidx.appcompat.app.AlertDialog reportDialog) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        ReportAccountModel data = new ReportAccountModel("",currentUser.getUid(), currentUser.getFirstname() + " " + currentUser.getLastname(), currentUser.getImageUrl(), theShopid, theShopname, theShopimage, title, description, category, image, timestamp, status);

        setLoadingState();
        FirebaseReportAccountHelper.reportAccount(data, new ReportAccountCallback() {
            @Override
            public void onSuccess(String message) {
           setNormalState();
                // Close report form
                reportDialog.dismiss();

                // Clear form
                clearReportForm();

                // Show confirmation
                showReportSuccessDialog();

            }

            @Override
            public void onFailure(String message) {
                setNormalState();
                Toast.makeText(
                        CustomerShopClicked.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
    private void clearReportForm() {

        imageUri = null;

        if (reportImagePreview != null) {
            reportImagePreview.setImageDrawable(null);
            reportImagePreview.setVisibility(View.GONE);
        }

        if (attachImageText != null) {
            attachImageText.setText("Attach Image");
        }
    }
    private void setLoadingState() {
        submitReportBtn.setEnabled(false);
        submitReportBtn.setAlpha(0.5f);
        submitReportBtn.setText("SUBMITTING...");
    }

    private void setNormalState() {
        submitReportBtn.setEnabled(true);
        submitReportBtn.setAlpha(1f);
        submitReportBtn.setText("SUBMIT REPORT");
    }
    private void showReportSuccessDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Report Submitted")
                .setMessage(
                        "Thank you for submitting your report. " +
                                "Your report has been received and will be reviewed by our administrators."
                )
                .setPositiveButton("OK", null)
                .show();
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
                        new com.google.android.material.bottomsheet.BottomSheetDialog(CustomerShopClicked.this);

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
                Toast.makeText(CustomerShopClicked.this,
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
}