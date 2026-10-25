package com.example.ca_lalay.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.SessionManager;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.auth.LoginActivity;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.callback.CustomerDataCallback;
import com.example.ca_lalay.model.UserModel;
import com.squareup.picasso.Picasso;

public class CustomerProfile extends AppCompatActivity {
    private TextView editProfileLink, logoutBtn, customerName, myVehicle, changePasswordLink;
    private SessionManager sessionManager;
    private ImageView profileImage, goToHome, goToMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_profile);

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

        sessionManager = new SessionManager(this);

        profileImage = findViewById(R.id.theProfile);
        customerName = findViewById(R.id.customerName);

        goToHome = findViewById(R.id.goToHome);
        goToMessage = findViewById(R.id.goToMessages);
        editProfileLink = findViewById(R.id.editProfileLink);
        changePasswordLink = findViewById(R.id.changePasswordLink);

        logoutBtn = findViewById(R.id.logoutBtn);
        myVehicle = findViewById(R.id.myVehicle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logoutBtn.setOnClickListener(d -> {
            FirebaseAuthManager.logout();
            UserSession.getInstance().clearSession();
            ShopOwnerSession.getInstance().clearSession();
            sessionManager.logout();
            Intent intent = new Intent(CustomerProfile.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        myVehicle.setOnClickListener(f -> {
            Intent intent = new Intent(CustomerProfile.this, CustomerMyVehicles.class);
            startActivity(intent);
        });
        setUp();

        goToMessage.setOnClickListener(s -> {
            Intent intent = new Intent(CustomerProfile.this, CustomerMessages.class);
            startActivity(intent);
            overridePendingTransition(
                    R.anim.fast_fade_in,
                    R.anim.fast_fade_out
            );
        });

        goToHome.setOnClickListener(s -> {
            Intent intent = new Intent(CustomerProfile.this, CustomerDashboard.class);
            startActivity(intent);
            overridePendingTransition(
                    R.anim.fast_fade_in,
                    R.anim.fast_fade_out
            );
        });

        changePasswordLink.setOnClickListener(s -> {
            Intent goToChangePassword = new Intent(CustomerProfile.this, CustomerChangePassword.class);
            startActivity(goToChangePassword);
        });

        editProfileLink.setOnClickListener(d -> {
            Intent editProfile = new Intent(CustomerProfile.this, CustomerEditProfile.class);
            startActivity(editProfile);
        });
    }
    private void setUp() {
        String uid = FirebaseAuthManager.getCurrentUserUid();

        FirebaseDatabaseManager.getCustomerData(uid, new CustomerDataCallback() {
            @Override
            public void onSuccess(String message, UserModel customerData) {
                Picasso.get().load(customerData.getImageUrl()).placeholder(R.drawable.sent_image_placeholder).into(profileImage);
                customerName.setText(customerData.getFirstname() + " " + customerData.getLastname());
            }

            @Override
            public void onFailure(String message) {

            }
        });
    }
}