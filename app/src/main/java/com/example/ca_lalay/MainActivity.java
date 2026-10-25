package com.example.ca_lalay;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.Session.SessionManager;
import com.example.ca_lalay.activities.customer.CustomerDashboard;
import com.example.ca_lalay.activities.shopowner.ShopOwnerDashboard;
import com.example.ca_lalay.auth.LoginActivity;
import com.example.ca_lalay.auth.RegisterUserActivity;
import com.example.ca_lalay.auth.RoleSelection;

public class MainActivity extends AppCompatActivity {
    private TextView loginLink;
    private Button continueBtn;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        loginLink = findViewById(R.id.loginLink);
        continueBtn = findViewById(R.id.continueBtn);


        sessionManager = new SessionManager(this);
        navigateUser();
        if (sessionManager.isLoggedIn()) {
            redirectBasedOnRole(sessionManager.getUserRole());
            finish();
            return;
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



    }
    private void redirectBasedOnRole(String role) {
        if (role.equals("customer")) {
            navigateTo(CustomerDashboard.class);
        } else if (role.equals("shop_owner") ) {
            navigateTo(ShopOwnerDashboard.class);
        } else if (role.equals("freelancer")) {
            navigateTo(ShopOwnerDashboard.class);
        }
    }
    private void navigateTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        startActivity(intent);
    }
    private void navigateUser() {
        loginLink.setOnClickListener(s -> {
            Intent ss = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(ss);
        });

        continueBtn.setOnClickListener(s -> {
            Intent reg = new Intent(MainActivity.this, RoleSelection.class);
            startActivity(reg);
        });
    }
}