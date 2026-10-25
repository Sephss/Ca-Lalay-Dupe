package com.example.ca_lalay.activities.customer;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.CustomerBookingAdapter;
import com.example.ca_lalay.firebase.FirebaseBookingManager;
import com.example.ca_lalay.firebase.callback.CustomerBookingListCallback;
import com.example.ca_lalay.model.CustomerBookingModel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerMyBookings extends AppCompatActivity {
    private ImageView backBtn;
    private RecyclerView bookingsRV;
    private List<CustomerBookingModel> bookingList;
    private CustomerBookingAdapter adapter;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_my_bookings);

        backBtn = findViewById(R.id.backBtn);
        bookingsRV = findViewById(R.id.bookingsRV);
        bookingList = new ArrayList<>();
        adapter = new CustomerBookingAdapter(this, bookingList);

        bookingsRV.setLayoutManager(new LinearLayoutManager(this));
        bookingsRV.setAdapter(adapter);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backBtn.setOnClickListener(s -> {
            finish();
        });
        fetchBookings();
    }
    private void fetchBookings() {

        String currentUserId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        FirebaseBookingManager.getBookingsByUser(
                currentUserId,
                new CustomerBookingListCallback() {

                    @Override
                    public void onSuccess(List<CustomerBookingModel> list) {

                        // Sort by timestamp
                        // Newest booking appears FIRST
                        Collections.sort(
                                list,
                                new Comparator<CustomerBookingModel>() {
                                    @Override
                                    public int compare(
                                            CustomerBookingModel booking1,
                                            CustomerBookingModel booking2
                                    ) {

                                        try {

                                            String timestamp1 =
                                                    booking1.getTimestamp();

                                            String timestamp2 =
                                                    booking2.getTimestamp();

                                            // Handle null timestamps
                                            if (timestamp1 == null && timestamp2 == null) {
                                                return 0;
                                            }

                                            if (timestamp1 == null) {
                                                return 1;
                                            }

                                            if (timestamp2 == null) {
                                                return -1;
                                            }

                                            long time1 =
                                                    Long.parseLong(timestamp1);

                                            long time2 =
                                                    Long.parseLong(timestamp2);

                                            // Newest first
                                            return Long.compare(time2, time1);

                                        } catch (Exception e) {
                                            return 0;
                                        }
                                    }
                                }
                        );

                        bookingList.clear();
                        bookingList.addAll(list);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(String errorMessage) {

                    }
                }
        );
    }
}