package com.example.ca_lalay.auth;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.R;
import com.example.ca_lalay.Session.SessionManager;
import com.example.ca_lalay.Session.ShopOwnerSession;
import com.example.ca_lalay.Session.UserSession;
import com.example.ca_lalay.firebase.FirebaseAuthManager;

public class AccountSuspended extends AppCompatActivity {
private Button iUnderstandBtn;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_suspended);

        iUnderstandBtn = findViewById(R.id.iUnderstandBtn);
        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iUnderstandBtn.setOnClickListener(s -> {
            FirebaseAuthManager.logout();
            UserSession.getInstance().clearSession();
            ShopOwnerSession.getInstance().clearSession();
            sessionManager.logout();

            finishAffinity();
        });
    }
}