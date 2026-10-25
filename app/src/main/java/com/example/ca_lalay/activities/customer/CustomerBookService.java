package com.example.ca_lalay.activities.customer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.CustomerVehicleSelectAdapter;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseCapacityManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseOnlinePaymentDetailsManager;
import com.example.ca_lalay.firebase.FirebaseShopOpeningDaysManager;
import com.example.ca_lalay.firebase.FirebaseVehicleManager;
import com.example.ca_lalay.firebase.callback.AvailableTimesCallback;
import com.example.ca_lalay.firebase.callback.CustomerBookingCallback;
import com.example.ca_lalay.firebase.callback.CustomerDataCallback;
import com.example.ca_lalay.firebase.callback.CustomerVehicleListCallback;
import com.example.ca_lalay.firebase.callback.ShopOpeningDaysCallback;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.model.BookingCreationParams;
import com.example.ca_lalay.model.CustomerVehicleModel;
import com.example.ca_lalay.model.OnlinePaymentDetailsModel;
import com.example.ca_lalay.model.ShopOpeningDaysModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.example.ca_lalay.model.TimeSlotOption;
import com.example.ca_lalay.model.UserModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.ca_lalay.firebase.FirebaseShopClosedDatesManager;
import com.example.ca_lalay.firebase.callback.ShopClosedDateCallback;
import com.example.ca_lalay.model.ShopClosedDateModel;

public class CustomerBookService extends AppCompatActivity {
    private String theShopLocation, shopImage, serviceId, serviceProviderId, serviceName, servicePrice, shopOwnerName, shopRating, serviceImage,serviceType, serviceDuration;
    private ImageView theServiceImage;
    private TextView dateTimeError, theVehicleDetails;
    private TextView theServiceName, theServicePrice, theShopOwnerName, theServiceType, selectedDate, selectedTime, billingServicePrice, billingTotalAmount, theEstimateDuration;
    private CardView selectDate, selectTime;
    private Button confirmBookingBtn;
    private EditText additionaalNotes;
    private String theTime = "";
    private String theDate = "";
    private String theDateISO = "";
    private String additionalNotes = "";
    private LinearLayout enterVehicle;
    private String enteredVehicle ="";
    private String theVehicleSize = "";
    private String mediumPercentage, largePercentage;

    private LinearLayout paymentMethodLayout;
    private TextView selectedPaymentMethod;
    private String theSelectedPaymentMethod = "Select payment method";
    private String theBookingStatus = "pending";

    private TextView viewShopOnlinePaymentsDetailsLink;

    // Selected slot's computed start/end, in epoch millis (0 until a valid time is chosen)
    private long theStartMillis = 0L;
    private long theEndMillis = 0L;

    // old version
    private String shopOpenAt, shopCloseAt;
    // new version get directly from shop
    private String shopOpening, shopClosing;
    private String shopCapacity;
    private String theShopID;

    // NEW: cached opening-days config for this shop, fetched once in onCreate
    private ShopOpeningDaysModel theOpeningDays;

    // upload receipt views
    private LinearLayout uploadReceiptLayout, receiptActions;
    private View receiptDivider;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView viewReceipt, reUploadReceipt, receiptUploadedTV;

    private ImageView backBtn;

    private TextView thePercentageMedium, thePercentageLarge, mediumAdditional, largeAdditional;
    private String theTotal, theAddition;
    private String overallAddition = "0";

