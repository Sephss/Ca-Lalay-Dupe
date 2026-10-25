package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseCustomerRatingsManager;
import com.example.ca_lalay.firebase.FirebaseReviewRatingsManager;
import com.example.ca_lalay.firebase.callback.RatingsCallback;
import com.example.ca_lalay.model.RatingsModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CustomerSubmitRatings extends AppCompatActivity {
    private ImageView shopImage;
    private TextView shopName, serviceName, ratingBarTV;
    private RatingBar ratingBar;
    private EditText etComment;
    private String shopImageURL, shopNameText, serviceNameText;
    private String bookerName, bookerImage, bookerID, bookingID, serviceID, theServiceProviderID;
    private Button submitReviewBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_submit_ratings);

        shopImage = findViewById(R.id.shopImage);
        shopName = findViewById(R.id.shopName);
        serviceName = findViewById(R.id.serviceName);
        ratingBar = findViewById(R.id.ratingBar);
        ratingBarTV = findViewById(R.id.ratingBarTV);
        etComment = findViewById(R.id.etComment);

        submitReviewBtn = findViewById(R.id.submitReviewBtn);

        Intent data = getIntent();
        shopImageURL = data.getStringExtra("shopImage");
        shopNameText = data.getStringExtra("shopName");
        serviceNameText = data.getStringExtra("serviceName");

        bookerName = data.getStringExtra("bookerName");
        bookerImage = data.getStringExtra("bookerImage");
        bookerID = FirebaseAuthManager.getCurrentUserUid();
        bookingID = data.getStringExtra("bookingID");

        serviceID = data.getStringExtra("theServiceID");
        theServiceProviderID = data.getStringExtra("theServiceProviderID");



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
        setUpDetails();
        dynamicRatingBar();

        submitReviewBtn.setOnClickListener(f -> {
            createReview();
        });
    }

    private void setUpDetails() {
        Picasso.get().load(shopImageURL).placeholder(R.drawable.sent_image_placeholder).into(shopImage);
        shopName.setText(shopNameText);
        serviceName.setText(serviceNameText);
    }
    private void createReview() {

        float star = ratingBar.getRating();
        String comment = etComment.getText().toString();

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MMMM dd, yyyy",
                Locale.ENGLISH
        );

        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        String currentDate = dateFormat.format(new Date());

        long timestamp = System.currentTimeMillis();

       if(star == 0) {
           Toast.makeText(this, "Please select a star rating.", Toast.LENGTH_SHORT).show();
           return;
       }

       String ratingStar = String.valueOf(star);
       String theTimestamp = String.valueOf(timestamp);

        RatingsModel data = new RatingsModel(bookerName, bookerImage, bookerID, ratingStar, comment, currentDate, "", serviceID, serviceNameText, theServiceProviderID, bookingID, theTimestamp, shopNameText, shopImageURL);

        setLoadingState();

        FirebaseReviewRatingsManager.saveRatingsReview(data, new RatingsCallback() {
            @Override
            public void onSuccess(String success) {
                setNormalState();

                FirebaseCustomerRatingsManager.saveRatingsReview(data, new RatingsCallback() {
                    @Override
                    public void onSuccess(String success) {
                        Toast.makeText(CustomerSubmitRatings.this, "Ratings submitted successfully!", Toast.LENGTH_SHORT).show();
                        setNormalState();
                        finish();

                    }

                    @Override
                    public void onFailure(String failed) {
                        Toast.makeText(CustomerSubmitRatings.this, failed, Toast.LENGTH_SHORT).show();
                        setNormalState();
                    }
                });
            }

            @Override
            public void onFailure(String failed) {
                Toast.makeText(CustomerSubmitRatings.this, failed, Toast.LENGTH_SHORT).show();
                setNormalState();
            }
        });
    }
    private void dynamicRatingBar() {

        ratingBarTV.setText("Tap a star to rate your experience.");

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {

            switch ((int) rating) {

                case 1:
                    ratingBarTV.setText("Poor – The service did not meet my expectations.");
                    break;

                case 2:
                    ratingBarTV.setText("Fair – The service was acceptable but needs significant improvement.");
                    break;

                case 3:
                    ratingBarTV.setText("Good – The service met my expectations.");
                    break;

                case 4:
                    ratingBarTV.setText("Very Good – I was satisfied with the quality of the service.");
                    break;

                case 5:
                    ratingBarTV.setText("Excellent – Outstanding service! I would highly recommend this shop.");
                    break;

                default:
                    ratingBarTV.setText("Tap a star to rate your experience.");
                    break;
            }

        });

    }
    private void setLoadingState() {
        submitReviewBtn.setEnabled(false);
        submitReviewBtn.setAlpha(0.5f);
        submitReviewBtn.setText("SUBMITTING...");
    }

    private void setNormalState() {
        submitReviewBtn.setEnabled(true);
        submitReviewBtn.setAlpha(1f);
        submitReviewBtn.setText("SUBMIT REVIEW");
    }
}