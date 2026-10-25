package com.example.ca_lalay.activities.adapter;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.model.ChatModel;
import com.example.ca_lalay.utils.ProfanityFilter;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatModel> messageList;
    private String currentUserId;
    private String otherUserImage;

    public ChatAdapter(
            List<ChatModel> messageList,
            String currentUserId,
            String otherUserImage
    ) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.otherUserImage = otherUserImage;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ChatViewHolder holder,
            int position
    ) {

        ChatModel chat = messageList.get(position);

        LinearLayout container =
                (LinearLayout) holder.itemView;

        // ==========================================
        // RESET VIEWS
        // ==========================================

        holder.messageText.setVisibility(View.VISIBLE);
        holder.messageImage.setVisibility(View.GONE);
        holder.likeIcon.setVisibility(View.GONE);
        holder.profileImage.setVisibility(View.GONE);

        // IMPORTANT
        // RecyclerView reuses ViewHolders
        holder.lastMessageTime.setVisibility(View.GONE);
        holder.lastMessageTime.setText("");
        String filteredMessage =
                ProfanityFilter.filter(chat.getMessage());

        // ==========================================
        // CHECK MESSAGE TYPE
        // ==========================================

        if ("image".equals(chat.getMessageType())) {

            holder.messageText.setVisibility(View.GONE);
            holder.likeIcon.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);

            Picasso.get()
                    .load(chat.getMessage())
                    .resize(600, 600)
                    .centerInside()
                    .placeholder(R.drawable.sent_image_placeholder)
                    .error(R.drawable.sent_image_placeholder)
                    .into(holder.messageImage);


            // ==========================================
            // TAP TO VIEW FULL SCREEN
            // ==========================================

            holder.messageImage.setOnClickListener(v ->
                    openFullScreenImage(v.getContext(), chat.getMessage())
            );

        } else if ("text".equals(chat.getMessageType())) {

            holder.messageImage.setVisibility(View.GONE);
            holder.messageImage.setOnClickListener(null);

            // ==========================================
            // CHECK IF LIKE (only relevant for text messages)
            // ==========================================

            boolean isLike =
                    "like".equals(chat.getMessage());

            if (isLike) {

                holder.messageText.setVisibility(View.GONE);
                holder.likeIcon.setVisibility(View.VISIBLE);

            } else {

                holder.messageText.setVisibility(View.VISIBLE);
                holder.likeIcon.setVisibility(View.GONE);

                holder.messageText.setText(
                        filteredMessage
                );
            }
        }

        // ==========================================
        // CHECK MESSAGE OWNER
        // ==========================================

        boolean isMine =
                chat.getSenderId().equals(currentUserId);

        // ==========================================
        // CHECK LAST MESSAGE IN STACK
        // ==========================================

        boolean isLastInGroup =
                isLastMessageInGroup(position);

        // ==========================================
        // SHOW DATE/TIME ONLY ON LAST MESSAGE
        // ==========================================

        if (isLastInGroup) {

            String timestamp =
                    chat.getTimestamp();

            if (timestamp != null
                    && !timestamp.isEmpty()) {

                holder.lastMessageTime.setText(
                        formatMessageTime(timestamp)
                );

                holder.lastMessageTime.setVisibility(
                        View.VISIBLE
                );
            }
        }

        // ==========================================
        // MY MESSAGE
        // ==========================================

        if (isMine) {

            container.setGravity(Gravity.END);

            holder.messageText.setBackgroundResource(
                    R.drawable.bg_chat_right
            );


            // No profile on my messages
            holder.profileImage.setVisibility(
                    View.GONE
            );
            holder.lastMessageTime.setVisibility(View.GONE);

        }

        // ==========================================
        // OTHER USER MESSAGE
        // ==========================================

        else {

            container.setGravity(Gravity.START);

            holder.messageText.setBackgroundResource(
                    R.drawable.bg_chat_left
            );

            /*
             * Profile only appears on the
             * last message of the stack.
             */

            if (isLastInGroup) {

                holder.profileImage.setVisibility(
                        View.VISIBLE
                );

                if (otherUserImage != null
                        && !otherUserImage.isEmpty()) {

                    Picasso.get()
                            .load(otherUserImage)
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

            } else {

                /*
                 * INVISIBLE instead of GONE keeps
                 * the messages aligned.
                 */
                holder.profileImage.setVisibility(
                        View.INVISIBLE
                );
            }
        }
    }

    // ==========================================
    // FULL SCREEN IMAGE VIEWER (Messenger-style)
    // ==========================================

    private void openFullScreenImage(android.content.Context context, String imageUrl) {

        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        PhotoView photoView = new PhotoView(context);
        photoView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        Picasso.get()
                .load(imageUrl)
                .into(photoView);

        dialog.setContentView(photoView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.BLACK)
            );
        }

        // Tap anywhere to dismiss
        photoView.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ==========================================
    // LAST MESSAGE IN STACK
    // ==========================================

    private boolean isLastMessageInGroup(int position) {

        // Last message in conversation
        if (position == messageList.size() - 1) {
            return true;
        }

        ChatModel currentMessage =
                messageList.get(position);

        ChatModel nextMessage =
                messageList.get(position + 1);

        /*
         * Different sender = end of stack
         */

        return !currentMessage
                .getSenderId()
                .equals(nextMessage.getSenderId());
    }

    // ==========================================
    // FORMAT TIMESTAMP
    // ==========================================

    private String formatMessageTime(String timestamp) {

        try {

            /*
             * Example Firebase value:
             *
             * 1788430198634
             *
             * This is milliseconds.
             */

            long millis =
                    Long.parseLong(timestamp);

            long now =
                    System.currentTimeMillis();

            long diff =
                    now - millis;

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

            Date date =
                    new Date(millis);

            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "MMM dd - hh:mm a",
                            Locale.getDefault()
                    );

            return sdf.format(date);

        } catch (Exception e) {

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

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ==========================================
    // VIEW HOLDER
    // ==========================================

    public static class ChatViewHolder
            extends RecyclerView.ViewHolder {

        TextView messageText;
        TextView lastMessageTime;

        ImageView profileImage;
        ImageView likeIcon, messageImage;

        public ChatViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            messageText =
                    itemView.findViewById(
                            R.id.messageText
                    );

            profileImage =
                    itemView.findViewById(
                            R.id.profileImage
                    );

            likeIcon =
                    itemView.findViewById(
                            R.id.likeIcon
                    );

            lastMessageTime =
                    itemView.findViewById(
                            R.id.lastMessageTime
                    );
            messageImage = itemView.findViewById(R.id.messageImage);
        }
    }
}