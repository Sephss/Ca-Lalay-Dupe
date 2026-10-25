package com.example.ca_lalay.activities.shopowner;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.CustomerBookingAdapter;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseBookingManager;
import com.example.ca_lalay.firebase.callback.CustomerBookingListCallback;
import com.example.ca_lalay.model.CustomerBookingModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShopOwnerViewBookings extends AppCompatActivity {
    private RecyclerView bookingsRV;
    private String uid;

    // Full sorted list fetched from Firebase (unfiltered, always sorted earliest first)
    private List<CustomerBookingModel> allBookingsList;

    // List actually bound to the adapter (result of filter + search on allBookingsList)
    private List<CustomerBookingModel> theBookingList;

    private ImageView backBtn, filterImage;
    private CustomerBookingAdapter adapter;
    private EditText searchEditText;



    private TextView selectedFilterText;
    private LinearLayout selectedFilterLayout;

    // "all", "pending", "inProgress", "done", "cancelled" -- matches DB values
    private String currentStatusFilter = "pending";
    private String currentSearchQuery = "";

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_view_bookings);

        selectedFilterLayout = findViewById(R.id.selectedFilterLayout);
        selectedFilterText = findViewById(R.id.selectedFilterText);
        backBtn = findViewById(R.id.backBtn);

        bookingsRV = findViewById(R.id.bookingsRV);
        allBookingsList = new ArrayList<>();
        theBookingList = new ArrayList<>();
        adapter = new CustomerBookingAdapter(this, theBookingList);

        bookingsRV.setLayoutManager(new LinearLayoutManager(this));
        bookingsRV.setAdapter(adapter);

        filterImage = findViewById(R.id.filterImage);
        searchEditText = findViewById(R.id.searchEditText);

        uid = FirebaseAuthManager.getCurrentUserUid();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupFilter();
        setupSearch();
        fetchBookings();

        backBtn.setOnClickListener(s -> finish());
    }

    private void fetchBookings() {

        FirebaseBookingManager.getBookingsByProvider(
                uid,
                new CustomerBookingListCallback() {

                    @Override
                    public void onSuccess(List<CustomerBookingModel> bookingList) {

                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "yyyy-MM-dd hh:mm a",
                                Locale.getDefault()
                        );

                        // Earliest service date/time first
                        Collections.sort(bookingList, new Comparator<CustomerBookingModel>() {
                            @Override
                            public int compare(
                                    CustomerBookingModel booking1,
                                    CustomerBookingModel booking2
                            ) {

                                try {

                                    String dateTime1 =
                                            booking1.getServiceDate() + " " +
                                                    booking1.getServiceTime();

                                    String dateTime2 =
                                            booking2.getServiceDate() + " " +
                                                    booking2.getServiceTime();

                                    Date bookingDate1 = sdf.parse(dateTime1);
                                    Date bookingDate2 = sdf.parse(dateTime2);

                                    return bookingDate1.compareTo(bookingDate2);

                                } catch (Exception e) {
                                    return 0;
                                }
                            }
                        });

                        allBookingsList.clear();
                        allBookingsList.addAll(bookingList);

                        // Default view: pending only, sorted (currentStatusFilter starts as "pending")
                        applyFilterAndSearch();
                    }

                    @Override
                    public void onFailure(String errorMessage) {

                    }
                }
        );
    }

    private void setupFilter() {
        filterImage.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, filterImage);
            popupMenu.getMenu().add(0, 0, 0, "All");
            popupMenu.getMenu().add(0, 1, 1, "Confirmed");
            popupMenu.getMenu().add(0, 2, 2, "Rejected");
            popupMenu.getMenu().add(0, 3, 3, "Pending");
            popupMenu.getMenu().add(0, 4, 4, "In Progress");
            popupMenu.getMenu().add(0, 5, 5, "Done");
            popupMenu.getMenu().add(0, 6, 6, "Cancelled");
            popupMenu.getMenu().add(0, 7, 7, "Expired");

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == 0) {
                    currentStatusFilter = "all";
                    selectedFilterText.setText("All Bookings");
                } else if (id == 1) {
                    currentStatusFilter = "confirmed";
                    selectedFilterText.setText("Confirmed Bookings");
                } else if (id == 2) {
                    currentStatusFilter = "rejected";
                    selectedFilterText.setText("Rejected Bookings");
                } else if (id == 3) {
                    currentStatusFilter = "pending";
                    selectedFilterText.setText("Pending Bookings");
                } else if (id == 4) {
                    currentStatusFilter = "inProgress";
                    selectedFilterText.setText("In-Progress Bookings");
                } else if (id == 5) {
                    currentStatusFilter = "done";
                    selectedFilterText.setText("Done Bookings");
                } else if (id == 6) {
                    currentStatusFilter = "cancelled";
                    selectedFilterText.setText("Cancelled Bookings");
                } else if (id == 7) {
                    currentStatusFilter = "expired";
                    selectedFilterText.setText("Expired Bookings");
                }

                applyFilterAndSearch();
                return true;
            });

            popupMenu.show();
        });
    }
    private boolean isBookingDatePast(String serviceDate) {

        if (serviceDate == null || serviceDate.trim().isEmpty()) {
            return false;
        }

        try {

            SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            );

            sdf.setLenient(false);

            Date bookingDate = sdf.parse(serviceDate);

            Date today = sdf.parse(
                    sdf.format(new Date())
            );

            return bookingDate != null && bookingDate.before(today);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Re-applies status filter + name search on top of allBookingsList (already sorted
    // earliest-first) and pushes the result into the adapter's backing list.
    private void applyFilterAndSearch() {

        List<CustomerBookingModel> result = new ArrayList<>();

        for (CustomerBookingModel booking : allBookingsList) {

            String status = booking.getBookingStatus() == null
                    ? ""
                    : booking.getBookingStatus();

            // Check if the booking date is already past
            boolean isPastBooking = isBookingDatePast(
                    booking.getServiceDate()
            );

            // Only pending, confirmed, and inProgress
            // can be considered expired
            boolean isExpired =
                    isPastBooking
                            && ("pending".equalsIgnoreCase(status)
                            || "confirmed".equalsIgnoreCase(status)
                            || "inProgress".equalsIgnoreCase(status));

            // =====================================================
            // STATUS FILTER
            // =====================================================

            boolean matchesStatus;

            if (currentStatusFilter.equals("all")) {

                // Show everything
                matchesStatus = true;

            } else if (currentStatusFilter.equals("expired")) {

                // Show only bookings that are actually expired
                matchesStatus = isExpired;

            } else if (currentStatusFilter.equals("pending")) {

                // Show pending bookings only if they are NOT expired
                matchesStatus =
                        "pending".equalsIgnoreCase(status)
                                && !isExpired;

            } else if (currentStatusFilter.equals("confirmed")) {

                // Show confirmed bookings only if they are NOT expired
                matchesStatus =
                        "confirmed".equalsIgnoreCase(status)
                                && !isExpired;

            } else if (currentStatusFilter.equals("inProgress")) {

                // Show in-progress bookings only if they are NOT expired
                matchesStatus =
                        "inProgress".equalsIgnoreCase(status)
                                && !isExpired;

            } else {

                // Done, Rejected, Cancelled
                matchesStatus =
                        status.equalsIgnoreCase(
                                currentStatusFilter
                        );
            }

            // If it doesn't match the selected filter,
            // skip this booking.
            if (!matchesStatus) {
                continue;
            }

            // =====================================================
            // SEARCH
            // =====================================================

            boolean matchesSearch = true;

            if (!currentSearchQuery.isEmpty()) {

                String query =
                        currentSearchQuery.toLowerCase(
                                Locale.getDefault()
                        );

                // Customer / booker name
                String bookerName =
                        booking.getBookerName() == null
                                ? ""
                                : booking.getBookerName();

                // Service name/type
                String serviceName =
                        booking.getServiceType() == null
                                ? ""
                                : booking.getServiceType();

                // Customer vehicle
                String vehicle =
                        booking.getBookerVehicle() == null
                                ? ""
                                : booking.getBookerVehicle();

                // Service date
                String serviceDate =
                        booking.getServiceDate() == null
                                ? ""
                                : booking.getServiceDate();

                // Service time
                String serviceTime =
                        booking.getServiceTime() == null
                                ? ""
                                : booking.getServiceTime();

                // Billing number
                String billingNumber =
                        booking.getBillingNumber() == null
                                ? ""
                                : booking.getBillingNumber();


                // Booking ID
                String bookingId =
                        booking.getBookingId() == null
                                ? ""
                                : booking.getBookingId();

                // Display status
                // If the booking is expired, search "Expired"
                // instead of its actual Firebase status.
                String displayStatus;

                if (isExpired) {
                    displayStatus = "expired";
                } else {
                    displayStatus = status;
                }

                // Search any of the supported fields
                matchesSearch =
                        bookerName
                                .toLowerCase(Locale.getDefault())
                                .contains(query)

                                || serviceName
                                .toLowerCase(Locale.getDefault())
                                .contains(query)

                                || vehicle
                                .toLowerCase(Locale.getDefault())
                                .contains(query)

                                || serviceDate
                                .toLowerCase(Locale.getDefault())
                                .contains(query)

                                || serviceTime
                                .toLowerCase(Locale.getDefault())
                                .contains(query)

                                || bookingId
                                .toLowerCase(Locale.getDefault())
                                .contains(query)

                                || billingNumber
                                .toLowerCase(Locale.getDefault())
                                .contains(query)

                                || displayStatus
                                .toLowerCase(Locale.getDefault())
                                .contains(query);
            }

            // =====================================================
            // ADD TO RESULT
            // =====================================================

            if (matchesSearch) {
                result.add(booking);
            }
        }

        // Replace adapter data
        theBookingList.clear();
        theBookingList.addAll(result);

        // Refresh RecyclerView
        adapter.notifyDataSetChanged();
    }
}