package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.ca_lalay.firebase.FirebaseCarServicesManager;
import com.example.ca_lalay.firebase.callback.CarBrowserServiceCallback;
import com.example.ca_lalay.model.ShopOwnerServicesModel;

import java.util.ArrayList;
import java.util.List;

public class CustomerViewShopAllServices extends AppCompatActivity {
    private RecyclerView shopServicesRV;
    private String shopId;
    private  CustomerServicesAdapter adapter;
    private List<ShopOwnerServicesModel> serviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_view_shop_all_services);

        shopServicesRV = findViewById(R.id.shopServicesRV);

        shopServicesRV.setLayoutManager(new LinearLayoutManager(this));
        serviceList = new ArrayList<>();
        adapter = new CustomerServicesAdapter(this, serviceList);
        shopServicesRV.setAdapter(adapter);

        Intent intent = getIntent();
        shopId = intent.getStringExtra("shopId");


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fetchServices();
    }
    private void fetchServices() {

        FirebaseCarServicesManager.fetchActiveServicesByShop(
                shopId,
                new CarBrowserServiceCallback() {

                    @Override
                    public void onSuccess(
                            String success,
                            String serviceId,
                            String serviceProviderId
                    ) {
                    }

                    @Override
                    public void onFailure(String failed) {

                        Toast.makeText(
                                CustomerViewShopAllServices.this,
                                failed,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onSuccessList(
                            List<ShopOwnerServicesModel> services
                    ) {

                        serviceList.clear();
                        serviceList.addAll(services);

                        adapter.updateFullList(services);
                        adapter.notifyDataSetChanged();
                    }
                }
        );
    }
}