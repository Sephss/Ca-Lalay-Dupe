package com.example.ca_lalay.activities.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.customer.CustomerServicesClicked;
import com.example.ca_lalay.model.ShopOwnerServicesModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerServicesAdapter extends RecyclerView.Adapter<CustomerServicesAdapter.ViewHolder> {

    private Context context;
    private List<ShopOwnerServicesModel> serviceList;
    private List<ShopOwnerServicesModel> fullList;

    public CustomerServicesAdapter(Context context, List<ShopOwnerServicesModel> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
        this.fullList = new ArrayList<>(serviceList);
    }

    @NonNull
    @Override
    public CustomerServicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.services_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerServicesAdapter.ViewHolder holder, int position) {

        ShopOwnerServicesModel service = serviceList.get(position);
        String description = service.getDescription();
        if (description != null && description.length() > 57) {
            description = description.substring(0, 57) + "...";
        }

        holder.serviceName.setText(service.getName());
        holder.serviceDescription.setText(description);
        holder.servicePrice.setText("₱ " + service.getPrice());


        holder.serviceProviderName.setText(service.getShopName());

        // Load image using Picasso
        if (service.getImageUrl() != null && !service.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(service.getImageUrl())
                    .placeholder(R.drawable.sent_image_placeholder)
                    .into(holder.serviceImage);
        }

        if(service.getStatus().equals("active")) {
            holder.serviceInactiveWarningLayout.setVisibility(View.GONE);
        } else {
            holder.serviceInactiveWarningLayout.setVisibility(View.VISIBLE);
        }

        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, CustomerServicesClicked.class);
            intent.putExtra("serviceId", service.getServiceId());
            intent.putExtra("shopID", service.getShopID());
            intent.putExtra("shopName", service.getShopName());
            intent.putExtra("serviceName", service.getName());
            intent.putExtra("serviceDescription", service.getDescription());
            intent.putExtra("servicePrice", service.getPrice());
            intent.putExtra("serviceProviderId", service.getServiceProviderId());
            intent.putExtra("serviceImage", service.getImageUrl());
            intent.putExtra("serviceCategory", service.getCategory());
            intent.putExtra("serviceDuration", service.getDurationMinutes());
            intent.putExtra("shopImage", service.getShopImageUrl());
            intent.putExtra("shopContactNum", service.getShopContactNum());
            intent.putExtra("shopLocation", service.getShopLocation());
            intent.putExtra("shopRating", service.getShopRatings());
            intent.putExtra("serviceDuration", service.getDurationMinutes());
            intent.putExtra("mediumPercentage", service.getMediumPercentage());
            intent.putExtra("largePercentage", service.getLargePercentage());
            intent.putExtra("shopOpenAt", service.getShopOpenAt());
            intent.putExtra("shopCloseAt", service.getShopClosedAt());

            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(
                        R.anim.slide_in_up,
                        R.anim.stay
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView serviceImage;
        TextView serviceName, serviceDescription, servicePrice, serviceProviderName, inactiveText;
        Button btnViewDetails;

        LinearLayout serviceInactiveWarningLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceImage = itemView.findViewById(R.id.serviceImage);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
            servicePrice = itemView.findViewById(R.id.servicePrice);
            serviceProviderName = itemView.findViewById(R.id.serviceProviderName);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails); // FIX ID if needed

            serviceInactiveWarningLayout = itemView.findViewById(R.id.serviceInactiveWarningLayout);
            inactiveText = itemView.findViewById(R.id.selectedFilterText);
        }
    }
    public void updateFullList(List<ShopOwnerServicesModel> list) {
        fullList.clear();
        fullList.addAll(list);
    }

    public void filter(
            String text,
            String category,
            Double minPrice,
            Double maxPrice,
            Double minDuration,
            Double maxDuration,
            String sortOption // "none" | "lowest" | "highest"
    ) {
        serviceList.clear();

        String searchText = text == null ? "" : text.trim().toLowerCase();

        for (ShopOwnerServicesModel item : fullList) {

            String serviceName = item.getName() == null ? "" : item.getName().toLowerCase();
            String shopName = item.getShopName() == null ? "" : item.getShopName().toLowerCase();
            String description = item.getDescription() == null ? "" : item.getDescription().toLowerCase();
            String itemCategory = item.getCategory() == null ? "" : item.getCategory().toLowerCase();
            String location = item.getShopLocation() == null ? "" : item.getShopLocation().toLowerCase();

            boolean matchesSearch =
                    serviceName.contains(searchText) ||
                            shopName.contains(searchText) ||
                            description.contains(searchText) ||
                            itemCategory.contains(searchText) ||
                            location.contains(searchText);

            boolean matchesCategory =
                    category == null ||
                            category.equalsIgnoreCase("Clear") ||
                            itemCategory.equals(category.toLowerCase());

            double itemPrice = parsePrice(item.getPrice());
            boolean matchesPrice =
                    (minPrice == null || itemPrice >= minPrice) &&
                            (maxPrice == null || itemPrice <= maxPrice);

            double itemDuration = parseDuration(item.getDurationMinutes());
            boolean matchesDuration =
                    (minDuration == null || itemDuration >= minDuration) &&
                            (maxDuration == null || itemDuration <= maxDuration);

            if (matchesSearch && matchesCategory && matchesPrice && matchesDuration) {
                serviceList.add(item);
            }
        }

        if ("lowest".equalsIgnoreCase(sortOption) || "highest".equalsIgnoreCase(sortOption)) {
            boolean sortHighest = "highest".equalsIgnoreCase(sortOption);
            Collections.sort(serviceList, (a, b) -> {
                double priceA = parsePrice(a.getPrice());
                double priceB = parsePrice(b.getPrice());
                return sortHighest
                        ? Double.compare(priceB, priceA)
                        : Double.compare(priceA, priceB);
            });
        }

        notifyDataSetChanged();
    }

    private double parsePrice(String price) {
        if (price == null || price.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(price.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDuration(String duration) {
        if (duration == null || duration.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(duration.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}