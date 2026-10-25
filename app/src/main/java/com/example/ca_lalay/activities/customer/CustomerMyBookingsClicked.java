package com.example.ca_lalay.activities.customer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseBillingManager;
import com.example.ca_lalay.firebase.FirebaseBookingManager;
import com.example.ca_lalay.firebase.FirebaseCapacityManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseOnlinePaymentDetailsManager;
import com.example.ca_lalay.firebase.FirebaseServiceReportsManager;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.callback.AvailableTimesCallback;
import com.example.ca_lalay.firebase.callback.BillNumberCallback;
import com.example.ca_lalay.firebase.callback.CustomerDataCallback;
import com.example.ca_lalay.firebase.callback.OnStatusUpdateCallback;
import com.example.ca_lalay.firebase.callback.SendServiceModificationRequestCallback;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.firebase.callback.UpdateBookingCallback;
import com.example.ca_lalay.model.OnlinePaymentDetailsModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.example.ca_lalay.model.TimeSlotOption;
import com.example.ca_lalay.model.UserModel;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerMyBookingsClicked extends AppCompatActivity {

    private Spinner updateStatusSpinner;

    private ImageView pendingIcon, inProgressIcon, doneIcon, theBookerImage;
    private TextView pendingText, inProgressText, doneText, cancelledBookingStatus, reviewTV, theBookerName, theBookerNumber;
    private View line1, line2;
    private boolean isExpired;

    private TextView rejectedReasonTV;
    private String theReasonRejected;

    private long pendingStartMillis = 0L;
    private long pendingEndMillis = 0L;

    private ShopOwnerModel currentShop;
    private UserModel currentUser;

    private Button confirmBtn, rejectBtn;

    private LinearLayout callCustomerLayout, messageCustomerLayout, contactTheShop;

    private ImageView backBtn, serviceProviderImage;
    private ImageView serviceImage;
    private TextView serviceName, servicePrice, rejectedBookingStatus;
    private ImageView progressIcon;
    private TextView bookingDate, bookingTime, bookingVehicle;
    private TextView bookingAdditionalNotes;
    private TextView serviceProviderName, serviceProviderRatings;
    private android.widget.RatingBar ratingBar;
    private TextView serviceProviderContactNum, serviceProviderLocation;
    private LinearLayout cancelBtn, contactShop, viewBill, statusBtnsLayout,  cancelBooking, rejectBooking,
            inProgressBooking, doneBooking, leaveAReviewBtn, bookerLayout, customerUILayout, editSchedule;

    String[] statusOptions = {"pending", "rejected", "inProgress", "done"};

    private String shopWillOpenAt, shopWillCloseAt;
    private String currentLoggedInUserID;
    private String theOverallPriceOfTheService;

    private LinearLayout marAsPaidLayout;
    private TextView paymentOption, paymentStatus;
    private String isBookerPaid;
    private String theBillingNumber;

    // UPLOAD RECEIPT
    private LinearLayout uploadReceiptLayout, uploadTheReceiptToDB, receiptActions;
    private TextView receiptUploadedTV, viewReceipt, reUploadReceipt, uploadLABEL, viewShopOnlinePaymentsDetailsLink;
    private View receiptDivider;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView viewReceiptUploadedTV;
    private LinearLayout VIEWTheUploadedReceiptLayout;
    private String theReceiptFromDB;


    //BOOKING MODIFICATION LAYOUT
    private TextView modificationRequestHeader, modificationRequestBody, sendModificationRequestLabel;
    private LinearLayout bookingModificationRequestLayout, additionalServicesTextLink, sendModificationRequestLayout, modifyBookingLayout;
    private Button approveModificationBtn, rejectModificationBtn;
    private String shopSubmitServiceAddRequest, customerApprovesServiceChanges, customerRespondedToServiceChanges;
    private String theServiceAdded, theServiceAddedTotalPrice, theServiceAddedRemarks;

    private String theShopName, theVehicleSize, theSelectedPaymentMethod, overallAddition, theServiceDuration, theShopImage, providerID, theBookingId, theBookingStatus, theServiceImage, theServiceName, theServicePrice, theBookingDate, theBookingTime, theBookingVehicle, theBookingAdditionalNotes, theServiceProviderImage, theServiceProviderName, theServiceProviderContactNum, theServiceProviderLocation, theServiceProviderRatings;
    private String theStarRatings, theRatingsID, theRatingsComment, theDateRated, theIsServiceRated;
    private String bookerName, bookerImage, bookerContactNumber, bookerID, theServiceID, theServiceProviderID;
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_my_bookings_clicked);


        // UPLOAD RECEIPT
        uploadReceiptLayout = findViewById(R.id.uploadReceiptLayout);
        uploadTheReceiptToDB = findViewById(R.id.uploadTheReceiptToDB);
        receiptUploadedTV = findViewById(R.id.receiptUploadedTV);
        viewReceipt = findViewById(R.id.viewReceipt);
        reUploadReceipt = findViewById(R.id.reUploadReceipt);
        receiptDivider = findViewById(R.id.receiptDivider);
        receiptActions = findViewById(R.id.receiptActions);
        uploadLABEL = findViewById(R.id.uploadLABEL);
        VIEWTheUploadedReceiptLayout  =findViewById(R.id.VIEWTheUploadedReceiptLayout);
        viewReceiptUploadedTV = findViewById(R.id.viewReceiptUploadedTV);
        viewShopOnlinePaymentsDetailsLink = findViewById(R.id.viewShopOnlinePaymentsDetailsLink);

        backBtn = findViewById(R.id.backBtn);
        serviceImage = findViewById(R.id.serviceImage);
        serviceName = findViewById(R.id.serviceName);
        servicePrice = findViewById(R.id.servicePrice);
        bookingDate = findViewById(R.id.bookingDate);
        bookingTime = findViewById(R.id.bookingTime);
        bookingVehicle = findViewById(R.id.bookingVehicle);
        bookingAdditionalNotes = findViewById(R.id.bookingAdditionalNotes);
        serviceProviderImage = findViewById(R.id.serviceProviderImage);
        serviceProviderName = findViewById(R.id.serviceProviderName);
        serviceProviderRatings = findViewById(R.id.serviceProviderRatings);
        rejectedReasonTV = findViewById(R.id.rejectedReasonTV);
        ratingBar = findViewById(R.id.ratingBar);
        serviceProviderContactNum = findViewById(R.id.serviceProviderContactNum);
        serviceProviderLocation = findViewById(R.id.serviceProviderLocation);
        contactShop = findViewById(R.id.contactShop);

        confirmBtn = findViewById(R.id.confirmBtn);
        rejectBtn = findViewById(R.id.rejectBtn);
        viewBill = findViewById(R.id.viewBill);

        paymentOption = findViewById(R.id.paymentOption);
        paymentStatus = findViewById(R.id.paymentStatus);
        marAsPaidLayout = findViewById(R.id.marAsPaidLayout);


        //BOOKING MODIFCATION LAYOUTS INIT
        sendModificationRequestLabel = findViewById(R.id.sendModificationRequestLabel);
        sendModificationRequestLayout = findViewById(R.id.sendModificationRequestLayout);
        modificationRequestBody = findViewById(R.id.modificationRequestBody);
        modificationRequestHeader = findViewById(R.id.modificationRequestHeader);
        bookingModificationRequestLayout = findViewById(R.id.bookingModificationRequestLayout);
        approveModificationBtn = findViewById(R.id.approveModificationBtn);
        rejectModificationBtn = findViewById(R.id.rejectModificationBtn);
        modifyBookingLayout = findViewById(R.id.modifyBookingLayout);
        additionalServicesTextLink = findViewById(R.id.additionalServicesTextLink);

        currentShop = ShopOwnerSession.getInstance().getCurrentShopOwner();
        currentUser = UserSession.getInstance().getCurrentUser();

        editSchedule = findViewById(R.id.editSchedule);
        currentLoggedInUserID = FirebaseAuthManager.getCurrentUserUid();

        // status bar
        pendingIcon = findViewById(R.id.pendingIcon);
        inProgressIcon = findViewById(R.id.inProgressIcon);
        doneIcon = findViewById(R.id.doneIcon);
        pendingText = findViewById(R.id.pendingText);
        inProgressText = findViewById(R.id.inProgressText);
        doneText = findViewById(R.id.doneText);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);

        reviewTV = findViewById(R.id.reviewTV);
        rejectedBookingStatus = findViewById(R.id.rejectedBookingStatus);

        leaveAReviewBtn = findViewById(R.id.leaveAReviewBtn);
        bookerLayout = findViewById(R.id.bookerLayout);
        customerUILayout = findViewById(R.id.customerUILayout);

        callCustomerLayout = findViewById(R.id.callCustomerLayout);
        messageCustomerLayout = findViewById(R.id.messageCustomerLayout);

        theBookerImage = findViewById(R.id.bookerImage);
        theBookerName = findViewById(R.id.bookerName);
        theBookerNumber = findViewById(R.id.bookerNumber);


        cancelledBookingStatus = findViewById(R.id.cancelledBookingStatus);
        rejectBooking = findViewById(R.id.rejectBooking);
        inProgressBooking = findViewById(R.id.inProgressBooking);
        doneBooking = findViewById(R.id.doneBooking);

        statusBtnsLayout = findViewById(R.id.statusBtnsLayout);

        cancelBooking = findViewById(R.id.cancelBooking);

        Intent intent = getIntent();
        theServiceImage = intent.getStringExtra("theServiceImage");
        theServiceName = intent.getStringExtra("theServiceName");
        theServicePrice = intent.getStringExtra("theServicePrice");
        theBookingDate = intent.getStringExtra("theServiceDate");
        theBookingTime = intent.getStringExtra("theServiceTime");
        theBookingVehicle = intent.getStringExtra("theServiceVehicle");
        theBookingAdditionalNotes = intent.getStringExtra("theBookingAdditionalNotes");
        theServiceProviderImage = intent.getStringExtra("theServiceProviderImage");
        theServiceProviderName = intent.getStringExtra("theServiceProviderName");
        theServiceProviderContactNum = intent.getStringExtra("theServiceProviderContactNum");
        theServiceProviderLocation = intent.getStringExtra("theServiceProviderLocation");
        theServiceProviderRatings = intent.getStringExtra("theServiceProviderRating");
        theBookingStatus = intent.getStringExtra("theBookingStatus");
        theBookingId = intent.getStringExtra("theBookingId");
        providerID = intent.getStringExtra("serviceProviderId");
        theShopName = intent.getStringExtra("shopName");
        theShopImage = intent.getStringExtra("shopImage");

        theReasonRejected = intent.getStringExtra("rejectedBookingReason");

        //BOOKING MODIFCATION GET VALUES
        shopSubmitServiceAddRequest  = intent.getStringExtra("shopSubmitServiceAddRequest");
        customerApprovesServiceChanges = intent.getStringExtra("customerApprovesServiceChanges");
        customerRespondedToServiceChanges = intent.getStringExtra("customerRespondedToServiceChanges");
        theServiceAdded = intent.getStringExtra("serviceAdded");
        theServiceAddedTotalPrice = intent.getStringExtra("serviceAddedTotalPrice");
        theServiceAddedRemarks = intent.getStringExtra("serviceAddedRemarks");
        // receipt uploaded from db
        theReceiptFromDB = intent.getStringExtra("imageReceipt");
        // billing number
        theBillingNumber = intent.getStringExtra("billingNumber");

        theVehicleSize = intent.getStringExtra("theVehicleSize");
        overallAddition = intent.getStringExtra("additionalFeeVehicleSize");
        theSelectedPaymentMethod = intent.getStringExtra("selectedPaymentMethod");
        isBookerPaid = intent.getStringExtra("isBookerPaid");
        theOverallPriceOfTheService = intent.getStringExtra("overallPriceOfService");

        bookerName = intent.getStringExtra("bookerName");
        bookerImage = intent.getStringExtra("bookerImage");
        bookerContactNumber = intent.getStringExtra("bookerContactNum");
        bookerID = intent.getStringExtra("bookerID");
        theServiceID = intent.getStringExtra("theServiceID");
        theServiceProviderID = intent.getStringExtra("theServiceProviderID");

        theStarRatings = intent.getStringExtra("theStarRatings");
        theRatingsID = intent.getStringExtra("theRatingID");
        theRatingsComment = intent.getStringExtra("theRatingComment");
        theDateRated = intent.getStringExtra("theDateRated");
        theIsServiceRated = intent.getStringExtra("theIsServiceRated");
        theServiceDuration = intent.getStringExtra("theShopsServiceDuration");

        isExpired = getIntent().getBooleanExtra("isExpired", false);


        updateButtonStates(theBookingStatus);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        displayTheUploadReceiptButton();
        displayTheImageReceipt();
        displayRejectedReason();
        displayPaymentStatus();
        setupEditScheduleBtn();
        getProviderDatas();
        setUpIcons();
        setupUIForCustomer();
        setupUIForShopOwner();
        setUpDatas();
        setUpReviewRatingsUI();
        displayShopModificationLayout();

        callCustomerLayout.setOnClickListener(d -> {
            String phoneNumber = bookerContactNumber;

            Intent call = new Intent (Intent.ACTION_DIAL);
            call.setData(Uri.parse(("tel:" + phoneNumber)));
            startActivity(call);
        });

        viewBill.setOnClickListener(s -> {
            Intent goToBillPage = new Intent(CustomerMyBookingsClicked.this, CustomerServiceBookedBilling.class);
            goToBillPage.putExtra("serviceName", theServiceName);
            goToBillPage.putExtra("serviceProvider", theServiceProviderName);
            goToBillPage.putExtra("vehicle", theBookingVehicle);
            goToBillPage.putExtra("vehicleSize", theVehicleSize);
            goToBillPage.putExtra("date", theBookingDate);
            goToBillPage.putExtra("time", theBookingTime);
            goToBillPage.putExtra("baseFee", theServicePrice);
            goToBillPage.putExtra("additionalFee", overallAddition);
            goToBillPage.putExtra("serviceDuration", theServiceDuration);
            goToBillPage.putExtra("paymentMethod", theSelectedPaymentMethod);
            goToBillPage.putExtra("paymentStatus", isBookerPaid);
            goToBillPage.putExtra("bookingStatus", theBookingStatus);
            goToBillPage.putExtra("theShopID", theServiceProviderID);
            goToBillPage.putExtra("customerName", bookerName);

            goToBillPage.putExtra("billingNumber", theBillingNumber);

            goToBillPage.putExtra("serviceAdded", theServiceAdded);
            goToBillPage.putExtra("serviceAddedTotalPrice", theServiceAddedTotalPrice);
            goToBillPage.putExtra("serviceAddedRemarks", theServiceAddedRemarks);

            startActivity(goToBillPage);
        });

        contactShop.setOnClickListener(s -> {
            messageNowCustomerOrShop(bookerID);
        });

        messageCustomerLayout.setOnClickListener(s -> {
            messageNowCustomerOrShop(bookerID);
        });

        leaveAReviewBtn.setOnClickListener(d -> {
            Intent reviewPage = new Intent(CustomerMyBookingsClicked.this, CustomerSubmitRatings.class);
            reviewPage.putExtra("shopImage", theServiceImage);
            reviewPage.putExtra("shopName", theServiceProviderName);
            reviewPage.putExtra("serviceName", theServiceName);

            reviewPage.putExtra("bookerName", bookerName);
            reviewPage.putExtra("bookerImage", bookerImage);
            reviewPage.putExtra("bookingID", theBookingId);

            reviewPage.putExtra("theServiceID", theServiceID);
            reviewPage.putExtra("theServiceProviderID", theServiceProviderID);

            startActivity(reviewPage);
            finish();
        });

        marAsPaidLayout.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Mark as Paid")
                    .setMessage("Are you sure you have received the customer's payment?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        markTheBookingAsPaid();
                    })
                    .show();
        });

        viewReceipt.setPaintFlags(
                viewReceipt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );
        reUploadReceipt.setPaintFlags(
                reUploadReceipt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );
        viewReceiptUploadedTV.setPaintFlags(
                viewReceiptUploadedTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );

        uploadReceiptLayout.setOnClickListener(d -> {
            choosePhotoFromGallery();
        });

        reUploadReceipt.setOnClickListener(f -> {
            choosePhotoFromGallery();
        });

        viewShopOnlinePaymentsDetailsLink.setOnClickListener(f -> {
            showOnlinePaymentDetailsDialog();
        });

        uploadTheReceiptToDB.setOnClickListener(f -> {

            new AlertDialog.Builder(this)
                    .setTitle("Upload Receipt")
                    .setMessage("Are you sure you want to upload this payment receipt?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Upload", (dialog, which) -> {
                        uploadTheImageReceipt();
                    })
                    .show();
        });

        viewReceiptUploadedTV.setOnClickListener(v -> {

            if (theReceiptFromDB == null || theReceiptFromDB.isEmpty()) {
                Toast.makeText(this, "Receipt image not available.", Toast.LENGTH_SHORT).show();
                return;
            }

            View dialogView = getLayoutInflater()
                    .inflate(R.layout.dialog_view_receipt, null);

            ImageView receiptImageView =
                    dialogView.findViewById(R.id.receiptImageView);

            Picasso.get()
                    .load(theReceiptFromDB)
                    .placeholder(R.drawable.sent_image_placeholder)
                    .error(R.drawable.sent_image_placeholder)
                    .into(receiptImageView);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setPositiveButton("Close", null)
                    .create();

            dialog.show();
        });

        viewReceipt.setOnClickListener(v -> {

            if (imageUri == null) {
                Toast.makeText(this, "No receipt selected.", Toast.LENGTH_SHORT).show();
                return;
            }

            View dialogView = getLayoutInflater()
                    .inflate(R.layout.dialog_view_receipt, null);

            ImageView receiptImageView =
                    dialogView.findViewById(R.id.receiptImageView);

            receiptImageView.setImageURI(imageUri);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setPositiveButton("Close", null)
                    .create();

            dialog.show();
        });

        // this is for customer cancelling the booking
        cancelBooking.setOnClickListener(d -> {
            cancelTheBooking();
        });

        // this is for shop cancelling the booking, it just named as rejectbooking
        // i didnt changed it so no other is affected
        rejectBooking.setOnClickListener(v -> {
            showStatusConfirmation(
                    "cancelled",
                    "Are you sure you want to cancel this booking?"
            );
        });

        inProgressBooking.setOnClickListener(v -> {
            showStatusConfirmation(
                    "inProgress",
                    "Are you sure you want to mark this booking as in progress?"
            );
        });

        doneBooking.setOnClickListener(v -> {
            showStatusConfirmationDoneBooking(
                    "Are you sure you want to mark this booking as done?",
                    "done"
            );
        });

        confirmBtn.setOnClickListener(s -> {

            showStatusConfirmation(
                    "confirmed",
                    "Are you sure you want to confirm this booking?"
            );
        });

        rejectBtn.setOnClickListener(d -> {

            new AlertDialog.Builder(this)
                    .setTitle("Reject Booking")
                    .setMessage("Are you sure you want to reject this booking?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", (dialog, which) -> {

                        // Open reason dialog
                        showRejectReasonDialog();

                    })
                    .show();
        });

        backBtn.setOnClickListener(s -> {
            finish();
        });

        editSchedule.setOnClickListener(v -> {
            showEditScheduleDialog();
        });

        additionalServicesTextLink.setOnClickListener(v -> {

            Dialog dialog = new Dialog(CustomerMyBookingsClicked.this);

            dialog.setContentView(R.layout.dialog_additional_services);

            Window window = dialog.getWindow();

            if (window != null) {
                window.setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT)
                );
            }

            TextView serviceAddedDetails =
                    dialog.findViewById(R.id.serviceAddedDetails);

            TextView serviceAddedPrice =
                    dialog.findViewById(R.id.serviceAddedPrice);

            TextView serviceAddedRemarks =
                    dialog.findViewById(R.id.serviceAddedRemarks);

            Button closeAdditionalServicesBtn =
                    dialog.findViewById(R.id.closeAdditionalServicesBtn);

            // Display saved information
            serviceAddedDetails.setText(
                    theServiceAdded
            );

            serviceAddedPrice.setText(
                    "₱" + theServiceAddedTotalPrice
            );

            serviceAddedRemarks.setText(
                    theServiceAddedRemarks
            );

            closeAdditionalServicesBtn.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        });

        modifyBookingLayout.setOnClickListener(v -> {

            Dialog dialog = new Dialog(CustomerMyBookingsClicked.this);

            dialog.setContentView(R.layout.dialog_modify_booking);

            Window window = dialog.getWindow();

            if (window != null) {
                window.setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT)
                );

                window.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }

            EditText serviceAddedInput =
                    dialog.findViewById(R.id.serviceAddedInput);

            EditText serviceAddedTotalPriceInput =
                    dialog.findViewById(R.id.serviceAddedTotalPriceInput);

            EditText serviceAddedRemarksInput =
                    dialog.findViewById(R.id.serviceAddedRemarksInput);

            Button cancelModifyBookingBtn =
                    dialog.findViewById(R.id.cancelModifyBookingBtn);

            Button saveModifyBookingBtn =
                    dialog.findViewById(R.id.saveModifyBookingBtn);

            cancelModifyBookingBtn.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            serviceAddedInput.setText(theServiceAdded);
            serviceAddedTotalPriceInput.setText(theServiceAddedTotalPrice);
            serviceAddedRemarksInput.setText(theServiceAddedRemarks);

            saveModifyBookingBtn.setOnClickListener(v1 -> {

                String serviceAdded =
                        serviceAddedInput.getText().toString().trim();

                String serviceAddedTotalPrice =
                        serviceAddedTotalPriceInput.getText().toString().trim();

                String serviceAddedRemarks =
                        serviceAddedRemarksInput.getText().toString().trim();

                if (serviceAdded.isEmpty()) {
                    serviceAddedInput.setError(
                            "Please enter the additional service."
                    );
                    serviceAddedInput.requestFocus();
                    return;
                }

                if (serviceAddedTotalPrice.isEmpty()) {
                    serviceAddedTotalPriceInput.setError(
                            "Please enter the additional total price."
                    );
                    serviceAddedTotalPriceInput.requestFocus();
                    return;
                }

                if (serviceAddedRemarks.isEmpty()) {
                    serviceAddedRemarksInput.setError(
                            "Please enter remarks."
                    );
                    serviceAddedRemarksInput.requestFocus();
                    return;
                }

                theServiceAdded = serviceAdded;
                theServiceAddedTotalPrice = serviceAddedTotalPrice;
                theServiceAddedRemarks = serviceAddedRemarks;

                modifyTheBooking(serviceAdded, serviceAddedTotalPrice, serviceAddedRemarks, dialog);

                double OVERALLPRICE = Double.parseDouble(theOverallPriceOfTheService);
                double ADDITIONALSERVICEPRICE = Double.parseDouble(theServiceAddedTotalPrice);
                double TOTALPRICE = OVERALLPRICE + ADDITIONALSERVICEPRICE;

                servicePrice.setText("₱" + TOTALPRICE);
            });

            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        });

        sendModificationRequestLayout.setOnClickListener(s -> {

            new AlertDialog.Builder(CustomerMyBookingsClicked.this)
                    .setTitle("Send Modification Request")
                    .setMessage(
                            "Are you sure you want to send a modification request to the customer?"
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Send", (dialog, which) -> {

                        FirebaseBookingManager.sendServiceModificationRequest(
                                theBookingId,
                                new SendServiceModificationRequestCallback() {

                                    @Override
                                    public void onSuccess(String message) {

                                        sendModificationRequestLayout.setVisibility(
                                                View.VISIBLE
                                        );

                                        sendModificationRequestLabel.setText(
                                                "Awaiting Customer Response"
                                        );

                                        // Disable while waiting for customer response.
                                        sendModificationRequestLayout.setEnabled(
                                                false
                                        );

                                        sendModificationRequestLabel.setAlpha(
                                                0.5f
                                        );

                                        Toast.makeText(
                                                CustomerMyBookingsClicked.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    @Override
                                    public void onFailure(String message) {

                                        Toast.makeText(
                                                CustomerMyBookingsClicked.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        );
                    })
                    .show();
        });

        approveModificationBtn.setOnClickListener(s -> {

            new AlertDialog.Builder(CustomerMyBookingsClicked.this)
                    .setTitle("Approve Modification Request")
                    .setMessage("Are you sure you want to approve the shop's modification request?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Approve", (dialog, which) -> {

                        FirebaseBookingManager.approveServiceModificationRequest(
                                theBookingId,
                                new SendServiceModificationRequestCallback() {

                                    @Override
                                    public void onSuccess(String message) {

                                        bookingModificationRequestLayout.setVisibility(View.GONE);

                                        approveModificationBtn.setVisibility(View.GONE);
                                        rejectModificationBtn.setVisibility(View.GONE);

                                        modificationRequestHeader.setVisibility(View.VISIBLE);
                                        modificationRequestHeader.setText(
                                                "You approved the shop's modification request!"
                                        );

                                        modificationRequestBody.setVisibility(View.GONE);

                                        Toast.makeText(
                                                CustomerMyBookingsClicked.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    @Override
                                    public void onFailure(String message) {

                                        Toast.makeText(
                                                CustomerMyBookingsClicked.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        );
                    })
                    .show();
        });

        rejectModificationBtn.setOnClickListener(f -> {

            new AlertDialog.Builder(CustomerMyBookingsClicked.this)
                    .setTitle("Reject Modification Request")
                    .setMessage("Are you sure you want to reject the shop's modification request?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reject", (dialog, which) -> {

                        FirebaseBookingManager.rejectServiceModificationRequest(
                                theBookingId,
                                new SendServiceModificationRequestCallback() {

                                    @Override
                                    public void onSuccess(String message) {

                                        bookingModificationRequestLayout.setVisibility(View.GONE);

                                        approveModificationBtn.setVisibility(View.GONE);
                                        rejectModificationBtn.setVisibility(View.GONE);

                                        modificationRequestHeader.setVisibility(View.VISIBLE);
                                        modificationRequestHeader.setText(
                                                "You rejected the shop's modification request!"
                                        );

                                        modificationRequestBody.setVisibility(View.GONE);

                                        Toast.makeText(
                                                CustomerMyBookingsClicked.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    @Override
                                    public void onFailure(String message) {

                                        Toast.makeText(
                                                CustomerMyBookingsClicked.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        );
                    })
                    .show();
        });

    }
    private void displayRejectedReason() {
        if(theReasonRejected.isEmpty() || theReasonRejected == null) {
            rejectedReasonTV.setVisibility(View.GONE);
        } else {
            if("none".equals(theReasonRejected)) {
                rejectedReasonTV.setVisibility(View.GONE);
            } else {
                rejectedReasonTV.setVisibility(View.VISIBLE);
                rejectedReasonTV.setText(theReasonRejected);
            }
        }
    }
    private void setTheRejectedBookingReason(String reason, String status) {
        FirebaseBookingManager.bookingRejectedReason(theBookingId, reason, new FirebaseBookingManager.OnUpdateRejectedBookingReason() {
            @Override
            public void onSuccess() {
                updateStatus(status);
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }
    private void showRejectReasonDialog() {

        EditText reasonInput = new EditText(this);
        reasonInput.setHint("Enter reason for rejecting this booking");
        reasonInput.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        int padding = (int) (20 * getResources().getDisplayMetrics().density);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, 0, padding, 0);
        container.addView(reasonInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reason for Rejection")
                .setView(container)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reject Booking", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            Button rejectButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            rejectButton.setOnClickListener(v -> {

                String reason = reasonInput.getText()
                        .toString()
                        .trim();

                if (reason.isEmpty()) {
                    reasonInput.setError("Please enter a reason");
                    reasonInput.requestFocus();
                    return;
                }
                setTheRejectedBookingReason(reason, "rejected");


                dialog.dismiss();
            });
        });

        dialog.show();
    }
    private void modifyTheBooking(String serviceAdded, String serviceAddedTotalPrice, String serviceAddedRemarks, Dialog dialog) {
        FirebaseBookingManager.saveServiceModification(
                theBookingId,
                serviceAdded,
                serviceAddedTotalPrice,
                serviceAddedRemarks,
                new SendServiceModificationRequestCallback() {

                    @Override
                    public void onSuccess(String message) {

                        Toast.makeText(
                                CustomerMyBookingsClicked.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();



                    }

                    @Override
                    public void onFailure(String message) {

                        Toast.makeText(
                                CustomerMyBookingsClicked.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
    private void displayShopModificationLayout() {
        if(currentLoggedInUserID.equals(theServiceProviderID)) {

            if (("yes".equals(shopSubmitServiceAddRequest)
                    && "yes".equals(customerRespondedToServiceChanges)
                    && "yes".equals(customerApprovesServiceChanges))) {
                if("done".equals(theBookingStatus) || "inProgress".equals(theBookingStatus)) {
                    additionalServicesTextLink.setVisibility(View.VISIBLE);
                }
            }

            if ("inProgress".equals(theBookingStatus)) {

            } else {
                sendModificationRequestLayout.setVisibility(View.GONE);
                return;
            }

            if ("no".equals(shopSubmitServiceAddRequest)) {

                sendModificationRequestLayout.setVisibility(View.VISIBLE);

                sendModificationRequestLabel.setText(
                        "Send Modification Request"
                );

                sendModificationRequestLayout.setEnabled(true);
                sendModificationRequestLabel.setAlpha(1.0f);

            }     else if ("yes".equals(shopSubmitServiceAddRequest)
                    && "no".equals(customerRespondedToServiceChanges)) {

                sendModificationRequestLayout.setVisibility(View.VISIBLE);

                sendModificationRequestLabel.setText(
                        "Awaiting Customer Response"
                );

                // Disable the sending button while waiting.
                sendModificationRequestLayout.setEnabled(false);
                sendModificationRequestLabel.setAlpha(0.5f);

            }    else if ("yes".equals(shopSubmitServiceAddRequest)
                    && "yes".equals(customerRespondedToServiceChanges)
                    && "no".equals(customerApprovesServiceChanges)) {

                sendModificationRequestLayout.setVisibility(View.VISIBLE);

                sendModificationRequestLabel.setText(
                        "Customer Rejected Request - Send Again"
                );

                sendModificationRequestLayout.setEnabled(true);
                sendModificationRequestLabel.setAlpha(1.0f);

            }  else if ("yes".equals(shopSubmitServiceAddRequest)
                    && "yes".equals(customerRespondedToServiceChanges)
                    && "yes".equals(customerApprovesServiceChanges)) {
                sendModificationRequestLayout.setVisibility(View.GONE);
                // Show your Modify Booking button here if you have one.
                modifyBookingLayout.setVisibility(View.VISIBLE);


            }

            if(isExpired) {
                sendModificationRequestLayout.setVisibility(View.GONE);
            }


        } else {
            // display the layout
            if("yes".equals(shopSubmitServiceAddRequest) && "no".equals(customerRespondedToServiceChanges)) {
                bookingModificationRequestLayout.setVisibility(View.VISIBLE);

            } else if("yes".equals(shopSubmitServiceAddRequest) && "yes".equals(customerRespondedToServiceChanges) && "yes".equals(customerApprovesServiceChanges)) {
                bookingModificationRequestLayout.setVisibility(View.VISIBLE);
                bookingModificationRequestLayout.setBackgroundColor(
                        Color.TRANSPARENT
                );
                bookingModificationRequestLayout.setVisibility(View.GONE);
                approveModificationBtn.setVisibility(View.GONE);
                rejectModificationBtn.setVisibility(View.GONE);
                modificationRequestHeader.setVisibility(View.VISIBLE);
                modificationRequestHeader.setText("You approved shop's modificatation request!");
                additionalServicesTextLink.setVisibility(View.VISIBLE);
                modificationRequestBody.setVisibility(View.GONE);

            } else if("yes".equals(shopSubmitServiceAddRequest) && "yes".equals(customerRespondedToServiceChanges) && "no".equals(customerApprovesServiceChanges)) {
                bookingModificationRequestLayout.setVisibility(View.VISIBLE);
                bookingModificationRequestLayout.setBackgroundColor(
                        Color.TRANSPARENT
                );
                bookingModificationRequestLayout.setVisibility(View.GONE);
                approveModificationBtn.setVisibility(View.GONE);
                rejectModificationBtn.setVisibility(View.GONE);
                modificationRequestHeader.setVisibility(View.VISIBLE);
                modificationRequestHeader.setText("You rejected shop's modificatation request!");
                modificationRequestBody.setVisibility(View.GONE);
            } else {
                bookingModificationRequestLayout.setVisibility(View.GONE);
            }

            if(isExpired) {
                sendModificationRequestLayout.setVisibility(View.GONE);
                bookingModificationRequestLayout.setVisibility(View.GONE);
            }

            if("done".equals(theBookingStatus)) {
                bookingModificationRequestLayout.setVisibility(View.GONE);
            }
        }


    }
    private void displayPaymentStatus() {
       if(isExpired) {
           marAsPaidLayout.setVisibility(View.GONE);
           return;
       }

        if(currentLoggedInUserID.equals(theServiceProviderID)) {
            if("confirmed".equals(theBookingStatus) || "inProgress".equals(theBookingStatus) || "done".equals(theBookingStatus)) {
                if("yes".equals(isBookerPaid)) {
                    marAsPaidLayout.setVisibility(View.GONE);
                } else {
                    marAsPaidLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    private void markTheBookingAsPaid() {
        FirebaseBookingManager.markBookingAsPaid(theBookingId, new FirebaseBookingManager.OnMarkBookingPaidListener() {
            @Override
            public void onSuccess() {
                marAsPaidLayout.setVisibility(View.GONE);
                paymentStatus.setText("[Paid]");
                isBookerPaid = "yes";
                Toast.makeText(CustomerMyBookingsClicked.this, "Booking marked as paid", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CustomerMyBookingsClicked.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showEditScheduleDialog() {

        // Safety check: only pending bookings can be edited
        if (!"pending".equalsIgnoreCase(theBookingStatus)) {

            new AlertDialog.Builder(this)
                    .setTitle("Schedule Cannot Be Changed")
                    .setMessage(
                            "Only pending bookings can be changed.\n\n" +
                                    "Once the booking has been confirmed by the shop owner, " +
                                    "the date and time can no longer be changed."
                    )
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_schedule, null);

        TextView selectedDateTV = view.findViewById(R.id.selectedDateTV);
        TextView selectedTimeTV = view.findViewById(R.id.selectedTimeTV);

        Button changeDateBtn = view.findViewById(R.id.changeDateBtn);
        Button changeTimeBtn = view.findViewById(R.id.changeTimeBtn);

        // Keep the current booking values. theBookingDate is already "yyyy-MM-dd"
        // and theBookingTime is already "hh:mm a", matching serviceDate/serviceTime.
        final String[] newDate = {theBookingDate};
        final String[] newTime = {theBookingTime};

        selectedDateTV.setText(newDate[0]);
        selectedTimeTV.setText(newTime[0]);

        // --------------------------------
        // CHANGE DATE
        // --------------------------------
        changeDateBtn.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date currentBookingDate = sdf.parse(theBookingDate);
                if (currentBookingDate != null) {
                    calendar.setTime(currentBookingDate);
                }
            } catch (Exception e) {
                Log.e("EDIT_SCHEDULE", "Date parsing error", e);
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (datePicker, year, month, dayOfMonth) -> {

                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, month, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        newDate[0] = sdf.format(selectedCalendar.getTime());

                        selectedDateTV.setText(newDate[0]);

                        // Changing the date invalidates whatever time was picked,
                        // since availability is date-specific.
                        newTime[0] = "";
                        selectedTimeTV.setText("");
                        pendingStartMillis = 0L;
                        pendingEndMillis = 0L;
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Prevent selecting yesterday or any previous date
            // Prevent selecting today or any previous date
            Calendar tomorrow = Calendar.getInstance();

            tomorrow.set(Calendar.HOUR_OF_DAY, 0);
            tomorrow.set(Calendar.MINUTE, 0);
            tomorrow.set(Calendar.SECOND, 0);
            tomorrow.set(Calendar.MILLISECOND, 0);

            tomorrow.add(Calendar.DAY_OF_MONTH, 1);

            datePickerDialog.getDatePicker().setMinDate(tomorrow.getTimeInMillis());

            datePickerDialog.show();
        });

        // --------------------------------
        // CHANGE TIME - now capacity-aware, same pattern as CustomerBookService
        // --------------------------------
        changeTimeBtn.setOnClickListener(v -> {

            if (newDate[0] == null || newDate[0].trim().isEmpty()) {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog loadingDialog = new AlertDialog.Builder(this)
                    .setMessage("Loading available times...")
                    .setCancelable(false)
                    .show();

            FirebaseCapacityManager.getAvailableStartTimes(
                    theServiceProviderID, newDate[0], theServiceDuration,
                    new AvailableTimesCallback() {
                        @Override
                        public void onSuccess(List<TimeSlotOption> slots) {
                            loadingDialog.dismiss();
                            showTimeSlotPicker(slots, newDate[0], newTime, selectedTimeTV);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            loadingDialog.dismiss();
                            Toast.makeText(CustomerMyBookingsClicked.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // --------------------------------
        // MODAL
        // --------------------------------
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Schedule")
                .setView(view)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("CONTINUE", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            Button continueButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            continueButton.setOnClickListener(v -> {

                if (newDate[0] == null || newDate[0].trim().isEmpty()) {
                    Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newTime[0] == null || newTime[0].trim().isEmpty() || pendingStartMillis == 0L) {
                    Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // No local operating-hours check needed here anymore - the time
                // picker only ever offers times that already fit shop hours, and
                // rescheduleBooking() re-validates it atomically regardless.

                dialog.dismiss();

                showScheduleChangeConfirmation(newDate[0], newTime[0]);
            });
        });

        dialog.show();
    }

    /** Shows the capacity-aware list of start times, mirroring CustomerBookService's picker. */
    private void showTimeSlotPicker(List<TimeSlotOption> slots, String forDateISO,
                                    String[] newTimeHolder, TextView selectedTimeTV) {
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
                View v = super.getView(position, convertView, parent);
                TextView text = v.findViewById(android.R.id.text1);
                text.setEnabled(slots.get(position).isAvailable());
                return v;
            }
        };

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select a start time")
                .setView(listView)
                .setNegativeButton("Cancel", null)
                .create();

        listView.setOnItemClickListener((parent, v, position, id) -> {
            TimeSlotOption chosen = slots.get(position);
            if (!chosen.isAvailable()) return;

            pendingStartMillis = chosen.getStartMillis();
            pendingEndMillis = chosen.getEndMillis();
            newTimeHolder[0] = chosen.getLabel();
            selectedTimeTV.setText(chosen.getLabel());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showScheduleChangeConfirmation(String newDate, String newTime) {

        new AlertDialog.Builder(this)
                .setTitle("Confirm Schedule Change")
                .setMessage(
                        "Are you sure you want to change your booking schedule to:\n\n" +
                                "Date: " + newDate + "\n" +
                                "Time: " + newTime + "\n\n" +
                                "Once the shop owner confirms this booking, " +
                                "the schedule can no longer be changed."
                )
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("CONFIRM", (dialog, which) -> {
                    updateBookingSchedule(newDate, newTime);
                })
                .show();
    }

    private void updateBookingSchedule(String newDate, String newTime) {

        // Extra safety check
        if (!"pending".equalsIgnoreCase(theBookingStatus)) {
            Toast.makeText(this, "This booking can no longer be changed.", Toast.LENGTH_LONG).show();
            return;
        }

        if (pendingStartMillis == 0L || pendingEndMillis == 0L) {
            Toast.makeText(this, "Please select a time slot again.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseCapacityManager.rescheduleBooking(
                theBookingId,
                theServiceProviderID,
                theBookingDate,   // old dateISO, before the change
                newDate,          // new dateISO
                pendingStartMillis,
                pendingEndMillis,
                newDate,
                newTime,
                new UpdateBookingCallback() {

                    @Override
                    public void onSuccess() {

                        // Update local values
                        theBookingDate = newDate;
                        theBookingTime = newTime;
                        pendingStartMillis = 0L;
                        pendingEndMillis = 0L;

                        // Update UI
                        bookingDate.setText(newDate);
                        bookingTime.setText(newTime);

                        Toast.makeText(
                                CustomerMyBookingsClicked.this,
                                "Booking schedule updated successfully.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(
                                CustomerMyBookingsClicked.this,
                                "Failed to update schedule: " + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void messageNowCustomerOrShop(String uid) {
        FirebaseDatabaseManager.getCustomerData(uid, new CustomerDataCallback() {
            @Override
            public void onSuccess(String message, UserModel customerData) {
                Intent intent = new Intent(CustomerMyBookingsClicked.this, CustomerMessageNow.class);


                // Customer
                intent.putExtra("customerId", uid);
                intent.putExtra("customerName", customerData.getFirstname() + " " + customerData.getLastname());
                intent.putExtra("customerImage", customerData.getImageUrl());

                // Shop
                intent.putExtra("shopId", providerID);
                intent.putExtra("shopName", theShopName);
                intent.putExtra("shopImage", theShopImage);

                startActivity(intent);
            }

            @Override
            public void onFailure(String message) {

            }
        });
    }
    private void showStatusConfirmationDoneBooking(String message, String status) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Action")
                .setMessage(message)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialog, which) -> {

                    FirebaseBillingManager.generateTicketNumber(new BillNumberCallback() {
                        @Override
                        public void onSuccess(String billNumber) {
                            theBillingNumber = billNumber;

                            FirebaseBookingManager.updateBillingNumber(theBookingId, billNumber, new FirebaseBookingManager.OnUpdateBillingNumberListener() {
                                @Override
                                public void onSuccess() {
                                    updateStatus(status);
                                }

                                @Override
                                public void onFailure(String error) {

                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    });

                })
                .show();
    }
    private void showStatusConfirmation(String status, String message) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Action")
                .setMessage(message)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", (dialog, which) -> {
                    updateStatus(status);

                    if(status.equals("done")) {
                        displayDoneProgressLayout();
                        statusBtnsLayout.setVisibility(View.GONE);
                        modifyBookingLayout.setVisibility(View.GONE);
                        sendModificationRequestLayout.setVisibility(View.GONE);
                    } else if (status.equals("inProgress")) {
                        displayInProgressLayout();
                        sendModificationRequestLayout.setVisibility(View.VISIBLE);
                        rejectBooking.setVisibility(View.GONE);
                        inProgressBooking.setVisibility(View.GONE);
                        doneBooking.setVisibility(View.VISIBLE);
                    } else if(status.equals("cancelled")) {
                        statusBtnsLayout.setVisibility(View.GONE);
                    } else if(status.equals("confirmed")) {
                        viewBill.setVisibility(View.VISIBLE);
                        marAsPaidLayout.setVisibility(View.VISIBLE);
                        confirmBtn.setVisibility(View.GONE);
                        rejectBtn.setVisibility(View.GONE);
                        statusBtnsLayout.setVisibility(View.VISIBLE);
                        rejectBooking.setVisibility(View.VISIBLE);
                        inProgressBooking.setVisibility(View.VISIBLE);
                        doneBooking.setVisibility(View.GONE);
                        setupDoneAndInProgressButton();
                    } else if (status.equals("rejected")) {
                        confirmBtn.setVisibility(View.GONE);
                        rejectBtn.setVisibility(View.GONE);
                        statusBtnsLayout.setVisibility(View.GONE);
                        finish();
                    }
                })
                .show();
    }
    private void setUpIcons() {
        String uid = FirebaseAuthManager.getCurrentUserUid();
        if(uid.equals(providerID)) {
            statusBtnsLayout.setVisibility(View.VISIBLE);
            cancelBooking.setVisibility(View.GONE);
            contactShop.setVisibility(View.GONE);
            editSchedule.setVisibility(View.GONE);

            if(isExpired &&
                    ("pending".equalsIgnoreCase(theBookingStatus)
                            || "confirmed".equalsIgnoreCase(theBookingStatus)
                            || "inProgress".equalsIgnoreCase(theBookingStatus))){
                statusBtnsLayout.setVisibility(View.GONE);
                return;
            }

            if(theBookingStatus.equals("pending")) {
                confirmBtn.setVisibility(View.VISIBLE);
                rejectBtn.setVisibility(View.VISIBLE);
                rejectBooking.setVisibility(View.GONE);
                inProgressBooking.setVisibility(View.GONE);
                doneBooking.setVisibility(View.GONE);
            } else if (theBookingStatus.equals("rejected")) {
                statusBtnsLayout.setVisibility(View.GONE);
            } else if(theBookingStatus.equals("confirmed")) {
                viewBill.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );

                String currentDate = sdf.format(new Date());
                if(theBookingDate.equals(currentDate)) {
                    statusBtnsLayout.setVisibility(View.VISIBLE);
                    rejectBooking.setVisibility(View.VISIBLE);
                    inProgressBooking.setVisibility(View.VISIBLE);
                    doneBooking.setVisibility(View.GONE);
                } else {
                    statusBtnsLayout.setVisibility(View.VISIBLE);
                    rejectBooking.setVisibility(View.VISIBLE);
                    inProgressBooking.setVisibility(View.GONE);
                    doneBooking.setVisibility(View.GONE);
                }

            }else if(theBookingStatus.equals("inProgress")) {
                viewBill.setVisibility(View.VISIBLE);
                rejectBooking.setVisibility(View.GONE);
                inProgressBooking.setVisibility(View.GONE);
                doneBooking.setVisibility(View.VISIBLE);
            } else if(theBookingStatus.equals("done")) {
                viewBill.setVisibility(View.VISIBLE);
                statusBtnsLayout.setVisibility(View.GONE);
            } else if(theBookingStatus.equals("cancelled")) {
                statusBtnsLayout.setVisibility(View.GONE);
            }

        }
    }
    private void updateStatus(String newStatus) {

        // disable buttons while updating
        setButtonsEnabled(false);

        FirebaseBookingManager.updateBookingStatus(
                theBookingId,
                newStatus,
                new OnStatusUpdateCallback() {
                    @Override
                    public void onSuccess(String message) {

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                        setButtonsEnabled(true);

                        // optional: update UI state
                        updateButtonStates(newStatus);

                        if(newStatus.equals("done")) {
                            FirebaseServiceReportsManager.updateServiceReport(providerID,theServiceID, theServiceName,theBookingDate);
                            statusBtnsLayout.setVisibility(View.GONE);

                            displayDoneProgressLayout();
                            statusBtnsLayout.setVisibility(View.GONE);
                            modifyBookingLayout.setVisibility(View.GONE);
                            sendModificationRequestLayout.setVisibility(View.GONE);

                        } else if (newStatus.equals("cancelled")) {
                            statusBtnsLayout.setVisibility(View.GONE);
                            finish();
                        } else if(newStatus.equals("rejected")) {
                            confirmBtn.setVisibility(View.GONE);
                            rejectBtn.setVisibility(View.GONE);
                            statusBtnsLayout.setVisibility(View.GONE);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(String error) {

                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();

                        setButtonsEnabled(true);
                    }
                }
        );
    }
    private void updateButtonStates(String status) {

        if (status.equals("done")) {
            setButtonsEnabled(false);
        }

        if (status.equals("cancel")) {
            inProgressBooking.setEnabled(false);
            doneBooking.setEnabled(false);
        }
    }
    private void setButtonsEnabled(boolean enabled) {

        rejectBooking.setEnabled(enabled);
        inProgressBooking.setEnabled(enabled);
        doneBooking.setEnabled(enabled);

        float alpha = enabled ? 1f : 0.5f;

        rejectBooking.setAlpha(alpha);
        inProgressBooking.setAlpha(alpha);
        doneBooking.setAlpha(alpha);
    }
    private void setUpDatas() {
        Picasso.get().load(theServiceImage).placeholder(R.drawable.sent_image_placeholder).into(serviceImage);
        serviceName.setText(theServiceName);
        double OVERALLPRICE = Double.parseDouble(theOverallPriceOfTheService);
        double ADDITIONALSERVICEPRICE = Double.parseDouble(theServiceAddedTotalPrice);
        double TOTALPRICE = OVERALLPRICE + ADDITIONALSERVICEPRICE;

        servicePrice.setText("₱" + TOTALPRICE);

        bookingDate.setText(theBookingDate);
        bookingTime.setText(theBookingTime);
        bookingVehicle.setText(theBookingVehicle);
        bookingAdditionalNotes.setText(theBookingAdditionalNotes);


        if("Online".equals(theSelectedPaymentMethod)) {
            paymentOption.setText("Online - Pay Directly to the shop");
        } else {
            paymentOption.setText("Cash – Pay at the Shop");
        }

        if("no".equals(isBookerPaid)) {
            paymentStatus.setText("[Unpaid]");
        } else {
            paymentStatus.setText("[Paid]");
        }

        if(isExpired &&
                ("pending".equalsIgnoreCase(theBookingStatus)
                        || "confirmed".equalsIgnoreCase(theBookingStatus)
                        || "inProgress".equalsIgnoreCase(theBookingStatus))) {
            editSchedule.setVisibility(View.GONE);
            cancelledBookingStatus.setText("BOOKING EXPIRED!");
            cancelledBookingStatus.setVisibility(View.VISIBLE);
            cancelBooking.setVisibility(View.GONE);

            setButtonsEnabled(false);
            statusBtnsLayout.setVisibility(View.GONE);

            inProgressIcon.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.in_progress_grey)
            );
            inProgressIcon.setBackgroundResource(R.drawable.circle_grey);

            doneIcon.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.done_grey)
            );
            doneIcon.setBackgroundResource(R.drawable.circle_grey);

            pendingIcon.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pending_grey)
            );
            pendingIcon.setBackgroundResource(R.drawable.circle_grey);
            return;
        }

        switch (theBookingStatus) {
            case "pending":


                inProgressIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.in_progress_grey)
                );
                inProgressIcon.setBackgroundResource(R.drawable.circle_grey);

                doneIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.done_grey)
                );
                doneIcon.setBackgroundResource(R.drawable.circle_grey);
                pendingText.setTextColor(Color.BLACK);
                break;
            case "confirmed":
                viewBill.setVisibility(View.VISIBLE);

                inProgressIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.in_progress_grey)
                );
                inProgressIcon.setBackgroundResource(R.drawable.circle_grey);

                doneIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.done_grey)
                );
                doneIcon.setBackgroundResource(R.drawable.circle_grey);
                pendingText.setTextColor(Color.BLACK);
                break;
            case "inProgress":
                viewBill.setVisibility(View.VISIBLE);
                doneIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.done_grey)
                );
                doneIcon.setBackgroundResource(R.drawable.circle_grey);
                line1.setBackgroundColor(Color.RED);
                pendingText.setTextColor(Color.BLACK);
                inProgressText.setTextColor(Color.BLACK);
                cancelBooking.setVisibility(View.GONE);
                break;

            case "done":
                viewBill.setVisibility(View.VISIBLE);
                line1.setBackgroundColor(Color.RED);
                line2.setBackgroundColor(Color.RED);
                cancelBooking.setVisibility(View.GONE);

                statusBtnsLayout.setVisibility(View.GONE);
                break;

            case "cancelled":
                cancelledBookingStatus.setVisibility(View.VISIBLE);
                cancelBooking.setVisibility(View.GONE);

                setButtonsEnabled(false);
                statusBtnsLayout.setVisibility(View.GONE);

                inProgressIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.in_progress_grey)
                );
                inProgressIcon.setBackgroundResource(R.drawable.circle_grey);

                doneIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.done_grey)
                );
                doneIcon.setBackgroundResource(R.drawable.circle_grey);

                pendingIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.pending_grey)
                );
                pendingIcon.setBackgroundResource(R.drawable.circle_grey);
                break;

            case "rejected":
                rejectedBookingStatus.setVisibility(View.VISIBLE);
                cancelBooking.setVisibility(View.GONE);

                setButtonsEnabled(false);
                statusBtnsLayout.setVisibility(View.GONE);

                inProgressIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.in_progress_grey)
                );
                inProgressIcon.setBackgroundResource(R.drawable.circle_grey);

                doneIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.done_grey)
                );
                doneIcon.setBackgroundResource(R.drawable.circle_grey);

                pendingIcon.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.pending_grey)
                );
                pendingIcon.setBackgroundResource(R.drawable.circle_grey);
                break;


                default:
                break;
        }

    }
    private void setUpReviewRatingsUI() {

        // Shop owners cannot leave reviews
        if (currentShop != null) {
            leaveAReviewBtn.setVisibility(View.GONE);
            return;
        }

        // Customer review logic
        if ("done".equals(theBookingStatus) && "no".equals(theIsServiceRated)) {
            leaveAReviewBtn.setVisibility(View.VISIBLE);

        } else if ("done".equals(theBookingStatus) && "yes".equals(theIsServiceRated)) {
            setAlreadyReviewedState();

        } else {
            leaveAReviewBtn.setVisibility(View.GONE);
        }
    }
    private void setupUIForShopOwner() {
        if(currentShop != null) {
            displayBookerLayout();
            customerUILayout.setVisibility(View.GONE);
        }
    }
    private void setupUIForCustomer () {
        if(currentUser != null) {
            customerUILayout.setVisibility(View.VISIBLE);
        }
    }
    private void cancelTheBooking() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {

                    FirebaseBookingManager.updateBookingStatus(
                            theBookingId,
                            "cancelled",
                            new OnStatusUpdateCallback() {
                                @Override
                                public void onSuccess(String message) {

                                    new androidx.appcompat.app.AlertDialog.Builder(CustomerMyBookingsClicked.this)
                                            .setTitle("Booking Cancelled")
                                            .setMessage("Your booking has been successfully cancelled.")
                                            .setPositiveButton("OK", (d, w) -> d.dismiss())
                                            .show();
                                    editSchedule.setVisibility(View.GONE);
                                    turnOffAllStatus();


                                }

                                @Override
                                public void onFailure(String error) {

                                    new androidx.appcompat.app.AlertDialog.Builder(CustomerMyBookingsClicked.this)
                                            .setTitle("Failed")
                                            .setMessage(error)
                                            .setPositiveButton("OK", (d, w) -> d.dismiss())
                                            .show();


                                }
                            }
                    );



                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void turnOffAllStatus () {
        cancelledBookingStatus.setVisibility(View.VISIBLE);
        cancelBooking.setVisibility(View.GONE);

        inProgressIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.in_progress_grey)
        );
        inProgressIcon.setBackgroundResource(R.drawable.circle_grey);

        doneIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.done_grey)
        );
        doneIcon.setBackgroundResource(R.drawable.circle_grey);

        pendingIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pending_grey)
        );
        pendingIcon.setBackgroundResource(R.drawable.circle_grey);

    }
    private void setAlreadyReviewedState() {
        leaveAReviewBtn.setVisibility(View.VISIBLE);
        leaveAReviewBtn.setEnabled(false);
        leaveAReviewBtn.setAlpha(0.5f);
        reviewTV.setText("Thanks for reviewing!");
    }
    private void displayBookerLayout() {
        bookerLayout.setVisibility(View.VISIBLE);

        Picasso.get().load(bookerImage).into(theBookerImage);
        theBookerName.setText(bookerName);
        theBookerNumber.setText(bookerContactNumber);
    }
    private void messageCustomer() {
        String uid = FirebaseAuthManager.getCurrentUserUid();
    }
    private void displayInProgressLayout() {
        doneIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.done_grey)
        );
        doneIcon.setBackgroundResource(R.drawable.circle_grey);
        line1.setBackgroundColor(Color.RED);
        pendingText.setTextColor(Color.BLACK);
        inProgressText.setTextColor(Color.BLACK);
        inProgressIcon.setBackgroundResource(R.drawable.circle_red);
        inProgressIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.in_progress_red)
        );
    }
    private void displayDoneProgressLayout(){
        line1.setBackgroundColor(Color.RED);
        line2.setBackgroundColor(Color.RED);
        cancelBooking.setVisibility(View.GONE);
        statusBtnsLayout.setVisibility(View.GONE);

        pendingText.setTextColor(Color.BLACK);
        inProgressText.setTextColor(Color.BLACK);
        doneText.setTextColor(Color.BLACK);
        inProgressIcon.setBackgroundResource(R.drawable.circle_red);
        inProgressIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.in_progress_red));
        doneIcon.setBackgroundResource(R.drawable.circle_red);
        doneIcon.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.done_red));

    }
    private void setupDoneAndInProgressButton() {

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        );

        String currentDate = sdf.format(new Date());

        Log.d("CURRENT_DATE", currentDate);

        if(theBookingDate.equals(currentDate)) {
            rejectBooking.setVisibility(View.VISIBLE);
            inProgressBooking.setVisibility(View.VISIBLE);
            doneBooking.setVisibility(View.GONE);
        } else {
            rejectBooking.setVisibility(View.VISIBLE);
            inProgressBooking.setVisibility(View.GONE);
            doneBooking.setVisibility(View.GONE);
        }
    }
    private void getProviderDatas() {
        FirebaseDatabaseManager.getShopOwnerData(providerID, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel data) {
                shopWillOpenAt = data.getShopOpenAt();
                shopWillCloseAt = data.getShopClosedAt();

                Picasso.get().load(data.getShopImage()).placeholder(R.drawable.sent_image_placeholder).into(serviceProviderImage);
                serviceProviderName.setText(data.getShopName());
                serviceProviderContactNum.setText(data.getContactNum());
                serviceProviderLocation.setText(data.getLocation());
                serviceProviderRatings.setText(data.getOverallRating());
            }

            @Override
            public void onFailure(String failed) {

            }
        });
    }

    private boolean isTimeWithinShopHours(String selectedTime) {

        try {

            SimpleDateFormat timeFormat =
                    new SimpleDateFormat("h:mm a", Locale.ENGLISH);

            timeFormat.setLenient(false);

            Date openingTime = timeFormat.parse(shopWillOpenAt);
            Date closingTime = timeFormat.parse(shopWillCloseAt);
            Date bookingTime = timeFormat.parse(selectedTime);

            if (openingTime == null ||
                    closingTime == null ||
                    bookingTime == null) {

                return false;
            }

            return !bookingTime.before(openingTime)
                    && !bookingTime.after(closingTime);

        } catch (ParseException e) {

            Log.e(
                    "SHOP_HOURS",
                    "Invalid shop operating hours",
                    e
            );

            return false;
        }
    }
    private void setupEditScheduleBtn() {
        if(currentLoggedInUserID.equals(providerID)) {
            editSchedule.setVisibility(View.GONE);
        } else {
            if(theBookingStatus.equals("pending")) {
                editSchedule.setVisibility(View.VISIBLE);
            }
        }
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
            uploadTheReceiptToDB.setVisibility(View.VISIBLE);
            receiptUploadedTV.setText("Receipt uploaded!");
            uploadReceiptLayout.setEnabled(false);
        }
    }
    private void uploadTheImageReceipt() {
        if(imageUri == null) {
            Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadLABEL.setText("UPLOADING...");
        uploadTheReceiptToDB.setEnabled(false);
        uploadTheReceiptToDB.setAlpha(0.5f);

        FirebaseStorageManager.uploadImage("receipts", imageUri, new FirebaseStorageManager.ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {

                FirebaseBookingManager.updateReceipt(theBookingId, imageUrl, new FirebaseBookingManager.OnUpdateImageReceipt() {
                    @Override
                    public void onSuccess() {
                        theReceiptFromDB = imageUrl;
                        uploadReceiptLayout.setVisibility(View.GONE);
                        uploadLABEL.setText("UPLOAD...");
                        uploadTheReceiptToDB.setEnabled(true);
                        uploadTheReceiptToDB.setAlpha(1f);
                        VIEWTheUploadedReceiptLayout.setVisibility(View.VISIBLE);
                        viewShopOnlinePaymentsDetailsLink.setVisibility(View.GONE);
                        Toast.makeText(CustomerMyBookingsClicked.this, "Image Receipt Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        uploadLABEL.setText("UPLOAD...");
                        uploadTheReceiptToDB.setEnabled(true);
                        uploadTheReceiptToDB.setAlpha(1f);
                        Toast.makeText(CustomerMyBookingsClicked.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                uploadLABEL.setText("UPLOAD...");
                uploadTheReceiptToDB.setEnabled(true);
                uploadTheReceiptToDB.setAlpha(1f);
                Toast.makeText(CustomerMyBookingsClicked.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void displayTheImageReceipt() {
        if("none".equals(theReceiptFromDB)) {
            VIEWTheUploadedReceiptLayout.setVisibility(View.GONE);
        } else {
            VIEWTheUploadedReceiptLayout.setVisibility(View.VISIBLE);
        }
    }
    private void displayTheUploadReceiptButton() {
        if(currentLoggedInUserID.equals(theServiceProviderID)) {
            uploadReceiptLayout.setVisibility(View.GONE);
            viewShopOnlinePaymentsDetailsLink.setVisibility(View.GONE);
        } else {
            if("Online".equals(theSelectedPaymentMethod)) {
                if(isExpired) {
                    uploadReceiptLayout.setVisibility(View.GONE);
                    return;
                }
                if("none".equals(theReceiptFromDB)) {
                    if("inProgress".equals(theBookingStatus) || "confirmed".equals(theBookingStatus)) {
                        uploadReceiptLayout.setVisibility(View.VISIBLE);
                        viewShopOnlinePaymentsDetailsLink.setVisibility(View.VISIBLE);
                    } else {
                        uploadReceiptLayout.setVisibility(View.GONE);
                        viewShopOnlinePaymentsDetailsLink.setVisibility(View.GONE);
                    }
                } else {
                    uploadReceiptLayout.setVisibility(View.GONE);
                    viewShopOnlinePaymentsDetailsLink.setVisibility(View.GONE);
                }
            } else {
                uploadReceiptLayout.setVisibility(View.GONE);
                viewShopOnlinePaymentsDetailsLink.setVisibility(View.GONE);
            }

        }
    }
    private void showOnlinePaymentDetailsDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Online Payment Details")
                .setMessage("Loading payment details...")
                .setNegativeButton("Close", null)
                .create();

        dialog.show();

        FirebaseOnlinePaymentDetailsManager.getOnlinePaymentDetails(
                providerID,
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
}