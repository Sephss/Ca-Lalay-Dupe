package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.shopowner.ShopOwnerEditService;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.squareup.picasso.Picasso;

public class CustomerServicesClicked extends AppCompatActivity {
    private ImageView backBtn, theServiceImg, theShopOwnerImg;
    private Button bookBtn, editServiceBtn, viewRatingsBtn;
    private TextView theServiceDescription, theServicePrice, theServiceName, theShopOwnerName, theShopOwnerContact, theShopOwnerLocation;

    private String theShopLocation, serviceId, shopName, serviceName, serviceDuration, serviceDescription, servicePrice, serviceProviderId, serviceImage, serviceCategory, shopImage, shopContactNum, shopLocation, shopRating;
    private String uid, shopID;
    private String mediumPercentage, largePercentage;
    private String shopOpenAt, shopCloseAt;
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_services_clicked);

        backBtn = findViewById(R.id.backBtn);

        Intent intent = getIntent();
        serviceId = intent.getStringExtra("serviceId");
        shopID = intent.getStringExtra("shopID");
        shopName = intent.getStringExtra("shopName");
        serviceName = intent.getStringExtra("serviceName");
        serviceDescription = intent.getStringExtra("serviceDescription");
        servicePrice = intent.getStringExtra("servicePrice");
        serviceProviderId = intent.getStringExtra("serviceProviderId");
        serviceImage = intent.getStringExtra("serviceImage");
        serviceCategory = intent.getStringExtra("serviceCategory");
        serviceDuration = intent.getStringExtra("serviceDuration");
        shopImage = intent.getStringExtra("shopImage");
        shopContactNum = intent.getStringExtra("shopContactNum");
        shopLocation = intent.getStringExtra("shopLocation");
        shopRating = intent.getStringExtra("shopRating");
        mediumPercentage = intent.getStringExtra("mediumPercentage");
        largePercentage = intent.getStringExtra("largePercentage");

        shopOpenAt = intent.getStringExtra("shopOpenAt");
        shopCloseAt = intent.getStringExtra("shopCloseAt");


        theServiceImg = findViewById(R.id.theServiceImg);
        theServiceDescription = findViewById(R.id.theServiceDescription);
        theServicePrice = findViewById(R.id.theServicePrice);
        theServiceName = findViewById(R.id.theServiceName);
        theShopOwnerName = findViewById(R.id.theShopOwnerName);
        theShopOwnerContact = findViewById(R.id.theShopOwnerContact);
        theShopOwnerLocation = findViewById(R.id.theShopOwnerLocation);
        theShopOwnerImg = findViewById(R.id.theShopOwnerImg);
        uid = FirebaseAuthManager.getCurrentUserUid();

        bookBtn = findViewById(R.id.bookBtn);
        editServiceBtn = findViewById(R.id.editServiceBtn);
        viewRatingsBtn = findViewById(R.id.viewRatingsBtn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backBtn.setOnClickListener(s -> {
            finish();
        });

        if(uid.equals(serviceProviderId)) {
            bookBtn.setVisibility(View.GONE);
            editServiceBtn.setVisibility(View.VISIBLE);
        }

        editServiceBtn.setOnClickListener(s -> {
            Intent ints = new Intent(CustomerServicesClicked.this, ShopOwnerEditService.class);
            ints.putExtra("serviceId", serviceId);
            startActivity(ints);
        });
        getProviderDatas();
        setUpDatas();

        bookBtn.setOnClickListener(s -> {
            Intent intent1 = new Intent(CustomerServicesClicked.this, CustomerBookService.class);
            intent1.putExtra("serviceId", serviceId);
            intent1.putExtra("serviceProviderId", serviceProviderId);
            intent1.putExtra("serviceImage", serviceImage);
            intent1.putExtra("serviceName", serviceName);
            intent1.putExtra("servicePrice", servicePrice);
            intent1.putExtra("shopName", shopName);
            intent1.putExtra("shopName", shopName);
            intent1.putExtra("shopRating", shopRating);
            intent1.putExtra("serviceCategory", serviceCategory);
            intent1.putExtra("shopImage", shopImage);
            intent1.putExtra("shopLocation", shopLocation);
            intent1.putExtra("serviceDuration", serviceDuration);
            intent1.putExtra("mediumPercentage", mediumPercentage);
            intent1.putExtra("largePercentage", largePercentage);

            intent1.putExtra("theShopID", shopID);

            intent1.putExtra("shopOpenAt", shopOpenAt);
            intent1.putExtra("shopCloseAt", shopCloseAt);

            startActivity(intent1);
        });

        viewRatingsBtn.setOnClickListener(d -> {
            Intent goToServiceRatigs = new Intent(CustomerServicesClicked.this, CustomerViewServiceRatings.class);
            goToServiceRatigs.putExtra("shopID", shopID);
            goToServiceRatigs.putExtra("serviceID", serviceId);
            goToServiceRatigs.putExtra("serviceName", serviceName);
            startActivity(goToServiceRatigs);
        });

        backBtn.setOnClickListener(s -> {
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
        });
    }
    @Override
    public void finish() {
        super.finish();
        // Slide down animation when closing
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
    }
    private void setUpDatas () {
        Picasso.get().load(serviceImage).placeholder(R.drawable.sent_image_placeholder).into(theServiceImg);
        theServiceName.setText(serviceName);
        theServiceDescription.setText(serviceDescription);
        theServicePrice.setText(servicePrice);



    }
    private void getProviderDatas() {
        FirebaseDatabaseManager.getShopOwnerData(shopID, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel data) {
                Picasso.get().load(data.getShopImage()).placeholder(R.drawable.sent_image_placeholder).into(theShopOwnerImg);
                theShopOwnerName.setText(data.getShopName());
                theShopOwnerContact.setText(data.getContactNum());
                theShopOwnerLocation.setText(data.getLocation());
            }

            @Override
            public void onFailure(String failed) {

            }
        });
    }
}
