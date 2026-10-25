package com.example.ca_lalay.activities.shopowner;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.auth.ShopOwnerOrFreelancerRegistration;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseLocationsManager;
import com.example.ca_lalay.firebase.FirebaseShopOpeningDaysManager;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.callback.LocationCallback;
import com.example.ca_lalay.firebase.callback.ShopOpeningDaysCallback;
import com.example.ca_lalay.firebase.callback.UpdateUserCallback;
import com.example.ca_lalay.model.ShopOpeningDaysModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

import android.app.DatePickerDialog;
import com.example.ca_lalay.firebase.FirebaseShopClosedDatesManager;
import com.example.ca_lalay.firebase.callback.ShopClosedDatesListCallback;
import com.example.ca_lalay.model.ShopClosedDateModel;
import java.text.SimpleDateFormat;
import java.util.List;

public class ShopOwnerEditProfilePage extends AppCompatActivity {
    private EditText shopName, shopSlot, shopContactNum, shopLocation, shopOpenAt, shopClosedAt, shopDescription;
    private ShopOwnerModel currentShop;
    private ImageView selectImage, backBtn;
    private Uri imageUri;
    private TextView whatisShopSlot;

    private LinearLayout selectLocation, newLocationSelected;
    private double selectedLatitude;
    private double selectedLongitude;
    private String selectedAddress = "";
    private boolean isNewLocationSelected = false;

    private Button saveBtn;

    // NEW: operating days checkboxes
    private CheckBox dayMonday, dayTuesday, dayWednesday, dayThursday, dayFriday, daySaturday, daySunday;
    private LinearLayout selectClosedDateRow, closedDatesListContainer;
    private TextView closedDateText;
    private EditText closedDateReason;
    private Button addClosedDateBtn;
    private String selectedClosedDateISO = "";

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    selectImage.setImageURI(imageUri);
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
        setContentView(R.layout.activity_shop_owner_edit_profile_page);

        shopName = findViewById(R.id.shopName);
        shopContactNum = findViewById(R.id.shopContactNum);
        shopLocation = findViewById(R.id.shopLocation);
        shopOpenAt = findViewById(R.id.shopOpenAt);
        shopClosedAt = findViewById(R.id.shopClosedAt);
        shopDescription = findViewById(R.id.shopDescription);
        backBtn = findViewById(R.id.backBtn);
        shopSlot = findViewById(R.id.shopSlot);
        whatisShopSlot = findViewById(R.id.whatisShopSlot);

        selectLocation = findViewById(R.id.selectLocation);
        newLocationSelected = findViewById(R.id.newLocationSelected);

        saveBtn = findViewById(R.id.saveBtn);

        selectImage = findViewById(R.id.selectImage);

        // NEW: operating days checkboxes
        dayMonday = findViewById(R.id.dayMonday);
        dayTuesday = findViewById(R.id.dayTuesday);
        dayWednesday = findViewById(R.id.dayWednesday);
        dayThursday = findViewById(R.id.dayThursday);
        dayFriday = findViewById(R.id.dayFriday);
        daySaturday = findViewById(R.id.daySaturday);
        daySunday = findViewById(R.id.daySunday);

        selectClosedDateRow = findViewById(R.id.selectClosedDateRow);
        closedDatesListContainer = findViewById(R.id.closedDatesListContainer);
        closedDateText = findViewById(R.id.closedDateText);
        closedDateReason = findViewById(R.id.closedDateReason);
        addClosedDateBtn = findViewById(R.id.addClosedDateBtn);

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
        setupTimeField(shopOpenAt);
        setupTimeField(shopClosedAt);

        setupDetails();


        selectImage.setOnClickListener(d -> {
            choosePhotoFromGallery();
        });

        saveBtn.setOnClickListener(d -> {
            updateDetails();
        });

        backBtn.setOnClickListener(s -> {
            finish();
        });

        whatisShopSlot.setOnClickListener(s -> {

            new AlertDialog.Builder(this)
                    .setTitle("What is Shop Slot?")
                    .setMessage(
                            "Shop slot refers to the maximum number of vehicles " +
                                    "your shop can work on at the same time.\n\n" +
                                    "For example, if your shop can work on 5 cars at the same time, " +
                                    "set your shop slot to 5.\n\n" +
                                    "Once all 5 slots are occupied for a specific time, " +
                                    "other customers will need to choose another available time."
                    )
                    .setPositiveButton("I Understand", null)
                    .show();
        });

        shopOpenAt.setOnClickListener(v -> showTimePicker(shopOpenAt));
        shopClosedAt.setOnClickListener(v -> showTimePicker(shopClosedAt));

