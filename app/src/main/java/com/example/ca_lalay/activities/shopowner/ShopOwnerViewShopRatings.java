package com.example.ca_lalay.activities.shopowner;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.activities.adapter.RatingsAdapter;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseReviewRatingsManager;
import com.example.ca_lalay.firebase.callback.GetRatingsCallback;
import com.example.ca_lalay.model.RatingsModel;
import com.example.ca_lalay.model.ShopOwnerModel;

import java.util.ArrayList;

public class ShopOwnerViewShopRatings extends AppCompatActivity {
    private TextView shopName, overallRatings, totalReviewsCount;
    private String shopUid;
    private ImageView backBtn;
    private ShopOwnerModel currentShop;
    private RecyclerView ratingsRV;
    private ArrayList<RatingsModel> allRatingsList;
    private RatingsAdapter adapter;
    private ArrayList<RatingsModel> ratingsList;
    private ImageView starOne, starTwo, starThree, starFour, starFive;
    private TextView allTV, fiveStarTV, fourStarTV, threeStarTV, twoStarTV, oneStarTV;
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_view_shop_ratings);

        starOne = findViewById(R.id.starOne);
        starTwo = findViewById(R.id.startwo);
        starThree = findViewById(R.id.starthree);
        starFour = findViewById(R.id.starfour);
        starFive = findViewById(R.id.starfive);

        backBtn = findViewById(R.id.backBtn);

        allTV = findViewById(R.id.allTV);
        fiveStarTV = findViewById(R.id.fiveStarTV);
        fourStarTV = findViewById(R.id.fourStarTV);
        threeStarTV = findViewById(R.id.threeStarTV);
        twoStarTV = findViewById(R.id.twoStarTV);
        oneStarTV = findViewById(R.id.oneStarTV);

        shopUid = FirebaseAuthManager.getCurrentUserUid();
        currentShop = ShopOwnerSession.getInstance().getCurrentShopOwner();

        shopName = findViewById(R.id.shopName);
        overallRatings = findViewById(R.id.overallRatings);
        totalReviewsCount = findViewById(R.id.totalReviewsCount);

        ratingsRV = findViewById(R.id.ratingsRV);
        ratingsList = new ArrayList<>();
        allRatingsList = new ArrayList<>();

        adapter = new RatingsAdapter(this, ratingsList);

        ratingsRV.setLayoutManager(new LinearLayoutManager(this));

        ratingsRV.setAdapter(adapter);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupData();
        loadRatings();

        backBtn.setOnClickListener(s -> {
            finish();
        });

        allTV.setOnClickListener(v -> {
            selectFilter(allTV);
            filterRatings(0);
        });

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
    }
    private void loadRatings() {
        FirebaseReviewRatingsManager.getAllRatings(shopUid, new GetRatingsCallback() {
            @Override
            public void onSuccess(ArrayList<RatingsModel> ratings) {
                allRatingsList.clear();
                allRatingsList.addAll(ratings);

                ratingsList.clear();
                ratingsList.addAll(ratings);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String message) {

            }
        });

    }
    private void setupData() {
        if (currentShop == null) {
            Toast.makeText(this, "Unable to load shop details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        shopName.setText(currentShop.getShopName() + " - Overall Ratings");
        overallRatings.setText(currentShop.getOverallRating());
        totalReviewsCount.setText(currentShop.getTotalReviews());

        displayOverallRating(currentShop.getOverallRating());
    }
    private void displayOverallRating(String overallRating) {

        float rating = Float.parseFloat(overallRating);

        int stars = Math.round(rating);

        ImageView[] starViews = {
                starOne,
                starTwo,
                starThree,
                starFour,
                starFive
        };

        for (int i = 0; i < starViews.length; i++) {

            if (i < stars) {
                starViews[i].setAlpha(1f);
            } else {
                starViews[i].setAlpha(0.2f);
            }

        }

    }
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
            tv.setBackgroundResource(R.drawable.rating_filter_bg);
            tv.setTextColor(ContextCompat.getColor(this, R.color.semiblack));
        }

        selectedTV.setBackgroundResource(R.drawable.rating_filter_bg_selected);
        selectedTV.setTextColor(ContextCompat.getColor(this, R.color.white));
    }
    private void filterRatings(int star) {

        ratingsList.clear();

        if (star == 0) {

            ratingsList.addAll(allRatingsList);

        } else {

            for (RatingsModel rating : allRatingsList) {

                int currentStar = (int) Float.parseFloat(rating.getRatingStar());

                if (currentStar == star) {
                    ratingsList.add(rating);
                }

            }

        }

        adapter.notifyDataSetChanged();
    }
}