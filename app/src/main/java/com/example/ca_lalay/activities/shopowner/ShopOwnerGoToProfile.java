package com.example.ca_lalay.activities.shopowner;

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
import com.example.ca_lalay.activities.customer.CustomerChangePassword;
import com.example.ca_lalay.activities.customer.CustomerMessages;
import com.example.ca_lalay.activities.customer.CustomerProfile;
import com.example.ca_lalay.auth.LoginActivity;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.squareup.picasso.Picasso;

public class ShopOwnerGoToProfile extends AppCompatActivity {
private ImageView theProfile, goToHome, goToMessage;
private TextView customerName, logoutBtn, editProfileLink, changePasswordLink, onlinePaymentDetailsLink;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_go_to_profile);

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

        customerName = findViewById(R.id.customerName);
        theProfile = findViewById(R.id.theProfile);
        editProfileLink = findViewById(R.id.editProfileLink);
        goToHome = findViewById(R.id.goToHome);
        goToMessage = findViewById(R.id.goToMessages);
        logoutBtn = findViewById(R.id.logoutBtn);
        onlinePaymentDetailsLink = findViewById(R.id.onlinePaymentDetailsLink);
        changePasswordLink = findViewById(R.id.changePasswordLink);

        sessionManager = new SessionManager(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUp();

        logoutBtn.setOnClickListener(s -> {
            FirebaseAuthManager.logout();
            ShopOwnerSession.getInstance().clearSession();
            UserSession.getInstance().clearSession();
            sessionManager.logout();
            Intent intent = new Intent(ShopOwnerGoToProfile.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        editProfileLink.setOnClickListener(d -> {
            Intent intent = new Intent(ShopOwnerGoToProfile.this, ShopOwnerEditProfilePage.class);
            startActivity(intent);
        });
        goToHome.setOnClickListener(d -> {
            Intent intent = new Intent(ShopOwnerGoToProfile.this, ShopOwnerDashboard.class);
            startActivity(intent);
            overridePendingTransition(
                    R.anim.fast_fade_in,
                    R.anim.fast_fade_out
            );
        });

        onlinePaymentDetailsLink.setOnClickListener(d -> {
            Intent intent = new Intent(ShopOwnerGoToProfile.this, ShopOwnerOnlinePaymentDetails.class);
            startActivity(intent);
        });

        changePasswordLink.setOnClickListener(s -> {
            Intent intent = new Intent(ShopOwnerGoToProfile.this, CustomerChangePassword.class);
            startActivity(intent);
        });
            goToMessage.setOnClickListener(s -> {
            Intent intent = new Intent(ShopOwnerGoToProfile.this, ShopOwnerMessages.class);
                startActivity(intent);
                overridePendingTransition(
                        R.anim.fast_fade_in,
                        R.anim.fast_fade_out
                );

});
    }
    private void setUp() {
        String uid = FirebaseAuthManager.getCurrentUserUid();

        FirebaseDatabaseManager.getShopOwnerData(uid, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel data) {

                Picasso.get().load(data.getShopImage()).placeholder(R.drawable.sent_image_placeholder).into(theProfile);
                customerName.setText(data.getShopName());
            }

            @Override
            public void onFailure(String failed) {

            }
        });
    }
}