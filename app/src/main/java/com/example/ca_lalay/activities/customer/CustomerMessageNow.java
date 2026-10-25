package com.example.ca_lalay.activities.customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.ChatAdapter;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseChatHelper;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseStorageManager;
import com.example.ca_lalay.firebase.callback.ChatCallback;
import com.example.ca_lalay.firebase.callback.ConversationCreateCallback;
import com.example.ca_lalay.firebase.callback.CustomerDataCallback;
import com.example.ca_lalay.firebase.callback.MessageFetchCallback;
import com.example.ca_lalay.firebase.callback.RoleCalllback;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.model.ChatModel;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomerMessageNow extends AppCompatActivity {
    private EditText messageEditText;
    private ImageView sendBtn, likeBtn;

    private TextView messageReceiverName;
    private ImageView messageReceiverProfile;
    private String theReceiverImage, theReceiverName;

    private ImageView backBtn;

    private RecyclerView messagesRecyclerView;
    private ChatAdapter adapter;
    private List<ChatModel> messageList;

    private String currentUserId;

    private String customerId;
    private String customerName;
    private String customerImage;

    private String shopId;
    private String shopName;
    private String shopImage;

    private ValueEventListener messagesListener;

    private Uri imageUri;
    private ImageView selectImagePreview, selectImageBtn, reselectImage;
    private FrameLayout imagePreviewContainer;
    private ImageButton removeImageButton;
    private LinearLayout sendImageLayout, sendTextMessageLayout;
    private Button sendImageBtn;


    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    selectImagePreview.setImageURI(imageUri);
                    sendTextMessageLayout.setVisibility(View.GONE);
                    sendImageLayout.setVisibility(View.VISIBLE);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {

                        if (success && imageUri != null) {

                            selectImagePreview.setImageURI(imageUri);

                            sendTextMessageLayout.setVisibility(View.GONE);
                            sendImageLayout.setVisibility(View.VISIBLE);

                        } else {

                            imageUri = null;

                            Toast.makeText(
                                    CustomerMessageNow.this,
                                    "Failed to capture image.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );


    private String conversationId;
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_message_now);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            Insets ime = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            int bottom = Math.max(
                    systemBars.bottom,
                    ime.bottom
            );

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    bottom
            );

            return insets;
        });

        messageEditText = findViewById(R.id.messageEditText);
        sendBtn = findViewById(R.id.sendButton);
        likeBtn = findViewById(R.id.likeBtn);

        messageReceiverProfile = findViewById(R.id.messageReceiverProfile);
        messageReceiverName = findViewById(R.id.messageReceiverName);

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        backBtn = findViewById(R.id.backBtn);




        selectImageBtn = findViewById(R.id.selectImageBtn);
        selectImagePreview = findViewById(R.id.selectImagePreview);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        removeImageButton = findViewById(R.id.removeImageButton);
        sendImageLayout = findViewById(R.id.sendImageLayout);
        sendTextMessageLayout = findViewById(R.id.sendTextMessageLayout);
        reselectImage = findViewById(R.id.reselectImage);
        sendImageBtn = findViewById(R.id.sendImageBtn);


        messageList = new ArrayList<>();

        currentUserId = FirebaseAuthManager.getCurrentUserUid();

        Intent intent = getIntent();
        customerId = intent.getStringExtra("customerId");
        customerName = intent.getStringExtra("customerName");
        customerImage = intent.getStringExtra("customerImage");

        shopId = intent.getStringExtra("shopId");
        shopName = intent.getStringExtra("shopName");
        shopImage = intent.getStringExtra("shopImage");
        conversationId =
                FirebaseChatHelper.getConversationId(
                        customerId,
                        shopId
                );
        String otherUserImage;

        if (currentUserId.equals(customerId)) {
            otherUserImage = shopImage;
        } else {
            otherUserImage = customerImage;
        }

        adapter = new ChatAdapter(
                messageList,
                currentUserId,
                otherUserImage
        );

        messagesRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        messagesRecyclerView.setAdapter(adapter);

        messageEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                boolean isEmpty = s.toString().trim().isEmpty();

                if (isEmpty) {
                 sendBtn.setVisibility(View.GONE);
                 likeBtn.setVisibility(View.VISIBLE);
                } else {
                    sendBtn.setVisibility(View.VISIBLE);
                    likeBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        fetchReceiverDetails();
        fetchMessages();
        sendBtn.setOnClickListener(d -> {
            String message = messageEditText.getText().toString().trim();
            if (message.isEmpty()) {
                return;
            }
            sendMessage(message, "text");
        });
        likeBtn.setOnClickListener(ds -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                sendMessage("like", "text");
            }, 500);
        });

        backBtn.setOnClickListener(s -> {
            finish();
        });

        selectImageBtn.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });
        reselectImage.setOnClickListener(s -> {
            choosePhotoFromGallery();
        });

        removeImageButton.setOnClickListener(v -> {
            sendImageLayout.setVisibility(View.GONE);
            sendTextMessageLayout.setVisibility(View.VISIBLE);
            // Clear the selected image URI
            imageUri = null;

            // Optional: clear the ImageView as well
            selectImagePreview.setImageDrawable(null);
        });

        sendImageBtn.setOnClickListener(s -> {
           sendImageMessage();
        });


    }


    private void sendImageMessage() {
        if (imageUri != null) {
            setLoadingState();
            sendImageBtn.setEnabled(false);
            FirebaseStorageManager.uploadImage("MessageImages", imageUri, new FirebaseStorageManager.ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    sendMessage(imageUrl, "image");

                    sendImageLayout.setVisibility(View.GONE);
                    sendTextMessageLayout.setVisibility(View.VISIBLE);
                    // Clear the selected image URI
                    imageUri = null;

                    // Optional: clear the ImageView as well
                    selectImagePreview.setImageDrawable(null);
                }

                @Override
                public void onFailure(String errorMessage) {
                    setNormalState();
                    Toast.makeText(CustomerMessageNow.this, errorMessage, Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(CustomerMessageNow.this, "Please select an image first.", Toast.LENGTH_SHORT).show();
        }
    }
    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }
    private void fetchReceiverDetails() {

        if (currentUserId.equals(customerId)) {

            // Customer → chatting with Shop
            messageReceiverName.setText(shopName);

            if (shopImage != null && !shopImage.isEmpty()) {
                Picasso.get()
                        .load(shopImage)
                        .resize(100, 100)
                        .centerCrop()
                        .placeholder(R.drawable.sent_image_placeholder)
                        .error(R.drawable.sent_image_placeholder)
                        .into(messageReceiverProfile);

            } else {
                messageReceiverProfile.setImageResource(
                        R.drawable.sent_image_placeholder
                );
            }

        } else {

            // Shop → chatting with Customer
            messageReceiverName.setText(customerName);

            if (customerImage != null && !customerImage.isEmpty()) {
                Picasso.get()
                        .load(customerImage)
                        .resize(100, 100)
                        .centerCrop()
                        .placeholder(R.drawable.sent_image_placeholder)
                        .error(R.drawable.sent_image_placeholder)
                        .into(messageReceiverProfile);

            } else {
                messageReceiverProfile.setImageResource(
                        R.drawable.sent_image_placeholder
                );
            }
        }
    }

    private void fetchMessages() {

        messagesListener = FirebaseChatHelper.fetchMessages(conversationId, new MessageFetchCallback() {

            @Override
            public void onSuccess(List<ChatModel> chats) {

                messageList.clear();
                messageList.addAll(chats);

                adapter.notifyDataSetChanged();

                if (!messageList.isEmpty()) {
                    messagesRecyclerView.scrollToPosition(messageList.size() - 1);

                    FirebaseChatHelper.markConversationAsRead(conversationId, currentUserId.equals(customerId));
                }


            }

            @Override
            public void onFailure(String message) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (messagesListener != null) {
            FirebaseChatHelper.getMessagesDatabase()
                    .child(conversationId)
                    .removeEventListener(messagesListener);
        }
    }

    private void sendMessage(String message, String messageType) {
        sendBtn.setEnabled(false);
        sendImageBtn.setEnabled(false);
        FirebaseChatHelper.createConversationIfNotExists(

                customerId,
                customerName,
                customerImage,

                shopId,
                shopName,
                shopImage,

                new ConversationCreateCallback() {

                    @Override
                    public void onSuccess(String id) {

                        String receiverId =
                                currentUserId.equals(customerId)
                                        ? shopId
                                        : customerId;

                        FirebaseChatHelper.sendMessage(

                                id,
                                currentUserId,
                                receiverId,
                                message,
                                messageType,

                                new ChatCallback() {

                                    @Override
                                    public void onSuccess(String msg) {

                                        FirebaseChatHelper.updateLastMessage(
                                                id,
                                                message,
                                                currentUserId.equals(customerId)
                                        );
                                        messageEditText.setText("");
                                        sendBtn.setEnabled(true);
                                        setNormalState();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(CustomerMessageNow.this, error, Toast.LENGTH_SHORT).show();
                                        sendBtn.setEnabled(true);
                                     setNormalState();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(CustomerMessageNow.this, error, Toast.LENGTH_SHORT).show();
                        sendBtn.setEnabled(true);
                     setNormalState();
                    }
                });
    }
    private void setLoadingState() {
        sendImageBtn.setEnabled(false);
        sendImageBtn.setAlpha(0.5f);
        sendImageBtn.setText("SENDING...");
    }

    private void setNormalState() {
        sendImageBtn.setEnabled(true);
        sendImageBtn.setAlpha(1f);
        sendImageBtn.setText("SEND");
    }
}