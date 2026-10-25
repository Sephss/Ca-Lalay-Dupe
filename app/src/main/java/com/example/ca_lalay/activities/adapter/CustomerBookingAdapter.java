package com.example.ca_lalay.activities.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.customer.CustomerMyBookingsClicked;
import com.example.ca_lalay.activities.customer.CustomerServicesClicked;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.model.CustomerBookingModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerBookingAdapter extends RecyclerView.Adapter<CustomerBookingAdapter.ViewHolder> {

    private Context context;
    private List<CustomerBookingModel> bookingList;

    public CustomerBookingAdapter(Context context, List<CustomerBookingModel> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bookings_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerBookingModel booking = bookingList.get(position);

        boolean isPastBooking = isBookingDatePast(
                booking.getServiceDate()
        );

        holder.serviceName.setText(booking.getServiceType());
        holder.serviceVehicle.setText(booking.getBookerVehicle());

        double overallPrice = Double.parseDouble(booking.getOverallPriceOfService());
        double additionalServicePrice = Double.parseDouble(booking.getServiceAddedTotalPrice());

        double totalPrice = overallPrice + additionalServicePrice;

        holder.servicePrice.setText(
                String.format("₱" + totalPrice)
        );

        holder.serviceDate.setText(booking.getServiceDate());
        holder.serviceTime.setText(booking.getServiceTime());

        // rejected reason
        if("none".equals(booking.getRejectedBookingReason())) {
            holder.rejectedReasonLayout.setVisibility(View.GONE);
        } else {
            holder.rejectedReasonLayout.setVisibility(View.VISIBLE);
            holder.rejectedReasonText.setText(booking.getRejectedBookingReason());
        }

        if(holder.currentUserID.equals(booking.getProviderId())) {
            holder.bookerName.setText(booking.getBookerName());
            Picasso.get().load(booking.getBookerImage()).placeholder(R.drawable.sent_image_placeholder).into(holder.bookerImage);
        } else {
            holder.bookerName.setText(booking.getServiceProviderName());
            Picasso.get().load(booking.getServiceProviderImage()).placeholder(R.drawable.sent_image_placeholder).into(holder.bookerImage);
        }

        if (isPastBooking &&
                ("pending".equalsIgnoreCase(booking.getBookingStatus())
                        || "confirmed".equalsIgnoreCase(booking.getBookingStatus())
                        || "inProgress".equalsIgnoreCase(booking.getBookingStatus()))) {

            holder.serviceStatus.setText("Expired");

        } else {

            switch (booking.getBookingStatus()) {

                case "pending":
                    holder.serviceStatus.setText("Pending");
                    break;

                case "inProgress":
                    holder.serviceStatus.setText("In Progress");
                    break;

                case "done":
                    holder.serviceStatus.setText("Done");
                    break;

                case "confirmed":
                    holder.serviceStatus.setText("Confirmed");
                    break;

                case "rejected":
                    holder.serviceStatus.setText("Rejected");
                    break;

                case "cancelled":
                    holder.serviceStatus.setText("Cancelled");
                    break;

                default:
                    holder.serviceStatus.setText("Pending");
                    break;
            }
        }

        // Images
        if (booking.getServiceImage() != null && !booking.getServiceImage().isEmpty()) {
            Picasso.get().load(booking.getServiceImage()).placeholder(R.drawable.sent_image_placeholder).into(holder.serviceImage);
        }

        // Button click
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, CustomerMyBookingsClicked.class);
            intent.putExtra("theServiceImage", booking.getServiceImage());
            intent.putExtra("theServiceName", booking.getServiceType());
            intent.putExtra("theServicePrice", booking.getServicePrice());
            intent.putExtra("theServiceStatus", booking.getBookingStatus());
            intent.putExtra("theServiceVehicle", booking.getBookerVehicle());
            intent.putExtra("theBookingAdditionalNotes", booking.getBookerAdditionalNotes());
            intent.putExtra("theServiceDate", booking.getServiceDate());
            intent.putExtra("theServiceTime", booking.getServiceTime());
            intent.putExtra("theServiceProviderImage", booking.getServiceProviderImage());
            intent.putExtra("theServiceProviderName", booking.getServiceProviderName());
            intent.putExtra("theServiceProviderRating", booking.getBookerRating());
            intent.putExtra("theServiceProviderContactNum", booking.getBookerContactNum());
            intent.putExtra("theServiceProviderLocation", booking.getServiceProviderLocation());
            intent.putExtra("theBookingStatus", booking.getBookingStatus());
            intent.putExtra("theBookingId", booking.getBookingId());
            intent.putExtra("serviceProviderId", booking.getProviderId());
            intent.putExtra("shopName", booking.getServiceProviderName());
            intent.putExtra("shopImage", booking.getServiceProviderImage());

            intent.putExtra("billingNumber", booking.getBillingNumber());
            intent.putExtra("rejectedBookingReason", booking.getRejectedBookingReason());

            intent.putExtra("serviceAdded", booking.getServiceAdded());
            intent.putExtra("serviceAddedRemarks", booking.getServiceAddedRemarks());
            intent.putExtra("serviceAddedTotalPrice", booking.getServiceAddedTotalPrice());

            intent.putExtra("theVehicleSize", booking.getBookerVehicleSize());
            intent.putExtra("theServicePrice", booking.getServicePrice());
            intent.putExtra("additionalFeeVehicleSize", booking.getAdditionalFeeVehicleSize());
            intent.putExtra("selectedPaymentMethod", booking.getBookerPaymentMethod());

            intent.putExtra("theShopsServiceDuration", booking.getServiceDuration());
            intent.putExtra("overallPriceOfService", booking.getOverallPriceOfService());

            // receipt
            intent.putExtra("imageReceipt", booking.getBookerReceiptImage());

            //BOOKING MODIFICATION REQUEST
            intent.putExtra("shopSubmitServiceAddRequest", booking.getShopSubmitServiceAddRequest());
            intent.putExtra("customerApprovesServiceChanges", booking.getCustomerApprovesServiceChanges());
            intent.putExtra("customerRespondedToServiceChanges", booking.getCustomerRespondedToServiceChanges ());

            intent.putExtra("bookerName", booking.getBookerName());
            intent.putExtra("bookerImage", booking.getBookerImage());
            intent.putExtra("bookerContactNum", booking.getBookerNumber());
            intent.putExtra("bookerID", booking.getBookerId());
            intent.putExtra("theServiceID", booking.getServiceId());
            intent.putExtra("theServiceProviderID", booking.getProviderId());

            //payment status
            intent.putExtra("isBookerPaid", booking.getIsBookerPaid());

            // if bookings is expired checeker
            intent.putExtra("isExpired", isPastBooking);

            intent.putExtra("theStarRatings", booking.getStarRatings());
            intent.putExtra("theRatingID", booking.getRatingID());
            intent.putExtra("theRatingComment", booking.getRatingComment());
            intent.putExtra("theDateRated", booking.getDateRated());
            intent.putExtra("theIsServiceRated", booking.getIsServiceRated());
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
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView serviceImage, bookerImage;
        TextView serviceName, serviceStatus, serviceVehicle, servicePrice, serviceDate, serviceTime;
        CircleImageView serviceProviderImage;
        TextView serviceProviderName, serviceProviderRating, bookerName;
        RatingBar ratingBar;
        Button btnViewDetails;
        String currentUserID;
        LinearLayout rejectedReasonLayout;
        TextView rejectedReasonText;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceImage = itemView.findViewById(R.id.serviceImage);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceStatus = itemView.findViewById(R.id.serviceStatus);
            serviceVehicle = itemView.findViewById(R.id.serviceVehicle);
            servicePrice = itemView.findViewById(R.id.servicePrice);
            serviceDate = itemView.findViewById(R.id.serviceDate);
            serviceTime = itemView.findViewById(R.id.serviceTime);

            bookerImage = itemView.findViewById(R.id.bookerImage);
            bookerName = itemView.findViewById(R.id.bookerName);

            ratingBar = itemView.findViewById(R.id.ratingBar);

            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);

            rejectedReasonLayout = itemView.findViewById(R.id.rejectedReasonLayout);
            rejectedReasonText = itemView.findViewById(R.id.rejectedReasonText);

            currentUserID = FirebaseAuthManager.getCurrentUserUid();
        }
    }
    private boolean isBookingDatePast(String serviceDate) {

        if (serviceDate == null || serviceDate.trim().isEmpty()) {
            return false;
        }

        try {

            SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            );

            sdf.setLenient(false);

            Date bookingDate = sdf.parse(serviceDate);

            // Get today's date without time
            Date today = sdf.parse(
                    sdf.format(new Date())
            );

            return bookingDate != null && bookingDate.before(today);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}