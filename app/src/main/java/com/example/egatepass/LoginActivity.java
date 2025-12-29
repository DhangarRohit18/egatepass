package com.example.egatepass;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private TextView tvForgot;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseHelper.getAuth();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgot = findViewById(R.id.tvForgot);

        btnRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

        btnLogin.setEnabled(false);

        // Hardcoded admin bypass
        if (email.equals("admin@gmail.com") && password.equals("admin1")) {
            btnLogin.setEnabled(true);
            RolePref.setEmail(LoginActivity.this, email);
            RolePref.setRole(LoginActivity.this, "admin");
            Toast.makeText(LoginActivity.this, "Logged in as Admin", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
            finish();
            return;
        }

        // Firebase user login
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            RolePref.setEmail(LoginActivity.this, user.getEmail());
                            RolePref.setRole(LoginActivity.this, "student");
                            Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: user null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Auth failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "unknown"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        String email = user != null ? user.getEmail() : null;
        String role = RolePref.getRole(this);

        if (role != null && email != null) {
            if ("admin".equals(role)) {
                startActivity(new Intent(this, AdminHomeActivity.class));
                finish();
                return;
            } else if ("student".equals(role)) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return;
            }
        }

        // If logged in via Firebase
        if (email != null) {
            if ("admin@gmail.com".equals(email)) {
                RolePref.setRole(this, "admin");
                RolePref.setEmail(this, email);
                startActivity(new Intent(this, AdminHomeActivity.class));
            } else {
                RolePref.setRole(this, "student");
                RolePref.setEmail(this, email);
                startActivity(new Intent(this, HomeActivity.class));
            }
            finish();
        }
    }
}
