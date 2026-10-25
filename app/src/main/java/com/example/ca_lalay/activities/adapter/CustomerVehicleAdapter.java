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
import com.example.ca_lalay.model.CustomerVehicleModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomerVehicleAdapter extends RecyclerView.Adapter<CustomerVehicleAdapter.VehicleViewHolder> {

    private Context context;
    private List<CustomerVehicleModel> vehicleList;
    private int lastAnimatedPosition = -1;

    private OnVehicleDeleteClickListener deleteListener;

    public CustomerVehicleAdapter(Context context,
                                  List<CustomerVehicleModel> vehicleList,   OnVehicleDeleteClickListener deleteListener) {

        this.context = context;
        this.vehicleList = vehicleList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.my_vehicles_itemlayout,
                        parent,
                        false);

        return new VehicleViewHolder(view);
    }

    public interface OnVehicleDeleteClickListener {
        void onDeleteClick(CustomerVehicleModel vehicle, int position);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VehicleViewHolder holder,
            int position) {

        CustomerVehicleModel vehicle = vehicleList.get(position);

        if (position > lastAnimatedPosition) {

            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationY(50f);

            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();

            lastAnimatedPosition = position;
        }

        holder.vehicleName.setText(vehicle.getVehicleName());
        holder.vehiclePlateNumber.setText(vehicle.getVehiclePlateNumber());
        holder.vehicleType.setText(vehicle.getVehicleType());
        holder.vehicleSize.setText(vehicle.getVehicleSize());
        holder.vehicleWheels.setText(vehicle.getVehicleWheels() + " Wheels");

        holder.trashIcon.setOnClickListener(v -> {

            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION) {
                deleteListener.onDeleteClick(
                        vehicleList.get(currentPosition),
                        currentPosition
                );
            }
        });

        Picasso.get()
                .load(vehicle.getVehicleImage())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.sent_image_placeholder)
                .into(holder.vehicleImage);
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public static class VehicleViewHolder extends RecyclerView.ViewHolder {

        ImageView vehicleImage, trashIcon;

        TextView vehicleName;
        TextView vehiclePlateNumber;
        TextView vehicleType;
        TextView vehicleSize;
        TextView vehicleWheels;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);

            vehicleImage = itemView.findViewById(R.id.vehicleImage);
            trashIcon = itemView.findViewById(R.id.trashIcon);
            vehicleName = itemView.findViewById(R.id.vehicleName);
            vehiclePlateNumber = itemView.findViewById(R.id.vehiclePlateNumber);
            vehicleType = itemView.findViewById(R.id.vehicleType);
            vehicleSize = itemView.findViewById(R.id.vehicleSize);
            vehicleWheels = itemView.findViewById(R.id.vehicleWheels);
        }
    }
}