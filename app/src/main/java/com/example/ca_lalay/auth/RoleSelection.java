package com.example.ca_lalay.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;

public class RoleSelection extends AppCompatActivity {
    private LinearLayout customerRole, shopOwnerRole, freelancerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_role_selection);

        customerRole = findViewById(R.id.customerRole);
        shopOwnerRole = findViewById(R.id.shopOwnerRole);
        freelancerRole = findViewById(R.id.freelancerRole);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        customerRole.setOnClickListener(s -> {
            Intent intent = new Intent(RoleSelection.this, RegisterUserActivity.class);
            intent.putExtra("role", "customer");
            startActivity(intent);
        });

        shopOwnerRole.setOnClickListener(s -> {
            Intent intent = new Intent(RoleSelection.this, ShopOwnerOrFreelancerRegistration.class);
            intent.putExtra("role", "shop_owner");
            startActivity(intent);
        });

        freelancerRole.setOnClickListener(s -> {
            Intent intent = new Intent(RoleSelection.this, ShopOwnerOrFreelancerRegistration.class);
            intent.putExtra("role", "freelancer");
            startActivity(intent);
        });
    }
}