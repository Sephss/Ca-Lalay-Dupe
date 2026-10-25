package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.RatingsAdapter;
import com.example.ca_lalay.firebase.FirebaseReviewRatingsManager;
import com.example.ca_lalay.firebase.callback.GetRatingsCallback;
import com.example.ca_lalay.model.RatingsModel;

import java.util.ArrayList;

public class CustomerViewServiceRatings extends AppCompatActivity {

    private ImageView backBtn;

    private TextView allTV,
            fiveStarTV,
            fourStarTV,
            threeStarTV,
            twoStarTV,
            oneStarTV;

    private TextView theServiceName;

    private RecyclerView ratingsRecyclerView;

    private RatingsAdapter adapter;

    // Contains ALL ratings fetched from Firebase for the shop
    private final ArrayList<RatingsModel> allRatingsList =
            new ArrayList<>();

    // Contains ratings currently displayed
    private final ArrayList<RatingsModel> ratingsList =
            new ArrayList<>();

    private String serviceProviderID;
    private String serviceID;
    private String serviceName;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_view_service_ratings);

        backBtn = findViewById(R.id.backBtn);

        allTV = findViewById(R.id.allTV);
        fiveStarTV = findViewById(R.id.fiveStarTV);
        fourStarTV = findViewById(R.id.fourStarTV);
        threeStarTV = findViewById(R.id.threeStarTV);
        twoStarTV = findViewById(R.id.twoStarTV);
        oneStarTV = findViewById(R.id.oneStarTV);

        theServiceName = findViewById(R.id.theServiceName);

        ratingsRecyclerView = findViewById(R.id.ratingsRecyclerView);

        // ---------------------------------------------------------
        // GET DATA FROM PREVIOUS ACTIVITY
        // ---------------------------------------------------------

        Intent intent = getIntent();

        serviceProviderID = intent.getStringExtra("shopID");
        serviceID = intent.getStringExtra("serviceID");
        serviceName = intent.getStringExtra("serviceName");

        // ---------------------------------------------------------
        // SYSTEM BAR INSETS
        // ---------------------------------------------------------

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // ---------------------------------------------------------
        // RECYCLER VIEW
        // ---------------------------------------------------------

        ratingsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new RatingsAdapter(
                this,
                ratingsList
        );

        ratingsRecyclerView.setAdapter(adapter);

        // ---------------------------------------------------------
        // DEFAULT FILTER
        // ---------------------------------------------------------

        selectFilter(allTV);

        // ---------------------------------------------------------
        // FILTER BUTTONS
        // ---------------------------------------------------------

        allTV.setOnClickListener(v -> {

            selectFilter(allTV);

            filterRatings(0);
        });

        theServiceName.setText(serviceName + " ratings");

        fiveStarTV.setOnClickListener(v -> {

            selectFilter(fiveStarTV);

            filterRatings(5);
        });

        fourStarTV.setOnClickListener(v -> {

            selectFilter(fourStarTV);

            filterRatings(4);
        });

        threeStarTV.setOnClickListener(v -> {

            selectFilter(threeStarTV);

            filterRatings(3);
        });

        twoStarTV.setOnClickListener(v -> {

            selectFilter(twoStarTV);

            filterRatings(2);
        });

        oneStarTV.setOnClickListener(v -> {

            selectFilter(oneStarTV);

            filterRatings(1);
        });

        // ---------------------------------------------------------
        // BACK
        // ---------------------------------------------------------

        backBtn.setOnClickListener(v -> finish());

        // ---------------------------------------------------------
        // LOAD RATINGS
        // ---------------------------------------------------------

        loadServiceRatings();
    }

    /**
     * Fetch all ratings belonging to the shop,
     * then keep only ratings belonging to this service.
     */
    private void loadServiceRatings() {

        if (serviceProviderID == null || serviceID == null) {
            return;
        }

        FirebaseReviewRatingsManager.getAllRatings(
                serviceProviderID,
                new GetRatingsCallback() {

                    @Override
                    public void onSuccess(
                            ArrayList<RatingsModel> ratings) {

                        // Store all shop ratings
                        allRatingsList.clear();
                        allRatingsList.addAll(ratings);

                        // Keep only ratings for this service
                        ratingsList.clear();

                        for (RatingsModel rating : allRatingsList) {

                            if (rating.getServiceID() != null
                                    && rating.getServiceID()
                                    .equals(serviceID)) {

                                ratingsList.add(rating);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(String message) {

                        // Optional: show error message
                    }
                }
        );
    }

    /**
     * Filters the already-loaded service ratings.
     *
     * 0 = All
     * 1 = 1 star
     * 2 = 2 stars
     * 3 = 3 stars
     * 4 = 4 stars
     * 5 = 5 stars
     */
    private void filterRatings(int starFilter) {

        ratingsList.clear();

        // ALL
        if (starFilter == 0) {

            for (RatingsModel rating : allRatingsList) {

                if (rating.getServiceID() != null
                        && rating.getServiceID()
                        .equals(serviceID)) {

                    ratingsList.add(rating);
                }
            }

        }

        // SPECIFIC STAR
        else {

            for (RatingsModel rating : allRatingsList) {

                if (rating.getServiceID() == null
                        || !rating.getServiceID()
                        .equals(serviceID)) {

                    continue;
                }

                try {

                    float ratingValue =
                            Float.parseFloat(
                                    rating.getRatingStar()
                            );

                    if ((int) ratingValue == starFilter) {
                        ratingsList.add(rating);
                    }

                } catch (NumberFormatException e) {

                    // Ignore invalid rating value
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Changes the appearance of the selected filter.
     */
    private void selectFilter(TextView selectedTV) {

        TextView[] filters = {
                allTV,
                fiveStarTV,
                fourStarTV,
                threeStarTV,
                twoStarTV,
                oneStarTV
        };

        for (TextView tv : filters) {

            tv.setBackgroundResource(
                    R.drawable.rating_filter_bg
            );

            tv.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.semiblack
                    )
            );
        }

        selectedTV.setBackgroundResource(
                R.drawable.rating_filter_bg_selected
        );

        selectedTV.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.white
                )
        );
    }
}