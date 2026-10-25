package com.example.ca_lalay.activities.customer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.ShopAdapter;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.callback.ShopListCallback;
import com.example.ca_lalay.model.ShopOwnerModel;

import java.util.ArrayList;
import java.util.List;

public class CustomerViewAllShops extends AppCompatActivity {
    private ImageView backBtn, filterBtn;
    private RecyclerView carShopsRV;
    private EditText searchShop;

    private List<ShopOwnerModel> shopList;
    private ShopAdapter adapter;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_view_all_shops);

        backBtn = findViewById(R.id.backBtn);
        filterBtn = findViewById(R.id.filterBtn);
        searchShop = findViewById(R.id.searchShop);
        carShopsRV = findViewById(R.id.carShopsRV);
        shopList = new ArrayList<>();
        adapter = new ShopAdapter(this, shopList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        );
        carShopsRV.setLayoutManager(layoutManager);
        carShopsRV.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchShop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchShops();

        backBtn.setOnClickListener(d -> finish());

        filterBtn.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_shop_filter, null);

        RadioGroup roleGroup = dialogView.findViewById(R.id.roleRadioGroup);
        RadioGroup ratingGroup = dialogView.findViewById(R.id.ratingRadioGroup);
        Button btnApply = dialogView.findViewById(R.id.btnApplyFilter);
        Button btnReset = dialogView.findViewById(R.id.btnResetFilter);

        // Pre-select whatever is currently active
        switch (adapter.getCurrentRoleFilter()) {
            case "shop_owner":
                roleGroup.check(R.id.radioShopOwner);
                break;
            case "freelancer":
                roleGroup.check(R.id.radioFreelancer);
                break;
            default:
                roleGroup.check(R.id.radioAll);
        }

        double currentMin = adapter.getCurrentMinRating();
        if (currentMin >= 5) ratingGroup.check(R.id.radioRating5);
        else if (currentMin >= 4) ratingGroup.check(R.id.radioRating4);
        else if (currentMin >= 3) ratingGroup.check(R.id.radioRating3);
        else if (currentMin >= 2) ratingGroup.check(R.id.radioRating2);
        else if (currentMin >= 1) ratingGroup.check(R.id.radioRating1);
        else ratingGroup.check(R.id.radioRatingAll);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnApply.setOnClickListener(v -> {
            String role;
            int roleCheckedId = roleGroup.getCheckedRadioButtonId();
            if (roleCheckedId == R.id.radioShopOwner) {
                role = "shop_owner";
            } else if (roleCheckedId == R.id.radioFreelancer) {
                role = "freelancer";
            } else {
                role = "all";
            }

            double minRating;
            int ratingCheckedId = ratingGroup.getCheckedRadioButtonId();
            if (ratingCheckedId == R.id.radioRating5) {
                minRating = 5.0;
            } else if (ratingCheckedId == R.id.radioRating4) {
                minRating = 4.0;
            } else if (ratingCheckedId == R.id.radioRating3) {
                minRating = 3.0;
            } else if (ratingCheckedId == R.id.radioRating2) {
                minRating = 2.0;
            } else if (ratingCheckedId == R.id.radioRating1) {
                minRating = 1.0;
            } else {
                minRating = 0.0;
            }

            adapter.setFilters(searchShop.getText().toString(), role, minRating);
            dialog.dismiss();
        });

        btnReset.setOnClickListener(v -> {
            searchShop.setText("");
            adapter.resetFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void fetchShops() {
        FirebaseDatabaseManager.getAllShopOwners(new ShopListCallback() {
            @Override
            public void onSuccess(List<ShopOwnerModel> shops) {
                adapter.updateData(shops);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CustomerViewAllShops.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}