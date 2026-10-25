package com.example.ca_lalay.activities.shopowner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseOnlinePaymentDetailsManager;
import com.example.ca_lalay.model.OnlinePaymentDetailsModel;

public class ShopOwnerOnlinePaymentDetails extends AppCompatActivity {
    private ImageView backBtn;
    private Button saveBtn;
    private EditText paymentDetails;
     private String uid;
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_online_payment_details);

        backBtn = findViewById(R.id.backBtn);
        saveBtn = findViewById(R.id.saveBtn);
        paymentDetails = findViewById(R.id.paymentDetails);
        uid = FirebaseAuthManager.getCurrentUserUid();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getDetails();

        backBtn.setOnClickListener(s -> {
            finish();
        });

        saveBtn.setOnClickListener(s -> {
            saveOnlinePaymentDetails();
        });
    }
    private void saveOnlinePaymentDetails() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String details = paymentDetails.getText().toString().trim();

        if(details.isEmpty()) {
            paymentDetails.setError("Please enter online payment details");
            paymentDetails.requestFocus();
            return;
        }

        setLoadingState();

        FirebaseOnlinePaymentDetailsManager.saveOnlinePaymentDetails(uid, details, new FirebaseOnlinePaymentDetailsManager.OnSaveOnlinePaymentDetailsListener() {
            @Override
            public void onSuccess() {
                setNormalState();
                Toast.makeText(ShopOwnerOnlinePaymentDetails.this, "Online payment details saved successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                setNormalState();
                Toast.makeText(ShopOwnerOnlinePaymentDetails.this, "Failed to save online payment details: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getDetails() {
        FirebaseOnlinePaymentDetailsManager.getOnlinePaymentDetails(uid, new FirebaseOnlinePaymentDetailsManager.OnGetOnlinePaymentDetailsListener() {
            @Override
            public void onSuccess(OnlinePaymentDetailsModel model) {
                paymentDetails.setText(model.getDetails());
            }

            @Override
            public void onNotFound() {

            }

            @Override
            public void onFailure(String error) {

            }
        });
    }
    private void setLoadingState() {
        saveBtn.setEnabled(false);
        saveBtn.setAlpha(0.5f);
        saveBtn.setText("SAVING...");
    }

    private void setNormalState() {
        saveBtn.setEnabled(true);
        saveBtn.setAlpha(1f);
        saveBtn.setText("SAVE PAYMENT DETAILS");
    }
}