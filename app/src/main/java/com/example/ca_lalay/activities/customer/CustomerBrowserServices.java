package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.example.ca_lalay.firebase.FirebaseCarServicesManager;
import com.example.ca_lalay.firebase.callback.CarBrowserServiceCallback;
import com.example.ca_lalay.model.ShopOwnerServicesModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomerBrowserServices extends AppCompatActivity {
    private RecyclerView serviceRV;
    CustomerServicesAdapter adapter;
    private EditText searchEditText;
    private ImageView filterImage, backBtn;

    // filter state - preserved across dialog re-opens
    private String selectedCategory = "Clear";
    private Double minPrice = null;
    private Double maxPrice = null;
    private Double minDuration = null;
    private Double maxDuration = null;

    /** "none" | "lowest" | "highest" - separate from category now */
    private String sortOption = "none";

    private LinearLayout selectedFilterLayout;
    private TextView selectedFilterText;

    private List<ShopOwnerServicesModel> serviceList;

    // Sort options ("Highest Price"/"Lowest Price") removed from here -
    // they now live in their own RadioGroup in the dialog.
    private static final String[] SERVICE_CATEGORIES = {
            "Clear",
            "Oil Change",
            "Car Wash",
            "Tire Replacement",
            "Engine Repair",
            "Brake Service",
            "Battery Replacement",
            "Car Detailing",
            "Others"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_browser_services);

        serviceRV = findViewById(R.id.serviceRV);
        serviceRV.setLayoutManager(new LinearLayoutManager(this));
        serviceList = new ArrayList<>();
        adapter = new CustomerServicesAdapter(this, serviceList);
        serviceRV.setAdapter(adapter);

        searchEditText = findViewById(R.id.searchEditText);
        filterImage = findViewById(R.id.filterImage);

        selectedFilterLayout = findViewById(R.id.selectedFilterLayout);
        selectedFilterText = findViewById(R.id.selectedFilterText);

        backBtn = findViewById(R.id.backBtn);

        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fetchServices();

        filterImage.setOnClickListener(s -> showFilterDialog());

        backBtn.setOnClickListener(v -> finish());
    }

    private void fetchServices() {
        FirebaseCarServicesManager.fetchAllActiveServices(
                new CarBrowserServiceCallback() {

                    @Override
                    public void onSuccess(String message, String serviceId, String providerId) {}

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(CustomerBrowserServices.this, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccessList(List<ShopOwnerServicesModel> services) {
                        serviceList.clear();
                        serviceList.addAll(services);

                        adapter.updateFullList(services);
                        adapter.notifyDataSetChanged();
                    }
                }
        );
    }

    /**
     * Opens the combined filter modal: category + price range + duration range + sort.
     * All active filters are applied together (AND'ed); sort is independent of category.
     */
    private void showFilterDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_filter_services, null);

        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText minPriceInput = dialogView.findViewById(R.id.minPriceInput);
        EditText maxPriceInput = dialogView.findViewById(R.id.maxPriceInput);
        EditText minDurationInput = dialogView.findViewById(R.id.minDurationInput);
        EditText maxDurationInput = dialogView.findViewById(R.id.maxDurationInput);
        RadioGroup sortRadioGroup = dialogView.findViewById(R.id.sortRadioGroup);
        Button btnClearFilters = dialogView.findViewById(R.id.btnClearFilters);
        Button btnApplyFilters = dialogView.findViewById(R.id.btnApplyFilters);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, SERVICE_CATEGORIES
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // restore previous state
        int categoryIndex = Arrays.asList(SERVICE_CATEGORIES).indexOf(selectedCategory);
        categorySpinner.setSelection(categoryIndex >= 0 ? categoryIndex : 0);

        if (minPrice != null) minPriceInput.setText(trimDecimal(minPrice));
        if (maxPrice != null) maxPriceInput.setText(trimDecimal(maxPrice));
        if (minDuration != null) minDurationInput.setText(trimDecimal(minDuration));
        if (maxDuration != null) maxDurationInput.setText(trimDecimal(maxDuration));

        switch (sortOption) {
            case "lowest":
                sortRadioGroup.check(R.id.radioSortLowest);
                break;
            case "highest":
                sortRadioGroup.check(R.id.radioSortHighest);
                break;
            default:
                sortRadioGroup.check(R.id.radioSortNone);
        }

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnClearFilters.setOnClickListener(v -> {
            selectedCategory = "Clear";
            minPrice = null;
            maxPrice = null;
            minDuration = null;
            maxDuration = null;
            sortOption = "none";

            updateSelectedFilterBadge();
            applyFilters();
            dialog.dismiss();
        });

        btnApplyFilters.setOnClickListener(v -> {
            String category = SERVICE_CATEGORIES[categorySpinner.getSelectedItemPosition()];

            Double newMinPrice = parseNullableDouble(minPriceInput.getText().toString());
            Double newMaxPrice = parseNullableDouble(maxPriceInput.getText().toString());
            Double newMinDuration = parseNullableDouble(minDurationInput.getText().toString());
            Double newMaxDuration = parseNullableDouble(maxDurationInput.getText().toString());

            if (newMinPrice != null && newMaxPrice != null && newMinPrice > newMaxPrice) {
                Toast.makeText(this, "Min price can't be greater than max price", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newMinDuration != null && newMaxDuration != null && newMinDuration > newMaxDuration) {
                Toast.makeText(this, "Min duration can't be greater than max duration", Toast.LENGTH_SHORT).show();
                return;
            }

            int checkedId = sortRadioGroup.getCheckedRadioButtonId();
            String newSortOption;
            if (checkedId == R.id.radioSortLowest) {
                newSortOption = "lowest";
            } else if (checkedId == R.id.radioSortHighest) {
                newSortOption = "highest";
            } else {
                newSortOption = "none";
            }

            selectedCategory = category;
            minPrice = newMinPrice;
            maxPrice = newMaxPrice;
            minDuration = newMinDuration;
            maxDuration = newMaxDuration;
            sortOption = newSortOption;

            updateSelectedFilterBadge();
            applyFilters();

            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyFilters() {
        adapter.filter(
                searchEditText.getText().toString(),
                selectedCategory,
                minPrice,
                maxPrice,
                minDuration,
                maxDuration,
                sortOption
        );
    }

    private Double parseNullableDouble(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trimDecimal(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    /** Shows a single badge summarizing every active filter (category, price, duration, sort). */
    private void updateSelectedFilterBadge() {
        List<String> parts = new ArrayList<>();

        if (!selectedCategory.equals("Clear")) {
            parts.add(selectedCategory);
        }
        if (minPrice != null || maxPrice != null) {
            String min = minPrice != null ? trimDecimal(minPrice) : "0";
            String max = maxPrice != null ? trimDecimal(maxPrice) : "∞";
            parts.add("₱" + min + " - ₱" + max);
        }
        if (minDuration != null || maxDuration != null) {
            String min = minDuration != null ? trimDecimal(minDuration) : "0";
            String max = maxDuration != null ? trimDecimal(maxDuration) : "∞";
            parts.add(min + "-" + max + " hrs");
        }
        if (sortOption.equals("lowest")) {
            parts.add("Sort: Lowest Price");
        } else if (sortOption.equals("highest")) {
            parts.add("Sort: Highest Price");
        }

        if (parts.isEmpty()) {
            selectedFilterLayout.setVisibility(View.GONE);
        } else {
            selectedFilterLayout.setVisibility(View.VISIBLE);
            selectedFilterText.setText(TextUtils.join("  •  ", parts));
        }
    }
}