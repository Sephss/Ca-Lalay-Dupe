package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ca_lalay.R;
import com.example.ca_lalay.activities.adapter.ConversationAdapter;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseChatHelper;
import com.example.ca_lalay.firebase.callback.ConversationFetchCallback;
import com.example.ca_lalay.model.ConversationModel;

import java.util.ArrayList;
import java.util.List;

public class CustomerMessages extends AppCompatActivity {
    private RecyclerView conversationRV;
    private List<ConversationModel> list;
    private ConversationAdapter adapter;
    private ImageView goToHome, goToProfile;

    private TextView searchConvo;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_messages);

        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        finish();

                        overridePendingTransition(
                                R.anim.fast_fade_in,
                                R.anim.fast_fade_out
                        );
                    }
                });

        goToProfile = findViewById(R.id.goToProfile);
        goToHome = findViewById(R.id.goToHome);
        conversationRV = findViewById(R.id.conversationRV);
        searchConvo = findViewById(R.id.searchConvo);

        currentUserId = FirebaseAuthManager.getCurrentUserUid();
        list = new ArrayList<>();

        adapter = new ConversationAdapter(
                list,
                currentUserId
        );

        conversationRV.setLayoutManager(
                new LinearLayoutManager(this)
        );

        conversationRV.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fetchConversations();
        goToHome.setOnClickListener(s -> {
            Intent intent = new Intent(CustomerMessages.this, CustomerDashboard.class);
            startActivity(intent);
            finish();
            overridePendingTransition(
                    R.anim.fast_fade_in,
                    R.anim.fast_fade_out
            );
        });

        goToProfile.setOnClickListener(s -> {
            Intent intent = new Intent(CustomerMessages.this, CustomerProfile.class);
            startActivity(intent);
            finish();
            overridePendingTransition(
                    R.anim.fast_fade_in,
                    R.anim.fast_fade_out
            );
        });

        // ==========================================
        // SEARCH CONVERSATIONS BY NAME
        // ==========================================

        searchConvo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void fetchConversations() {

        FirebaseChatHelper.fetchConversations(
                currentUserId,
                new ConversationFetchCallback() {

                    @Override
                    public void onSuccess(List<ConversationModel> conversations) {

                        list.clear();

                        list.addAll(conversations);

                        adapter.setFullList(conversations);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(String message) {

                    }
                });
    }
}