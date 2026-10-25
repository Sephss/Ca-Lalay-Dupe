package com.example.ca_lalay.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.MainActivity;
import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.SessionManager;
import com.example.ca_lalay.firebase.FirebaseAuthManager;

public class PendingRegistration extends AppCompatActivity {
    private Button btnContinue;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pending_registration);

        btnContinue = findViewById(R.id.btnContinue);
        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnContinue.setOnClickListener(s -> {
            Intent intent = new Intent(PendingRegistration.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        logoutSession();
    }
    private void logoutSession() {
        FirebaseAuthManager.logout();
        sessionManager.logout();
    }
}