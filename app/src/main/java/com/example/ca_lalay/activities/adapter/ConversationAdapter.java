package com.example.ca_lalay.activities.adapter;

import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.customer.CustomerMessageNow;
import com.example.ca_lalay.model.ConversationModel;
import com.example.ca_lalay.utils.ProfanityFilter;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<ConversationModel> list;
    private List<ConversationModel> fullList;
    private String currentUserId;

    public ConversationAdapter(List<ConversationModel> list, String currentUserId) {
        this.list = list;
        this.currentUserId = currentUserId;
        this.fullList = new ArrayList<>(list);
    }

    // ==========================================
    // KEEP THE UNFILTERED LIST IN SYNC
    // (call this whenever the source data changes,
    // e.g. after a fresh fetch from Firebase)
    // ==========================================

    public void setFullList(List<ConversationModel> conversations) {
        this.fullList = new ArrayList<>(conversations);
    }

    // ==========================================
    // FILTER BY CONVERSATION NAME
    // (uses the same "opposite user" name logic
    // as onBindViewHolder)
    // ==========================================

    public void filter(String query) {

        list.clear();

        if (query == null || query.trim().isEmpty()) {

            list.addAll(fullList);

        } else {

            String lower = query.toLowerCase(Locale.ROOT).trim();

            for (ConversationModel convo : fullList) {

                String name;

                if (convo.getCustomerId().equals(currentUserId)) {
                    name = convo.getShopName();
                } else {
                    name = convo.getCustomerName();
                }

                if (name != null && name.toLowerCase(Locale.ROOT).contains(lower)) {
                    list.add(convo);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ConversationModel convo = list.get(position);

        String name;
        String imageUrl;
        String otherId;
        String filteredMessage =
                ProfanityFilter.filter(convo.getLastMessage());

        // Reset views first because RecyclerView reuses ViewHolders
        holder.lastMessageText.setVisibility(View.VISIBLE);
        holder.likeIcon.setVisibility(View.GONE);

        holder.lastMessageTime.setText(
                formatMessageTime(convo.getLastTimestamp())
        );

        String lastMessage = convo.getLastMessage();

        if (lastMessage != null && !lastMessage.isEmpty()) {

            if (lastMessage.equals("like")) {

                holder.lastMessageText.setVisibility(View.GONE);
                holder.likeIcon.setVisibility(View.VISIBLE);

            } else if (isImageUrl(lastMessage)) {

                // ==========================================
                // LAST MESSAGE WAS AN IMAGE
                // ==========================================

                holder.lastMessageText.setSingleLine(true);
                holder.lastMessageText.setEllipsize(TextUtils.TruncateAt.END);
                holder.lastMessageText.setText("📷 Photo");

            } else {

                // Only show the first line of the message
                String previewMessage = filteredMessage.split("\\r?\\n")[0];
                holder.lastMessageText.setSingleLine(true);
                holder.lastMessageText.setEllipsize(TextUtils.TruncateAt.END);
                holder.lastMessageText.setText(previewMessage);
            }

        } else {

            holder.lastMessageText.setText("");
        }

        boolean isCustomerView;

        // show opposite user
        if (convo.getCustomerId().equals(currentUserId)) {

            // CUSTOMER VIEW → show SHOP
            isCustomerView = true;
            name = convo.getShopName();
            imageUrl = convo.getShopImage();
            otherId = convo.getShopId();

        } else {

            // SHOP VIEW → show CUSTOMER
            isCustomerView = false;
            name = convo.getCustomerName();
            imageUrl = convo.getCustomerImage();
            otherId = convo.getCustomerId();
        }

        holder.nameText.setText(name);

        // ==========================================
        // UNREAD DOT
        // ==========================================

        boolean isRead;

        if (isCustomerView) {
            isRead = convo.isReadByCustomer();
        } else {
            isRead = convo.isReadByShop();
        }

        if (isRead) {
            holder.unreadDot.setVisibility(View.INVISIBLE);
            // Reset read conversation style
            holder.lastMessageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.darkergrey)
            );

            holder.lastMessageTime.setTypeface(null, Typeface.NORMAL);
            holder.nameText.setTypeface(null, Typeface.NORMAL);
            holder.lastMessageText.setTypeface(null, Typeface.NORMAL);

        } else {
            holder.unreadDot.setVisibility(View.VISIBLE);
            holder.lastMessageTime.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red)

            );
            holder.lastMessageTime.setTypeface(null, Typeface.BOLD);
            holder.nameText.setTypeface(null, Typeface.BOLD);
            holder.lastMessageText.setTypeface(null, Typeface.BOLD);
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .resize(100, 100)
                    .centerCrop()
                    .placeholder(R.drawable.sent_image_placeholder)
                    .error(R.drawable.sent_image_placeholder)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(
                    R.drawable.sent_image_placeholder
            );
        }


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CustomerMessageNow.class);
            intent.putExtra("customerId", convo.getCustomerId());
            intent.putExtra("customerName", convo.getCustomerName());
            intent.putExtra("customerImage", convo.getCustomerImage());

            intent.putExtra("shopId", convo.getShopId());
            intent.putExtra("shopName", convo.getShopName());
            intent.putExtra("shopImage", convo.getShopImage());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameText, lastMessageText, lastMessageTime;
        ImageView profileImage, likeIcon;
        View unreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.nameText);
            lastMessageText = itemView.findViewById(R.id.lastMessageText);
            profileImage = itemView.findViewById(R.id.profileImage);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            lastMessageTime = itemView.findViewById(R.id.lastMessageTime);
            unreadDot = itemView.findViewById(R.id.unreadDot);
        }
    }

    // ==========================================
    // DETECT IF LAST MESSAGE WAS AN IMAGE
    // (no messageType field available, so we
    // detect based on it being a URL)
    // ==========================================

    private boolean isImageUrl(String message) {

        if (message == null) {
            return false;
        }

        String lower = message.toLowerCase(Locale.ROOT);

        return lower.startsWith("http://")
                || lower.startsWith("https://");
    }

    // ==========================================
    // FORMAT TIMESTAMP
    // ==========================================

    private String formatMessageTime(String timestamp) {

        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }

        try {

            long millis = Long.parseLong(timestamp);
            long now = System.currentTimeMillis();
            long diff = now - millis;

            if (isToday(millis)) {

                // ==========================================
                // LESS THAN 1 MINUTE -> "Just now"
                // ==========================================

                if (diff < DateUtils.MINUTE_IN_MILLIS) {
                    return "Just now";
                }

                // ==========================================
                // 1 MINUTE OR MORE -> "X minutes/hours ago"
                // ==========================================

                CharSequence relative =
                        DateUtils.getRelativeTimeSpanString(
                                millis,
                                now,
                                DateUtils.MINUTE_IN_MILLIS
                        );

                return relative.toString();
            }

            // ==========================================
            // NOT TODAY -> FULL DATE/TIME
            // ==========================================

            Date date = new Date(millis);

            SimpleDateFormat sdf =
                    new SimpleDateFormat("MMM dd - hh:mm a", Locale.getDefault());

            return sdf.format(date);

        } catch (NumberFormatException e) {
            return "";
        }
    }

    // ==========================================
    // CHECK IF TIMESTAMP IS FROM TODAY
    // ==========================================

    private boolean isToday(long millis) {

        Calendar messageCal = Calendar.getInstance();
        messageCal.setTimeInMillis(millis);

        Calendar todayCal = Calendar.getInstance();

        return messageCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)
                && messageCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR);
    }
}