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

public class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<RatingsModel> ratingsList;

    public RatingsAdapter(Context context,
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
                .inflate(R.layout.ratings_item_layout,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        RatingsModel model = ratingsList.get(position);

        holder.reviewerName.setText(model.getRaterName());

        holder.serviceRated.setText(model.getServiceName());

        holder.dateRated.setText(model.getDateRated());

        String filteredComment =
                ProfanityFilter.filter(model.getRatingComment());

        holder.reviewComment.setText(filteredComment);

        Picasso.get()
                .load(model.getRaterImage())
                .placeholder(R.drawable.sent_image_placeholder)
                .into(holder.reviewerImage);

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

        CircleImageView reviewerImage;

        TextView reviewerName,
                serviceRated,
                dateRated,
                reviewComment;

        ImageView starOne,
                starTwo,
                starThree,
                starFour,
                starFive;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            reviewerImage = itemView.findViewById(R.id.reviewerImage);

            reviewerName = itemView.findViewById(R.id.reviewerName);

            serviceRated = itemView.findViewById(R.id.serviceRated);

            dateRated = itemView.findViewById(R.id.dateRated);

            reviewComment = itemView.findViewById(R.id.reviewComment);

            starOne = itemView.findViewById(R.id.starOne);

            starTwo = itemView.findViewById(R.id.startwo);

            starThree = itemView.findViewById(R.id.starthree);

            starFour = itemView.findViewById(R.id.starfour);

            starFive = itemView.findViewById(R.id.starfive);

        }

    }

}