    private LinearLayout mediumFeeLayout, largeFeeLayout;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_book_service);

        theServiceImage = findViewById(R.id.theServiceImage);
        theServiceName = findViewById(R.id.theServiceName);
        theServicePrice = findViewById(R.id.theServicePrice);
        theShopOwnerName = findViewById(R.id.theShopOwnerName);
        theServiceType = findViewById(R.id.theServiceType);
        selectDate = findViewById(R.id.selectDate);
        selectTime = findViewById(R.id.selectTime);
        selectedDate = findViewById(R.id.selectedDate);
        selectedTime = findViewById(R.id.selectedTime);
        billingServicePrice = findViewById(R.id.billingServicePrice);
        billingTotalAmount = findViewById(R.id.billingTotalAmount);
        theEstimateDuration = findViewById(R.id.theEstimateDuration);
        confirmBookingBtn = findViewById(R.id.confirmBookingBtn);
        additionaalNotes = findViewById(R.id.additionaalNotes);
        dateTimeError = findViewById(R.id.dateTimeError);
        enterVehicle = findViewById(R.id.enterVehicle);
        theVehicleDetails = findViewById(R.id.theVehicleDetails);
        mediumFeeLayout = findViewById(R.id.mediumFeeLayout);
        largeFeeLayout = findViewById(R.id.largeFeeLayout);
        thePercentageMedium = findViewById(R.id.thePercentageMedium);
        thePercentageLarge = findViewById(R.id.thePercentageLarge);
        viewShopOnlinePaymentsDetailsLink = findViewById(R.id.viewShopOnlinePaymentsDetailsLink);

        // upload receipt initialization
        uploadReceiptLayout = findViewById(R.id.uploadReceiptLayout);
        receiptDivider = findViewById(R.id.receiptDivider);
        receiptActions = findViewById(R.id.receiptActions);
        viewReceipt = findViewById(R.id.viewReceipt);
        reUploadReceipt = findViewById(R.id.reUploadReceipt);
        receiptUploadedTV = findViewById(R.id.receiptUploadedTV);

        paymentMethodLayout = findViewById(R.id.paymentMethodLayout);
        selectedPaymentMethod = findViewById(R.id.selectedPaymentMethod);

        mediumAdditional = findViewById(R.id.mediumAdditional);
        largeAdditional = findViewById(R.id.largeAdditional);

        backBtn = findViewById(R.id.backBtn);

        Intent intent = getIntent();
        serviceId = intent.getStringExtra("serviceId");
        serviceProviderId = intent.getStringExtra("serviceProviderId");
        serviceName = intent.getStringExtra("serviceName");
        servicePrice = intent.getStringExtra("servicePrice");
        shopOwnerName = intent.getStringExtra("shopName");
        shopRating = intent.getStringExtra("shopRating");
        serviceImage = intent.getStringExtra("serviceImage");
        serviceType = intent.getStringExtra("serviceCategory");
        serviceDuration = intent.getStringExtra("serviceDuration");
        shopImage = intent.getStringExtra("shopImage");
        theShopLocation = intent.getStringExtra("shopLocation");
        mediumPercentage = intent.getStringExtra("mediumPercentage");
        largePercentage = intent.getStringExtra("largePercentage");

        theShopID = intent.getStringExtra("theShopID");

        shopOpenAt = intent.getStringExtra("shopOpenAt");
        shopCloseAt = intent.getStringExtra("shopCloseAt");

        // In onCreate(), right after selectDate = findViewById(R.id.selectDate); add:
        selectDate.setEnabled(false);
        selectDate.setAlpha(0.5f); // visually shows it's not tappable yet

        getShopOpeningHours();
        getShopOpeningDaysData(); // NEW
        setUpDetails();

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

        selectDate.setOnClickListener(v -> openDatePicker());
        selectTime.setOnClickListener(v -> openTimePicker());

        paymentMethodLayout.setOnClickListener(s -> {
            displayPaymentOptions();
        });

        confirmBookingBtn.setOnClickListener(s -> {
            bookTheService();
        });

        backBtn.setOnClickListener(s -> {
            finish();
        });

        viewShopOnlinePaymentsDetailsLink.setOnClickListener(s -> {
            showOnlinePaymentDetailsDialog();
        });

        uploadReceiptLayout.setOnClickListener(d -> {
            choosePhotoFromGallery();
        });

        reUploadReceipt.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });

        reUploadReceipt.setPaintFlags(
                reUploadReceipt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );

        viewReceipt.setPaintFlags(
                viewReceipt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );

        viewReceipt.setOnClickListener(v -> {
            if (imageUri != null) {
                showReceiptPreview(imageUri);
            } else {
                Toast.makeText(this, "No receipt uploaded.", Toast.LENGTH_SHORT).show();
            }
        });



        enterVehicle.setOnClickListener(v -> {
            String uid = FirebaseAuthManager.getCurrentUserUid();

            FirebaseVehicleManager.getUserVehicles(uid, new CustomerVehicleListCallback() {
                @Override
                public void onSuccess(List<CustomerVehicleModel> vehicles) {

                    if (vehicles.isEmpty()) {
                        new AlertDialog.Builder(CustomerBookService.this)
                                .setTitle("No Vehicles Found")
                                .setMessage("You haven't registered any vehicles yet. Please add one in 'My Vehicles' first.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    showVehicleSelectBottomSheet(vehicles);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(CustomerBookService.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // NEW: fetches the shop's opening-days config once and caches it for the date picker to use
    private void getShopOpeningDaysData() {
        FirebaseShopOpeningDaysManager.getOpeningDays(serviceProviderId, new ShopOpeningDaysCallback() {
            @Override
            public void onSuccess(ShopOpeningDaysModel openingDays) {
                theOpeningDays = openingDays;
                selectDate.setEnabled(true);
                selectDate.setAlpha(1f);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Fail open: leave theOpeningDays null so the date picker doesn't block
                // bookings just because this fetch failed.
            }
        });
    }

    private void showReceiptPreview(Uri uri) {

        ImageView receiptImage = new ImageView(this);

        receiptImage.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        receiptImage.setAdjustViewBounds(true);
        receiptImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        try {
            receiptImage.setImageURI(uri);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to display receipt.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Receipt")
                .setView(receiptImage)
                .setPositiveButton("Close", null)
                .create();

        dialog.show();
    }
    private void displayPaymentOptions() {

        String[] paymentMethods = {
                "Cash – Pay at the Shop",
                "Online – Pay Directly to the Shop"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Payment Method");

        builder.setItems(paymentMethods, (dialog, which) -> {

            if (which == 0) {

                theSelectedPaymentMethod = "Cash";
                selectedPaymentMethod.setText("Cash – Pay at the Shop");



            } else if (which == 1) {

                theSelectedPaymentMethod = "Online";
                selectedPaymentMethod.setText("Online – Pay Directly to the Shop");


            }

            dialog.dismiss();
        });

        builder.show();
    }
    private void showOnlinePaymentDetailsDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Online Payment Details")
                .setMessage("Loading payment details...")
                .setNegativeButton("Close", null)
                .create();

        dialog.show();

        FirebaseOnlinePaymentDetailsManager.getOnlinePaymentDetails(
                serviceProviderId,
                new FirebaseOnlinePaymentDetailsManager.OnGetOnlinePaymentDetailsListener() {

                    @Override
                    public void onSuccess(OnlinePaymentDetailsModel model) {

                        if (model != null &&
                                model.getDetails() != null &&
                                !model.getDetails().trim().isEmpty()) {

                            dialog.setMessage(model.getDetails());

                        } else {

                            dialog.setMessage(
                                    "This shop has not provided online payment details."
                            );
                        }
                    }

                    @Override
                    public void onNotFound() {

                        dialog.setMessage(
                                "This shop has not provided online payment details."
                        );
                    }

                    @Override
                    public void onFailure(String error) {

                        dialog.setMessage(
                                "Unable to load online payment details. Please try again."
                        );
                    }
                }
        );
    }

    private void getShopOpeningHours() {
        FirebaseDatabaseManager.getShopOwnerData(serviceProviderId, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel data) {
                shopOpening = data.getShopOpenAt();
                shopClosing = data.getShopClosedAt();
                shopCapacity = "5";
            }

            @Override
            public void onFailure(String failed) {

            }
        });
    }
    private void showVehicleSelectBottomSheet(List<CustomerVehicleModel> vehicles) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_vehicle_select, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView recyclerView = sheetView.findViewById(R.id.vehicleSelectRecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CustomerVehicleSelectAdapter selectAdapter = new CustomerVehicleSelectAdapter(vehicles, vehicle -> {
            enteredVehicle = vehicle.getVehicleName() + " - " + vehicle.getVehiclePlateNumber();
            theVehicleSize = vehicle.getVehicleSize();

            switch (vehicle.getVehicleSize()) {
                case "Medium":
                    double basePrice = Double.parseDouble(servicePrice);
                    double theMediumPercentage = Double.parseDouble(mediumPercentage);

                    double additional = basePrice * theMediumPercentage / 100;

                    overallAddition = String.valueOf(additional);

                    double totalPrice = basePrice + additional;
                    billingTotalAmount.setText(String.valueOf(totalPrice));

                    mediumFeeLayout.setVisibility(View.VISIBLE);
                    thePercentageMedium.setText("Medium vehicle (+" + mediumPercentage + "%)");
                    mediumAdditional.setText(String.valueOf(additional));
                    largeFeeLayout.setVisibility(View.GONE);
                    break;
                case "Large":
                    double basePrice2 = Double.parseDouble(servicePrice);
                    double theLargePercentage = Double.parseDouble(largePercentage);
                    double additional2 = basePrice2 * theLargePercentage / 100;

                    overallAddition = String.valueOf(additional2);

                    double totalPrice2 = basePrice2 + additional2;
                    billingTotalAmount.setText(String.valueOf(totalPrice2));

                    mediumFeeLayout.setVisibility(View.GONE);
                    largeFeeLayout.setVisibility(View.VISIBLE);
                    thePercentageLarge.setText("Large vehicle (+" + largePercentage + "%)");
                    largeAdditional.setText(String.valueOf(additional2));
                    break;
                default:
                    mediumFeeLayout.setVisibility(View.GONE);
                    largeFeeLayout.setVisibility(View.GONE);
            }
            theVehicleDetails.setText(enteredVehicle);
            bottomSheetDialog.dismiss();
        });

        recyclerView.setAdapter(selectAdapter);

        bottomSheetDialog.show();
    }
    private void setUpDetails() {
        theServiceName.setText(serviceName);
        theServicePrice.setText("₱ " + servicePrice);
        theShopOwnerName.setText(shopOwnerName);
        theServiceType.setText(serviceType);
        theEstimateDuration.setText(serviceDuration);
        billingServicePrice.setText(servicePrice);
        billingTotalAmount.setText(servicePrice);
        Picasso.get().load(serviceImage).into(theServiceImage);
    }
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(selectedYear, selectedMonth, selectedDay);

                    // Weekly recurring closed-day check (unchanged from before)
                    if (theOpeningDays != null
                            && !theOpeningDays.isOpenOn(selectedCal.get(Calendar.DAY_OF_WEEK))) {

                        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                        String dayName = dayNameFormat.format(selectedCal.getTime());

                        new AlertDialog.Builder(CustomerBookService.this)
                                .setTitle("Shop Closed")
                                .setMessage("This shop is closed on " + dayName + ". Please choose another date.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    SimpleDateFormat databaseFormat =
                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String candidateDateISO = databaseFormat.format(selectedCal.getTime());

                    // NEW: check for a specific one-off closed date (renovation, holiday, etc.)
                    AlertDialog checkingDialog = new AlertDialog.Builder(this)
                            .setMessage("Checking availability...")
                            .setCancelable(false)
                            .show();

                    FirebaseShopClosedDatesManager.getClosedDate(serviceProviderId, candidateDateISO, new ShopClosedDateCallback() {
                        @Override
                        public void onFound(ShopClosedDateModel closedDate) {
                            checkingDialog.dismiss();

                            new AlertDialog.Builder(CustomerBookService.this)
                                    .setTitle("Shop Closed")
                                    .setMessage("This shop will be closed on this date.\n\nReason: " + closedDate.getReason())
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                        @Override
                        public void onNotFound() {
                            checkingDialog.dismiss();
                            applySelectedDate(selectedCal, candidateDateISO);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            checkingDialog.dismiss();
                            // Fail open: if this lookup fails, don't block the booking flow
                            applySelectedDate(selectedCal, candidateDateISO);
                        }
                    });
                },
                year, month, day
        );

        // Cannot select today — minimum date is tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        // limit to current year only
        Calendar endOfYear = Calendar.getInstance();
        endOfYear.set(Calendar.MONTH, Calendar.DECEMBER);
        endOfYear.set(Calendar.DAY_OF_MONTH, 31);

        datePickerDialog.getDatePicker().setMaxDate(endOfYear.getTimeInMillis());

        datePickerDialog.show();
    }

    // NEW: extracted so both the "not found" and "lookup failed" paths can finish the date selection
    private void applySelectedDate(Calendar selectedCal, String dateISO) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        theDate = displayFormat.format(selectedCal.getTime());
        theDateISO = dateISO;
        selectedDate.setText(theDate);

        // Changing the date invalidates any previously selected time.
        theTime = "";
        theStartMillis = 0L;
        theEndMillis = 0L;
        selectedTime.setText("");
    }

    /**
     * Replaces the old free-form TimePickerDialog. The customer must pick a date first,
     * then sees only start times that fit within shop hours, labeled as full where the
     * shop's configured capacity is already reserved for that interval.
     */
    private void openTimePicker() {
        if (theDateISO == null || theDateISO.isEmpty()) {
            Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (serviceDuration == null || serviceDuration.trim().isEmpty()) {
            Toast.makeText(this, "Service duration is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Loading available times...")
                .setCancelable(false)
                .show();

        FirebaseCapacityManager.getAvailableStartTimes(serviceProviderId, theDateISO, serviceDuration,
                new AvailableTimesCallback() {
                    @Override
                    public void onSuccess(List<TimeSlotOption> slots) {
                        loadingDialog.dismiss();
                        showTimeSlotDialog(slots);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        loadingDialog.dismiss();
                        Toast.makeText(CustomerBookService.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showTimeSlotDialog(List<TimeSlotOption> slots) {
        if (slots.isEmpty()) {
            Toast.makeText(this, "No start times fit the shop's hours for this service.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> labels = new ArrayList<>();
        for (TimeSlotOption slot : slots) {
            labels.add(slot.isAvailable() ? slot.getLabel() : slot.getLabel() + " — FULL");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, labels) {
            @Override
            public boolean isEnabled(int position) {
                return slots.get(position).isAvailable();
            }

            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setEnabled(slots.get(position).isAvailable());
                return view;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a start time");
        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        AlertDialog dialog = builder.setView(listView).setNegativeButton("Cancel", null).create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            TimeSlotOption chosen = slots.get(position);
            if (!chosen.isAvailable()) return;

            theStartMillis = chosen.getStartMillis();
            theEndMillis = chosen.getEndMillis();
            theTime = chosen.getLabel();
            selectedTime.setText(chosen.getLabel()
                    + "  (ends " + com.example.ca_lalay.utils.BookingTimeUtils.formatTime(theEndMillis) + ")");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void bookTheService() {

        String bookingServiceId = serviceId;
        String bookingeServiceProviderId = serviceProviderId;
        additionalNotes = additionaalNotes.getText().toString();
        String bookingServiceImage = serviceImage;
        String bookingServiceName = serviceName;
        String bookingServiceType = serviceType;
        String bookingServicePrice = servicePrice;
        String bookingServiceProviderName = shopOwnerName;
        String bookingServiceRate = shopRating;
        String bookingServiceDuration = serviceDuration;
        String bookingServiceTotalAmount = servicePrice;
        String bookingServiceDate = theDate;
        String bookingServiceTime = theTime;
        String bookingServiceProviderImage = shopImage;
        String bookingShopLocation = theShopLocation;

        String thePaymentSelected = selectedPaymentMethod.getText().toString();

        String theOverall = billingTotalAmount.getText().toString();

        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

        String uid = FirebaseAuthManager.getCurrentUserUid();

        if(enteredVehicle.equals("") || enteredVehicle == null) {
            Toast.makeText(this, "Please select a vehicle", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.getText().toString().trim().isEmpty()) {
            dateTimeError.setVisibility(View.VISIBLE);
            return;
        }

        if (selectedTime.getText().toString().trim().isEmpty() || theStartMillis == 0L) {
            dateTimeError.setVisibility(View.VISIBLE);
            return;
        }

        if(theSelectedPaymentMethod.equals("Select payment method")) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        if(additionalNotes.isEmpty()) {
            additionalNotes = "None";
        }

        String currentTimestamp = String.valueOf(System.currentTimeMillis());

        dateTimeError.setVisibility(View.GONE);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Booking")
                .setMessage("Are you sure you want to book this service?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    confirmBookingBtn.setEnabled(false);
                    confirmBookingBtn.setText("Booking...");
                    confirmBookingBtn.setAlpha(0.5f);

                    FirebaseDatabaseManager.getCustomerData(uid, new CustomerDataCallback() {
                        @Override
                        public void onSuccess(String message, UserModel customerData) {

                            BookingCreationParams params = new BookingCreationParams();
                            params.serviceId = bookingServiceId;
                            params.bookerId = uid;
                            params.bookerImage = customerData.getImageUrl();
                            params.serviceImage = bookingServiceImage;
                            params.serviceProviderImage = bookingServiceProviderImage;
                            params.bookerName = customerData.getFirstname() + " " + customerData.getLastname();
                            params.serviceName = bookingServiceName;
                            params.serviceProviderName = bookingServiceProviderName;
                            params.serviceProviderLocation = bookingShopLocation;
                            params.bookerContactNum = customerData.getContactNum();
                            params.bookerEmail = customerData.getEmail();
                            params.bookerVehicle = enteredVehicle;
                            params.bookerAdditionalNotes = additionalNotes;
                            params.bookerRating = "0";
                            params.bookerFeedback = "None";
                            params.serviceType = bookingServiceType;
                            params.servicePrice = servicePrice;
                            params.serviceDuration = bookingServiceDuration;
                            params.serviceDate = theDateISO;
                            params.serviceTime = bookingServiceTime;
                            params.createdAt = createdAt;
                            params.bookerVehicleSize = theVehicleSize;
                            params.bookerNumber = customerData.getContactNum();
                            params.timestamp = currentTimestamp;
                            params.bookerPaymentMethod = theSelectedPaymentMethod;
                            params.isBookerPaid = "no";
                            params.additionalFeeVehicleSize = overallAddition;
                            params.overallPriceOfService = theOverall;

                            FirebaseCapacityManager.reserveSlotAndCreateBooking(
                                    bookingeServiceProviderId, theDateISO, theStartMillis, theEndMillis,
                                    params, new CustomerBookingCallback() {
                                        @Override
                                        public void onSuccess(String message, String bookingId) {

                                            confirmBookingBtn.setEnabled(false);
                                            confirmBookingBtn.setText("BOOKED SUCCESSFULLY!");
                                            confirmBookingBtn.setAlpha(0.5f);
                                            Toast.makeText(CustomerBookService.this, message, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CustomerBookService.this, CustomerServiceBookedBilling.class);
                                            intent.putExtra("serviceName", serviceName);
                                            intent.putExtra("serviceProvider", shopOwnerName);
                                            intent.putExtra("vehicle", enteredVehicle);
                                            intent.putExtra("vehicleSize", theVehicleSize);
                                            intent.putExtra("date", theDate);
                                            intent.putExtra("time", theTime);
                                            intent.putExtra("baseFee", servicePrice);
                                            intent.putExtra("additionalFee", overallAddition);
                                            intent.putExtra("serviceDuration", serviceDuration);
                                            intent.putExtra("paymentMethod", theSelectedPaymentMethod);
                                            intent.putExtra("paymentStatus", "no");
                                            intent.putExtra("bookingStatus", theBookingStatus);
                                            intent.putExtra("theShopID", theShopID);
                                            intent.putExtra("customerName", customerData.getFirstname() + " " + customerData.getLastname());

                                            intent.putExtra("billingNumber", "none");

                                            intent.putExtra("serviceAdded", "none");
                                            intent.putExtra("serviceAddedTotalPrice", "0");
                                            intent.putExtra("serviceAddedRemarks", "none");
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(String message) {
                                            Toast.makeText(CustomerBookService.this, message, Toast.LENGTH_SHORT).show();
                                            confirmBookingBtn.setEnabled(true);
                                            confirmBookingBtn.setText("CONFIRM BOOKING");
                                            confirmBookingBtn.setAlpha(1f);
                                        }
                                    });
                        }

                        @Override
                        public void onFailure(String message) {

                        }
                    });

                })
                .setNegativeButton("No", null)
                .show();


    }
    private void setLoadingState() {
        confirmBookingBtn.setEnabled(false);
        confirmBookingBtn.setAlpha(0.5f);
        confirmBookingBtn.setText("BOOKING...");
    }

    private void setNormalState() {
        confirmBookingBtn.setEnabled(true);
        confirmBookingBtn.setAlpha(1f);
        confirmBookingBtn.setText("CONFIRM BOOKING");
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

            receiptDivider.setVisibility(View.VISIBLE);
            receiptActions.setVisibility(View.VISIBLE);
            receiptUploadedTV.setText("Receipt uploaded");

        }
    }
}