package com.example.ca_lalay.activities.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.model.RatingsModel;
import com.example.ca_lalay.utils.ProfanityFilter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerRatingsAdapter extends RecyclerView.Adapter<CustomerRatingsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<RatingsModel> ratingsList;

    public CustomerRatingsAdapter(Context context,
                          ArrayList<RatingsModel> ratingsList) {

        this.context = context;
        this.ratingsList = ratingsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.shop_rated_layout,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        RatingsModel model = ratingsList.get(position);

        holder.shopNameRated.setText(model.getShopName());

        holder.serviceRated.setText(model.getServiceName());

        holder.dateRated.setText(model.getDateRated());

        holder.youRatedTV.setText("You rated " + model.getRatingStar());

        String filteredComment =
                ProfanityFilter.filter(model.getRatingComment());

        holder.yourReviewComment.setText(filteredComment);

        Picasso.get()
                .load(model.getShopImage())
                .placeholder(R.drawable.sent_image_placeholder)
                .into(holder.shopImageRated);

        int stars = (int) Float.parseFloat(model.getRatingStar());

        ImageView[] imageViews = {
                holder.starOne,
                holder.starTwo,
                holder.starThree,
                holder.starFour,
                holder.starFive
        };

        for (int i = 0; i < imageViews.length; i++) {

            if (i < stars) {
                imageViews[i].setAlpha(1f);
            } else {
                imageViews[i].setAlpha(.2f);
            }

        }

    }

    @Override
    public int getItemCount() {
        return ratingsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView shopImageRated;

        TextView shopNameRated, serviceRated, dateRated, yourReviewComment, youRatedTV;

        ImageView starOne,
                starTwo,
                starThree,
                starFour,
                starFive;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            shopImageRated = itemView.findViewById(R.id.shopImageRated);
            shopNameRated = itemView.findViewById(R.id.shopNameRated);
            serviceRated = itemView.findViewById(R.id.serviceRated);
            dateRated = itemView.findViewById(R.id.dateRated);
            yourReviewComment = itemView.findViewById(R.id.yourReviewComment);
            youRatedTV = itemView.findViewById(R.id.youRatedTV);

            starOne = itemView.findViewById(R.id.starOne);

            starTwo = itemView.findViewById(R.id.startwo);

            starThree = itemView.findViewById(R.id.starthree);

            starFour = itemView.findViewById(R.id.starfour);

            starFive = itemView.findViewById(R.id.starfive);
        }

    }

}