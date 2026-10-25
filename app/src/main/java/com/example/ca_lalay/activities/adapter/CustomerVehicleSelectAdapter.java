package com.example.ca_lalay.activities.adapter;

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

public class CustomerVehicleSelectAdapter extends RecyclerView.Adapter<CustomerVehicleSelectAdapter.ViewHolder> {

    public interface OnVehicleSelectListener {
        void onVehicleSelected(CustomerVehicleModel vehicle);
    }

    private final List<CustomerVehicleModel> vehicleList;
    private final OnVehicleSelectListener listener;

    public CustomerVehicleSelectAdapter(List<CustomerVehicleModel> vehicleList, OnVehicleSelectListener listener) {
        this.vehicleList = vehicleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerVehicleModel vehicle = vehicleList.get(position);

        holder.vehicleName.setText(vehicle.getVehicleName());
        holder.vehiclePlate.setText(vehicle.getVehiclePlateNumber());
        holder.vehicleType.setText(vehicle.getVehicleType());

        Picasso.get()
                .load(vehicle.getVehicleImage())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.sent_image_placeholder)
                .into(holder.vehicleImage);

        holder.itemView.setOnClickListener(v -> listener.onVehicleSelected(vehicle));
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView vehicleImage;
        TextView vehicleName, vehiclePlate, vehicleType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            vehicleImage = itemView.findViewById(R.id.vehicleImage);
            vehicleName = itemView.findViewById(R.id.vehicleName);
            vehiclePlate = itemView.findViewById(R.id.vehiclePlate);
            vehicleType = itemView.findViewById(R.id.vehicleType);
        }
    }
}