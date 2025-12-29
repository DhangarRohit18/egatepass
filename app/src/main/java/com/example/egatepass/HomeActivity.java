package com.example.egatepass;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btnDailyPass).setOnClickListener(v ->
                startActivity(new Intent(this, CreateGatepassActivity.class))
        );
        findViewById(R.id.btnRequests).setOnClickListener(v ->
                startActivity(new Intent(this, MyRequestsActivity.class))
        );
        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
        findViewById(R.id.btnShowQr).setOnClickListener(v -> {
            startActivity(new Intent(this, MyRequestsActivity.class));
        });
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            RolePref.clear(this);
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}
