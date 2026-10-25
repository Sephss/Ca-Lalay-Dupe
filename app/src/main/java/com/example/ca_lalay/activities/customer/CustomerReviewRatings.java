package com.example.ca_lalay.activities.customer;

import android.media.Image;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.ca_lalay.firebase.callback.RatingCountCallback;
import com.example.ca_lalay.firebase.callback.ShopSummaryCallback;
import com.example.ca_lalay.model.RatingsModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomerReviewRatings extends AppCompatActivity {
    private RecyclerView ratingsRV;
    private String shopUid, theShopImage, theShopName;
    private ArrayList<RatingsModel> ratingsList;
    private ArrayList<RatingsModel> allRatingsList;
    private RatingsAdapter adapter;
    private TextView totalRatingsTV, overallRatings, shopName;
    private ImageView starOne, starTwo, starThree, starFour, starFive, shopImage;

    private TextView allTV, fiveStarTV, fourStarTV, threeStarTV, twoStarTV, oneStarTV;

    private ProgressBar fiveStarPB, fourStarPB, threeStarPB, twoStarPB, oneStarPB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_review_ratings);

        ratingsRV = findViewById(R.id.ratingsRV);
        totalRatingsTV = findViewById(R.id.totalRatingsTV);
        overallRatings = findViewById(R.id.overallRatings);

        starOne = findViewById(R.id.starOne);
        starTwo = findViewById(R.id.startwo);
        starThree = findViewById(R.id.starthree);
        starFour = findViewById(R.id.starfour);
        starFive = findViewById(R.id.starfive);

        fiveStarPB = findViewById(R.id.fiveStarPB);
        fourStarPB = findViewById(R.id.fourStarPB);
        threeStarPB = findViewById(R.id.threeStarPB);
        twoStarPB = findViewById(R.id.twoStarPB);
        oneStarPB = findViewById(R.id.oneStarPB);

        allTV = findViewById(R.id.allTV);
        fiveStarTV = findViewById(R.id.fiveStarTV);
        fourStarTV = findViewById(R.id.fourStarTV);
        threeStarTV = findViewById(R.id.threeStarTV);
        twoStarTV = findViewById(R.id.twoStarTV);
        oneStarTV = findViewById(R.id.oneStarTV);

        shopImage = findViewById(R.id.shopImage);
        shopName = findViewById(R.id.shopName);

        shopUid = getIntent().getStringExtra("shopUid");
        theShopImage = getIntent().getStringExtra("shopImage");
        theShopName = getIntent().getStringExtra("shopName");

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
        loadRatings();
        loadAllReviewsDatas();

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

        FirebaseReviewRatingsManager.getAllRatings(
                shopUid,
                new GetRatingsCallback() {

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
    private void loadAllReviewsDatas() {

        Picasso.get().load(theShopImage).placeholder(R.drawable.sent_image_placeholder).into(shopImage);
        shopName.setText(theShopName);


       FirebaseReviewRatingsManager.getShopSummary(shopUid, new ShopSummaryCallback() {
           @Override
           public void onSuccess(ShopOwnerModel shop) {
               totalRatingsTV.setText(shop.getTotalReviews() + " reviews");
               overallRatings.setText(shop.getOverallRating());
               displayOverallRating(shop.getOverallRating());
               updateRatingDistribution(shop);
           }

           @Override
           public void onFailure(String message) {

           }
       });
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
    private void updateRatingDistribution(ShopOwnerModel shop) {

        int totalReviews = Integer.parseInt(shop.getTotalReviews());

        if (totalReviews == 0) {

            fiveStarPB.setProgress(0);
            fourStarPB.setProgress(0);
            threeStarPB.setProgress(0);
            twoStarPB.setProgress(0);
            oneStarPB.setProgress(0);


            return;
        }

        int fivePercent = Math.round(
                (Integer.parseInt(shop.getFiveStarCount()) * 100f) / totalReviews);

        int fourPercent = Math.round(
                (Integer.parseInt(shop.getFourStarCount()) * 100f) / totalReviews);

        int threePercent = Math.round(
                (Integer.parseInt(shop.getThreeStarCount()) * 100f) / totalReviews);

        int twoPercent = Math.round(
                (Integer.parseInt(shop.getTwoStarCount()) * 100f) / totalReviews);

        int onePercent = Math.round(
                (Integer.parseInt(shop.getOneStarCount()) * 100f) / totalReviews);

        fiveStarPB.setProgress(fivePercent);
        fourStarPB.setProgress(fourPercent);
        threeStarPB.setProgress(threePercent);
        twoStarPB.setProgress(twoPercent);
        oneStarPB.setProgress(onePercent);


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
}