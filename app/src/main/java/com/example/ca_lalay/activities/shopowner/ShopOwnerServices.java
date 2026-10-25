package com.example.ca_lalay.activities.shopowner;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.CustomerServicesAdapter;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseCarServicesManager;
import com.example.ca_lalay.firebase.callback.CarBrowserServiceCallback;
import com.example.ca_lalay.firebase.callback.CountServicesCallback;
import com.example.ca_lalay.model.ShopOwnerServicesModel;

import java.util.ArrayList;
import java.util.List;

public class ShopOwnerServices extends AppCompatActivity {
    private Button createServiceBtn;
    private String theShopName;
    private RecyclerView servicesRV;
    private ImageView backBtn;
    private String uid;
    private CustomerServicesAdapter adapter;
    private List<ShopOwnerServicesModel> serviceList;
    private TextView serviceCount, activeSercicesCount, InActiveSercicesCount;
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_services);

        createServiceBtn = findViewById(R.id.createServiceBtn);
        servicesRV = findViewById(R.id.servicesRV);

        servicesRV.setLayoutManager(new LinearLayoutManager(this));
        serviceList = new ArrayList<>();
        adapter = new CustomerServicesAdapter(this, serviceList);
        servicesRV.setAdapter(adapter);

        backBtn = findViewById(R.id.backBtn);
        serviceCount = findViewById(R.id.serviceCount);
        activeSercicesCount = findViewById(R.id.activeSercicesCount);
        InActiveSercicesCount = findViewById(R.id.InActiveSercicesCount);

        theShopName = getIntent().getStringExtra("shopName");
        uid = FirebaseAuthManager.getCurrentUserUid();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        countAllService();
        countAllActiveService();
        countAllInactiveService();

        backBtn.setOnClickListener(s -> {
            finish();
        });

        createServiceBtn.setOnClickListener(s -> {
            Intent intent = new Intent(ShopOwnerServices.this, ShopOwnerCreateNewService.class);
            intent.putExtra("shopName", theShopName);
            startActivity(intent);
        });
        fetchServices();
    }
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
    }
    private void fetchServices () {
        FirebaseCarServicesManager.fetchServicesByShop(uid, new CarBrowserServiceCallback() {
            @Override
            public void onSuccess(String success, String serviceId, String serviceProviderId) {

            }

            @Override
            public void onFailure(String failed) {

            }

            @Override
            public void onSuccessList(List<ShopOwnerServicesModel> services) {
                serviceList.clear();
                serviceList.addAll(0, services);
                adapter.updateFullList(services);
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void countAllService() {
        FirebaseCarServicesManager.getServiceCount(uid, new CountServicesCallback() {
            @Override
            public void onSuccess(int count) {
                String theCount = String.valueOf(count);
                serviceCount.setText(theCount);
                Log.d("TOTAL COUNT: ", theCount);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ShopOwnerServices.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void countAllActiveService() {
        FirebaseCarServicesManager.getActiveServiceCount(uid, new CountServicesCallback() {
            @Override
            public void onSuccess(int count) {
                String theCount = String.valueOf(count);
                activeSercicesCount.setText(theCount);
                Log.d("ACTIVE SERVICE COUNT: ", theCount);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ShopOwnerServices.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void countAllInactiveService() {
        FirebaseCarServicesManager.getInactiveServiceCount(uid, new CountServicesCallback() {
            @Override
            public void onSuccess(int count) {
                String theCount = String.valueOf(count);
                InActiveSercicesCount.setText(theCount);
            }

            @Override
            public void onFailure(String message) {

            }
        });
    }
}