        selectClosedDateRow.setOnClickListener(v -> openClosedDatePicker());

        addClosedDateBtn.setOnClickListener(v -> submitClosedDate());

        selectLocation.setOnClickListener(d -> {
            Intent ent =
                    new Intent(
                            ShopOwnerEditProfilePage.this,
                            ShopOwnerSelectLocation.class
                    );

            locationPickerLauncher.launch(ent);
        });
    }

    private void openClosedDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // can't close today, same rule as customer booking

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, day);

                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                    selectedClosedDateISO = isoFormat.format(picked.getTime());
                    closedDateText.setText(displayFormat.format(picked.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        dialog.show();
    }

    private void submitClosedDate() {
        String reason = closedDateReason.getText().toString().trim();

        if (selectedClosedDateISO.isEmpty()) {
            Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (reason.isEmpty()) {
            closedDateReason.setError("Please enter a reason");
            closedDateReason.requestFocus();
            return;
        }

        String uid = currentShop.getUid();
        addClosedDateBtn.setEnabled(false);

        FirebaseShopClosedDatesManager.addClosedDate(uid, selectedClosedDateISO, reason, new UpdateUserCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(ShopOwnerEditProfilePage.this, "Closed date added", Toast.LENGTH_SHORT).show();
                closedDateText.setText("Tap to select a date");
                closedDateReason.setText("");
                selectedClosedDateISO = "";
                addClosedDateBtn.setEnabled(true);
                loadClosedDates();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ShopOwnerEditProfilePage.this, message, Toast.LENGTH_SHORT).show();
                addClosedDateBtn.setEnabled(true);
            }
        });
    }

    private void loadClosedDates() {
        String uid = currentShop.getUid();
        FirebaseShopClosedDatesManager.getAllClosedDates(uid, new ShopClosedDatesListCallback() {
            @Override
            public void onSuccess(List<ShopClosedDateModel> closedDates) {
                closedDatesListContainer.removeAllViews();
                for (ShopClosedDateModel model : closedDates) {
                    closedDatesListContainer.addView(buildClosedDateRow(model));
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Silent — not critical if this list fails to load
            }
        });
    }

    private View buildClosedDateRow(ShopClosedDateModel model) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(20, 20, 20, 20);
        row.setBackgroundResource(R.drawable.edittext_rounded);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 10);
        row.setLayoutParams(rowParams);

        TextView infoText = new TextView(this);
        String displayDate = model.getDate();
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            displayDate = displayFormat.format(isoFormat.parse(model.getDate()));
        } catch (Exception ignored) {
            // fall back to raw ISO string if parsing fails
        }
        infoText.setText(displayDate + "\n" + model.getReason());
        infoText.setTextColor(getResources().getColor(R.color.semiblack));
        infoText.setTextSize(13f);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        infoText.setLayoutParams(infoParams);

        TextView removeText = new TextView(this);
        removeText.setText("Remove");
        removeText.setTextColor(getResources().getColor(R.color.red));
        removeText.setTextSize(13f);
        removeText.setPaintFlags(removeText.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        removeText.setOnClickListener(v -> {
            FirebaseShopClosedDatesManager.removeClosedDate(currentShop.getUid(), model.getDate(), new UpdateUserCallback() {
                @Override
                public void onSuccess(String message) {
                    loadClosedDates();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(ShopOwnerEditProfilePage.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        row.addView(infoText);
        row.addView(removeText);
        return row;
    }

    private void setupDetails() {
        if (currentShop == null) {
            Toast.makeText(this, "Unable to load shop details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        shopName.setText(currentShop.getShopName());
        shopContactNum.setText(currentShop.getContactNum());
        shopLocation.setText(currentShop.getLocation());
        shopOpenAt.setText(currentShop.getShopOpenAt());
        shopClosedAt.setText(currentShop.getShopClosedAt());
        shopDescription.setText(currentShop.getShopAbout());
        shopSlot.setText(currentShop.getShopSlot());

        Picasso.get().load(currentShop.getShopImage()).placeholder(R.drawable.sent_image_placeholder).into(selectImage);

        // NEW: load existing operating days for this shop
        loadOpeningDays();
        loadClosedDates();
    }

    // NEW: fetches the shop's current opening-days config and reflects it in the checkboxes
    private void loadOpeningDays() {
        String uid = currentShop.getUid();
        FirebaseShopOpeningDaysManager.getOpeningDays(uid, new ShopOpeningDaysCallback() {
            @Override
            public void onSuccess(ShopOpeningDaysModel openingDays) {
                dayMonday.setChecked(openingDays.isMonday());
                dayTuesday.setChecked(openingDays.isTuesday());
                dayWednesday.setChecked(openingDays.isWednesday());
                dayThursday.setChecked(openingDays.isThursday());
                dayFriday.setChecked(openingDays.isFriday());
                daySaturday.setChecked(openingDays.isSaturday());
                daySunday.setChecked(openingDays.isSunday());
            }

            @Override
            public void onFailure(String errorMessage) {
                // Leave checkboxes at their default (all checked) if this fails to load.
            }
        });
    }

    private final androidx.activity.result.ActivityResultLauncher<Intent>
            locationPickerLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            Intent data = result.getData();

                            selectedLatitude =
                                    data.getDoubleExtra(
                                            "latitude",
                                            0
                                    );

                            selectedLongitude =
                                    data.getDoubleExtra(
                                            "longitude",
                                            0
                                    );

                            selectedAddress =
                                    data.getStringExtra(
                                            "address"
                                    );

                            if (selectedLatitude != 0
                                    && selectedLongitude != 0
                                    && selectedAddress != null
                                    && !selectedAddress.isEmpty()) {
                                isNewLocationSelected = true;
                                newLocationSelected.setVisibility(View.VISIBLE);
                                shopLocation.setText(selectedAddress);
                            } else {
                                newLocationSelected.setVisibility(View.GONE);
                                isNewLocationSelected = false;
                            }

                            Toast.makeText(
                                    this,
                                    "Location Selected",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Optional logs
                            android.util.Log.d(
                                    "RETREIVED LAT",
                                    selectedLatitude + ""
                            );

                            android.util.Log.d(
                                    "RETREIVED LON",
                                    selectedLongitude + ""
                            );

                            android.util.Log.d(
                                    "RETREIVED LOC",
                                    selectedAddress
                            );
                        }
                    });
    private void updateDetails() {
        // Get input values
        String shopNameValue = shopName.getText().toString().trim();
        String phoneNumber = shopContactNum.getText().toString().trim();
        String locationValue = shopLocation.getText().toString().trim();
        String openAtValue = shopOpenAt.getText().toString().trim();
        String closedAtValue = shopClosedAt.getText().toString().trim();
        String descriptionValue = shopDescription.getText().toString().trim();
        int theShopSlot = Integer.parseInt(shopSlot.getText().toString().trim());

        // =========================
        // VALIDATION
        // =========================

        // Shop name
        if (shopNameValue.isEmpty()) {
            shopName.setError("Business name is required");
            shopName.requestFocus();
            return;
        }

        // Contact number
        if (phoneNumber.isEmpty()) {
            shopContactNum.setError("Contact number is required");
            shopContactNum.requestFocus();
            return;
        }

        // Contact number must contain digits only
        if (!phoneNumber.matches("\\d+")) {
            shopContactNum.setError("Contact number must contain digits only");
            shopContactNum.requestFocus();
            return;
        }

        // Contact number must be exactly 11 digits
        if (phoneNumber.length() != 11) {
            shopContactNum.setError("Contact number must be exactly 11 digits");
            shopContactNum.requestFocus();
            return;
        }

        // Location
        if (locationValue.isEmpty()) {
            shopLocation.setError("Business location is required");
            shopLocation.requestFocus();
            return;
        }

        // Opening time
        if (openAtValue.isEmpty()) {
            shopOpenAt.setError("Opening time is required");
            shopOpenAt.requestFocus();
            return;
        }

        // Closing time
        if (closedAtValue.isEmpty()) {
            shopClosedAt.setError("Closing time is required");
            shopClosedAt.requestFocus();
            return;
        }

        if(theShopSlot == 0) {
            shopSlot.setError("Shop slot is required");
            shopSlot.requestFocus();
            return;
        }

        // Shop description
        if (descriptionValue.isEmpty()) {
            shopDescription.setError("Business description is required");
            shopDescription.requestFocus();
            return;
        }

        String shopSlotAvailable = String.valueOf(theShopSlot);

        String uid = FirebaseAuthManager.getCurrentUserUid();

        ShopOwnerModel details = new ShopOwnerModel();
        details.setUid(uid);
        details.setShopName(shopName.getText().toString().trim());
        details.setContactNum(shopContactNum.getText().toString().trim());
        details.setLocation(shopLocation.getText().toString().trim());
        details.setShopOpenAt(shopOpenAt.getText().toString().trim());
        details.setShopClosedAt(shopClosedAt.getText().toString().trim());
        details.setShopAbout(shopDescription.getText().toString().trim());
        details.setShopSlot(shopSlotAvailable);

        // NEW: build opening days model from checkbox states
        ShopOpeningDaysModel openingDays = new ShopOpeningDaysModel();
        openingDays.setMonday(dayMonday.isChecked());
        openingDays.setTuesday(dayTuesday.isChecked());
        openingDays.setWednesday(dayWednesday.isChecked());
        openingDays.setThursday(dayThursday.isChecked());
        openingDays.setFriday(dayFriday.isChecked());
        openingDays.setSaturday(daySaturday.isChecked());
        openingDays.setSunday(daySunday.isChecked());

        String oldImage = currentShop.getShopImage();

        setLoadingState();

        if (selectedLatitude != 0
                && selectedLongitude != 0
                && selectedAddress != null
                && !selectedAddress.isEmpty()) {

            details.setLatitude(selectedLatitude);
            details.setLongitude(selectedLongitude);
            details.setLocation(selectedAddress);
        } else {
            details.setLatitude(currentShop.getLatitude());
            details.setLongitude(currentShop.getLongitude());

        }

        if (imageUri != null) {

            FirebaseStorageManager.uploadImage(
                    "UserProfile",
                    imageUri,
                    new FirebaseStorageManager.ImageUploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            details.setShopImage(imageUrl);
                            saveDetailsToDB(details, openingDays);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(
                                    ShopOwnerEditProfilePage.this,
                                    errorMessage,
                                    Toast.LENGTH_SHORT
                            ).show();

                            setNormalState();
                        }
                    }
            );

        } else {

            details.setShopImage(oldImage);
            saveDetailsToDB(details, openingDays);
        }

    }
    private void saveDetailsToDB(ShopOwnerModel data, ShopOpeningDaysModel openingDays) {
        FirebaseDatabaseManager.updateShopOwnerData(data, new UpdateUserCallback() {
            @Override
            public void onSuccess(String message) {
                currentShop.setShopName(data.getShopName());
                currentShop.setContactNum(data.getContactNum());
                currentShop.setLocation(data.getLocation());
                currentShop.setShopOpenAt(data.getShopOpenAt());
                currentShop.setShopClosedAt(data.getShopClosedAt());
                currentShop.setShopAbout(data.getShopAbout());
                currentShop.setShopImage(data.getShopImage());
                currentShop.setShopSlot(data.getShopSlot());

                // NEW: save operating days right after the main profile update succeeds
                FirebaseShopOpeningDaysManager.updateOpeningDays(data.getUid(), openingDays, new UpdateUserCallback() {
                    @Override
                    public void onSuccess(String opDaysMessage) {
                        // Silent on success — main profile success toast below already confirms the save
                    }

                    @Override
                    public void onFailure(String opDaysError) {
                        Toast.makeText(
                                ShopOwnerEditProfilePage.this,
                                "Profile saved, but operating days failed to update: " + opDaysError,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

                if(isNewLocationSelected) {
                    FirebaseLocationsManager.updateLocation(data.getUid(), selectedLatitude, selectedLongitude, selectedAddress, new LocationCallback() {
                        @Override
                        public void onSuccess(String success) {
                            Toast.makeText(
                                    ShopOwnerEditProfilePage.this,
                                    "Profile and location updated successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            setNormalState();
                        }

                        @Override
                        public void onFailure(String failed) {
                            Toast.makeText(
                                    ShopOwnerEditProfilePage.this,
                                    message,
                                    Toast.LENGTH_SHORT
                            ).show();

                            setNormalState();
                        }
                    });
                } else {
                    Toast.makeText(
                            ShopOwnerEditProfilePage.this,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                    setNormalState();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ShopOwnerEditProfilePage.this, message, Toast.LENGTH_SHORT).show();
                setNormalState();
            }
        });
    }
    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
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
    private void showTimePicker(EditText targetField) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // If the field already has a value, start the picker from that time
        String existing = targetField.getText().toString().trim();
        if (!existing.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", Locale.getDefault());
                Calendar parsed = Calendar.getInstance();
                parsed.setTime(sdf.parse(existing));
                hour = parsed.get(Calendar.HOUR_OF_DAY);
                minute = parsed.get(Calendar.MINUTE);
            } catch (Exception ignored) {
                // fall back to current time if parsing fails
            }
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selected.set(Calendar.MINUTE, selectedMinute);

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", Locale.getDefault());
                    targetField.setText(sdf.format(selected.getTime()));
                },
                hour,
                minute,
                false // false = 12-hour format with AM/PM, true = 24-hour format
        );

        timePickerDialog.show();
    }
    private void setupTimeField(EditText field) {
        field.setFocusable(false);
        field.setFocusableInTouchMode(false);
        field.setClickable(true);
        field.setCursorVisible(false);
        field.setLongClickable(false); // prevents long-press text-selection popup
    }

}