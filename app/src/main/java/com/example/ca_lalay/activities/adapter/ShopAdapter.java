package com.example.ca_lalay.activities.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.customer.CustomerShopClicked;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {

    private Context context;
    private List<ShopOwnerModel> shopList;
    private List<ShopOwnerModel> shopListFull; // unfiltered master list

    // current filter state
    private String currentQuery = "";
    private String currentRoleFilter = "all";   // "all" | "shop_owner" | "freelancer"
    private double currentMinRating = 0.0;       // 0 = no rating filter

    public ShopAdapter(Context context, List<ShopOwnerModel> shopList) {
        this.context = context;
        this.shopList = shopList;
        this.shopListFull = new ArrayList<>(shopList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopOwnerModel shop = shopList.get(position);

        holder.shopName.setText(shop.getShopName());
        holder.shopRatings.setText(shop.getOverallRating());

        String theRole = shop.getRole();

        if ("freelancer".equals(theRole)) {
            holder.providerBadgeBG.setVisibility(View.VISIBLE);
            holder.providerBadge.setText("FREELANCER");
            holder.providerBadgeIcon.setImageResource(R.drawable.freelancer_tag_icon);
        } else if ("shop_owner".equals(theRole)) {
            holder.providerBadgeBG.setVisibility(View.VISIBLE);
            holder.providerBadge.setText("SHOP");
            holder.providerBadgeIcon.setImageResource(R.drawable.shop_tag_icon);
        }

        holder.viewFullDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, CustomerShopClicked.class);
            intent.putExtra("shopImage", shop.getShopImage());
            intent.putExtra("shopName", shop.getShopName());
            intent.putExtra("shopContactNum", shop.getContactNum());
            intent.putExtra("shopOpenAt", shop.getShopOpenAt() + " to " + shop.getShopClosedAt());
            intent.putExtra("shopRating", shop.getRating());
            intent.putExtra("shopEmail", shop.getEmail());
            intent.putExtra("shopAbout", shop.getShopAbout());
            intent.putExtra("shopId", shop.getUid());
            intent.putExtra("theShopLocation", shop.getLocation());
            intent.putExtra("theProviderRole", shop.getRole());
            intent.putExtra("shopLat", shop.getLatitude());
            intent.putExtra("shopLong", shop.getLongitude());

            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(
                        R.anim.slide_in_up,
                        R.anim.stay
                );
            }
        });

        Picasso.get()
                .load(shop.getShopImage())
                .placeholder(R.drawable.sent_image_placeholder)
                .into(holder.shopImage);
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    // Call this whenever you load/refresh shops from Firebase
    public void updateData(List<ShopOwnerModel> newShops) {
        List<ShopOwnerModel> activeShops = new ArrayList<>();

        for (ShopOwnerModel shop : newShops) {
            if ("active".equalsIgnoreCase(shop.getStatus())) {
                activeShops.add(shop);
            }
        }

        activeShops.sort((shop1, shop2) -> {
            long timestamp1 = 0L;
            long timestamp2 = 0L;

            try {
                if (shop1.getTimestamp() != null) timestamp1 = Long.parseLong(shop1.getTimestamp());
            } catch (NumberFormatException ignored) { }

            try {
                if (shop2.getTimestamp() != null) timestamp2 = Long.parseLong(shop2.getTimestamp());
            } catch (NumberFormatException ignored) { }

            return Long.compare(timestamp2, timestamp1);
        });

        shopListFull.clear();
        shopListFull.addAll(activeShops);

        applyFilters(); // re-apply whatever filters/search are currently active
    }

    // ---------- SEARCH ----------
    public void filter(String query) {
        currentQuery = query == null ? "" : query;
        applyFilters();
    }

    // ---------- ROLE FILTER ----------
    // role: "all", "shop_owner", or "freelancer"
    public void filterByRole(String role) {
        currentRoleFilter = role == null ? "all" : role;
        applyFilters();
    }

    // ---------- RATING FILTER ----------
    // minRating: 0 = no filter, 4.0 = show 4 stars and up, etc.
    public void filterByMinRating(double minRating) {
        currentMinRating = minRating;
        applyFilters();
    }

    // Set everything at once (used by the filter dialog's "Apply" button)
    public void setFilters(String query, String role, double minRating) {
        currentQuery = query == null ? "" : query;
        currentRoleFilter = role == null ? "all" : role;
        currentMinRating = minRating;
        applyFilters();
    }

    public void resetFilters() {
        currentQuery = "";
        currentRoleFilter = "all";
        currentMinRating = 0.0;
        applyFilters();
    }

    public String getCurrentRoleFilter() {
        return currentRoleFilter;
    }

    public double getCurrentMinRating() {
        return currentMinRating;
    }

    // ---------- CORE FILTER LOGIC ----------
    private void applyFilters() {
        String q = currentQuery.trim().toLowerCase(Locale.getDefault());

        List<ShopOwnerModel> filtered = new ArrayList<>();

        for (ShopOwnerModel shop : shopListFull) {

            boolean matchesQuery = q.isEmpty() ||
                    (shop.getShopName() != null && shop.getShopName().toLowerCase(Locale.getDefault()).contains(q)) ||
                    (shop.getLocation() != null && shop.getLocation().toLowerCase(Locale.getDefault()).contains(q)) ||
                    (shop.getContactNum() != null && shop.getContactNum().toLowerCase(Locale.getDefault()).contains(q)) ||
                    (shop.getEmail() != null && shop.getEmail().toLowerCase(Locale.getDefault()).contains(q));

            boolean matchesRole = "all".equals(currentRoleFilter) ||
                    currentRoleFilter.equals(shop.getRole());

            boolean matchesRating = currentMinRating <= 0 ||
                    parseRating(shop.getOverallRating()) >= currentMinRating;

            if (matchesQuery && matchesRole && matchesRating) {
                filtered.add(shop);
            }
        }

        shopList.clear();
        shopList.addAll(filtered);
        notifyDataSetChanged();
    }

    // Safely parses "5.0", "4.7", "5", null, or garbage without crashing
    private double parseRating(String ratingStr) {
        if (ratingStr == null || ratingStr.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(ratingStr.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView shopImage;
        TextView shopName, shopRatings;
        FrameLayout viewFullDetails;

        TextView providerBadge;
        LinearLayout providerBadgeBG;
        ImageView providerBadgeIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            shopImage = itemView.findViewById(R.id.shopImage);
            shopName = itemView.findViewById(R.id.shopName);
            shopRatings = itemView.findViewById(R.id.shopRatings);

            providerBadge = itemView.findViewById(R.id.providerBadge);
            providerBadgeBG = itemView.findViewById(R.id.providerBadgeBG);
            providerBadgeIcon = itemView.findViewById(R.id.providerBadgeIcon);

            viewFullDetails = itemView.findViewById(R.id.viewFullDetails);
        }
    }
}