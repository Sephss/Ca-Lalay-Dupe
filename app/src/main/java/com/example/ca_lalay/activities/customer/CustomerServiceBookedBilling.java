package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;

public class CustomerServiceBookedBilling extends AppCompatActivity {
    private TextView serviceName, bookingStatusLabel,  paymentMethod, paymentStatus, serviceProviderName, vehicle, vehicleSize, dateBooked, timeBooked, baseFee, additionalFee, estimatedDuration, totalFee, additionalVehicleSize, theOverallBill;
    private String theServiceName, thePaymentMethod, thePaymentStatus, theShopID, theBookingStatus, theServiceProviderName, theVehicle, theVehicleSize, theDateBooked, theTimeBooked, theBaseFee, theAdditionalFee, theEstimatedDuration;
    private LinearLayout additionalLayout;
    private Button doneBtn;
    private ImageView backBtn;
    private TextView customerName;
    private String theCustomerName;

    // Billing number
    private TextView billingNumber;
    private String theBillingNumber;
    private LinearLayout billingNumberLayout;

    // BOOKING MODIFICATION LAYOUT
    private String theServiceAdded, theServiceAddedTotalPrice, theServiceAddedRemarks;
    private LinearLayout additionalServiceLayout, serviceAddedFeeLayout;
    private TextView addedRemarksTV, serviceAddedTV, serviceAddedTotalFee;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_service_booked_billing);

        serviceName = findViewById(R.id.serviceName);
        serviceProviderName = findViewById(R.id.serviceProviderName);
        vehicle = findViewById(R.id.vehicleName);
        vehicleSize = findViewById(R.id.vehicleSize);
        dateBooked = findViewById(R.id.dateBooked);
        timeBooked = findViewById(R.id.timeBooked);
        backBtn = findViewById(R.id.backBtn);

        // billing number
        billingNumber = findViewById(R.id.billingNumber);
        billingNumberLayout = findViewById(R.id.billingNumberLayout);

        baseFee = findViewById(R.id.baseFee);
        additionalFee = findViewById(R.id.additionalFee);
        estimatedDuration = findViewById(R.id.estimatedDuration);
        totalFee = findViewById(R.id.totalFee);

        theOverallBill = findViewById(R.id.theOverallBill);
        paymentMethod = findViewById(R.id.paymentMethod);
        paymentStatus = findViewById(R.id.paymentStatus);

        doneBtn = findViewById(R.id.doneBtn);
        bookingStatusLabel = findViewById(R.id.bookingStatusLabel);

        customerName = findViewById(R.id.customerName);

        additionalLayout = findViewById(R.id.additionalLayout);
        additionalVehicleSize = findViewById(R.id.additionalVehicleSize);

        // booking modifications initializations
        additionalServiceLayout = findViewById(R.id.additionalServiceLayout);
        addedRemarksTV = findViewById(R.id.addedRemarksTV);
        serviceAddedTV = findViewById(R.id.serviceAddedTV);
        serviceAddedFeeLayout = findViewById(R.id.serviceAddedFeeLayout);
        serviceAddedTotalFee = findViewById(R.id.serviceAddedTotalFee);

        Intent intent = getIntent();
        theServiceName = intent.getStringExtra("serviceName");
        theServiceProviderName = intent.getStringExtra("serviceProvider");
        theVehicle = intent.getStringExtra("vehicle");
        theVehicleSize = intent.getStringExtra("vehicleSize");
        theDateBooked = intent.getStringExtra("date");
        theTimeBooked = intent.getStringExtra("time");
        theBaseFee = intent.getStringExtra("baseFee");
        theAdditionalFee = intent.getStringExtra("additionalFee");
        theEstimatedDuration = intent.getStringExtra("serviceDuration");
        thePaymentMethod = intent.getStringExtra("paymentMethod");
        thePaymentStatus = intent.getStringExtra("paymentStatus");
        theBookingStatus = intent.getStringExtra("bookingStatus");
        theShopID = intent.getStringExtra("theShopID");
        theCustomerName = intent.getStringExtra("customerName");

        //get billing number
        theBillingNumber = intent.getStringExtra("billingNumber");

        // get the booking modifications details
        theServiceAdded = intent.getStringExtra("serviceAdded");
        theServiceAddedTotalPrice = intent.getStringExtra("serviceAddedTotalPrice");
        theServiceAddedRemarks = intent.getStringExtra("serviceAddedRemarks");


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupDetails();
        displayBookingModificationsDetails();

        doneBtn.setOnClickListener(s-> {
            finish();
        });
        backBtn.setOnClickListener(s -> {
            finish();
        });
    }
    private void setupDetails() {
        serviceName.setText(theServiceName);
        serviceProviderName.setText(theServiceProviderName);
        vehicle.setText(theVehicle);
        vehicleSize.setText(theVehicleSize);
        dateBooked.setText(theDateBooked);
        timeBooked.setText(theTimeBooked);
        customerName.setText(theCustomerName);

        baseFee.setText(theBaseFee);
        estimatedDuration.setText(theEstimatedDuration);

        if("none".equals(theBillingNumber)) {
            billingNumberLayout.setVisibility(View.GONE);
        } else {
            billingNumberLayout.setVisibility(View.VISIBLE);
            billingNumber.setText(theBillingNumber);
        }

        if("no".equals(thePaymentStatus)) {
            paymentStatus.setText("Unpaid");
        } else {
            paymentStatus.setText("Paid");
        }

        if(theBookingStatus.equals("pending")) {
            bookingStatusLabel.setText("Booking pending");
        } else if (theBookingStatus.equals("confirmed")) {
            bookingStatusLabel.setText("Booking confirmed");
        } else if (theBookingStatus.equals("inProgress")) {
            bookingStatusLabel.setText("Booking in progress");
        } else if (theBookingStatus.equals("done")) {
            bookingStatusLabel.setText("Booking done");
        }

        if(thePaymentMethod.equals("Cash")) {
            paymentMethod.setText("Cash – Pay at the Shop");
        } else {
            paymentMethod.setText("Online – Pay Directly to the Shop");
        }

        if(theAdditionalFee.isEmpty() || theAdditionalFee.equals("") || theAdditionalFee == null) {
            additionalLayout.setVisibility(View.GONE);

            double BASE_FEE = Double.parseDouble(theBaseFee);
            double THE_SERVICEADDED_TOTAL_PRICE = Double.parseDouble(theServiceAddedTotalPrice);
            double THE_TOTAL = BASE_FEE + THE_SERVICEADDED_TOTAL_PRICE;

            totalFee.setText("₱" + THE_TOTAL);
            theOverallBill.setText("₱" + THE_TOTAL);
        } else {
            if(theVehicleSize.equals("Large")) {
                additionalLayout.setVisibility(View.VISIBLE);
                additionalFee.setText(theAdditionalFee);
                double baseFee = Double.parseDouble(theBaseFee);
                double additionalFee = Double.parseDouble(theAdditionalFee);
                double percentage = (additionalFee / baseFee) * 100;

                double theTotal = baseFee + additionalFee;
                double THE_SERVICEADDED_TOTAL_PRICE = Double.parseDouble(theServiceAddedTotalPrice);
                double THE_TOTAL = theTotal + THE_SERVICEADDED_TOTAL_PRICE;

                totalFee.setText("₱" + THE_TOTAL);
                theOverallBill.setText("₱" + THE_TOTAL);

                additionalVehicleSize.setText("Large vehicle " + "(+" + percentage + "%)");
            } else if (theVehicleSize.equals("Medium")) {
                additionalLayout.setVisibility(View.VISIBLE);
                additionalFee.setText(theAdditionalFee);
                double baseFee = Double.parseDouble(theBaseFee);
                double additionalFee = Double.parseDouble(theAdditionalFee);
                double percentage = (additionalFee / baseFee) * 100;

                double theTotal = baseFee + additionalFee;
                double THE_SERVICEADDED_TOTAL_PRICE = Double.parseDouble(theServiceAddedTotalPrice);
                double THE_TOTAL = theTotal + THE_SERVICEADDED_TOTAL_PRICE;

                totalFee.setText("₱" + THE_TOTAL);
                theOverallBill.setText("₱" + THE_TOTAL);

                additionalVehicleSize.setText("Medium vehicle " + "(+" + percentage + "%)");
            }
            else if (theVehicleSize.equals("Small")) {
                additionalLayout.setVisibility(View.GONE);

                double baseFee = Double.parseDouble(theBaseFee);
                double THE_SERVICEADDED_TOTAL_PRICE = Double.parseDouble(theServiceAddedTotalPrice);
                double THE_TOTAL = baseFee + THE_SERVICEADDED_TOTAL_PRICE;


                totalFee.setText("₱" + THE_TOTAL);
                theOverallBill.setText("₱" + THE_TOTAL);

            }else {
                additionalLayout.setVisibility(View.GONE);

            }
        }
    }
    private void displayBookingModificationsDetails() {
        if("none".equals(theServiceAdded) || "none".equals(theServiceAddedRemarks)) {
            additionalServiceLayout.setVisibility(View.GONE);
            serviceAddedFeeLayout.setVisibility(View.GONE);
        } else {
            serviceAddedTV.setText(theServiceAdded);
            addedRemarksTV.setText(theServiceAddedRemarks);
            serviceAddedTotalFee.setText(theServiceAddedTotalPrice);
            additionalServiceLayout.setVisibility(View.VISIBLE);
            serviceAddedFeeLayout.setVisibility(View.VISIBLE);
        }

    }
}