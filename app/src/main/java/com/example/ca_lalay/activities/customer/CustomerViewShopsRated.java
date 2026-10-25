package com.example.ca_lalay.activities.customer;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.CustomerRatingsAdapter;
import com.example.ca_lalay.activities.adapter.RatingsAdapter;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseCustomerRatingsManager;
import com.example.ca_lalay.firebase.callback.GetRatingsCallback;
import com.example.ca_lalay.model.RatingsModel;

import java.util.ArrayList;

public class CustomerViewShopsRated extends AppCompatActivity {
    private RecyclerView shopsRatedRV;
    private ArrayList<RatingsModel> ratingsList;
    private ArrayList<RatingsModel> allRatingsList;
    private CustomerRatingsAdapter adapter;
    private String uid;
    private ImageView backBtn;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_view_shops_rated);

        uid = FirebaseAuthManager.getCurrentUserUid();

        backBtn = findViewById(R.id.backBtn);
        shopsRatedRV = findViewById(R.id.shopsRatedRV);
        ratingsList = new ArrayList<>();
        allRatingsList = new ArrayList<>();

        adapter = new CustomerRatingsAdapter(this, ratingsList);

        shopsRatedRV.setLayoutManager(new LinearLayoutManager(this));

        shopsRatedRV.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loadAllMyRatings();

        backBtn.setOnClickListener(s -> {
            finish();
        });
    }
    private void loadAllMyRatings() {
        FirebaseCustomerRatingsManager.getAllMyRatings(uid, new GetRatingsCallback() {
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
                Toast.makeText(CustomerViewShopsRated.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}