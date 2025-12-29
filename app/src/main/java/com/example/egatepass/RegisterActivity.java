package com.example.egatepass;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirm, etEnrollment, etMobile;
    private AutoCompleteTextView spDepartment, spYear;
    private CheckBox cbTerms;
    private Button btnDoRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirm = findViewById(R.id.etRegConfirmPassword);
        etEnrollment = findViewById(R.id.etEnrollment);
        etMobile = findViewById(R.id.etMobile);
        spDepartment = findViewById(R.id.spDepartment);
        spYear = findViewById(R.id.spYear);
        cbTerms = findViewById(R.id.cbTerms);
        btnDoRegister = findViewById(R.id.btnDoRegister);

        auth = FirebaseHelper.getAuth();
        db = FirebaseHelper.getFirestore();

        String[] depts = {"Select Department", "CSE", "IT", "ECE", "EE", "MECH", "CIVIL"};
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, depts);
        spDepartment.setAdapter(deptAdapter);

        String[] years = {"Select Year", "FY", "SY", "TY", "Final Year"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years);
        spYear.setAdapter(yearAdapter);

        btnDoRegister.setOnClickListener(v -> register());
    }

    private void register() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirm = etConfirm.getText() != null ? etConfirm.getText().toString().trim() : "";
        String enrollment = etEnrollment.getText() != null ? etEnrollment.getText().toString().trim() : "";
        String mobile = etMobile.getText() != null ? etMobile.getText().toString().trim() : "";
        String department = spDepartment.getText().toString().trim();
        String year = spYear.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Enter full name"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Enter email"); return; }
        if (TextUtils.isEmpty(pass)) { etPassword.setError("Enter password"); return; }
        if (!pass.equals(confirm)) { etConfirm.setError("Passwords do not match"); return; }
        if (!cbTerms.isChecked()) { Toast.makeText(this, "Agree to terms", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(enrollment)) { etEnrollment.setError("Enter enrollment number"); return; }
        if (department.equals("Select Department")) { Toast.makeText(this, "Select department", Toast.LENGTH_SHORT).show(); return; }
        if (year.equals("Select Year")) { Toast.makeText(this, "Select year", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(mobile)) { etMobile.setError("Enter mobile"); return; }

        btnDoRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    btnDoRegister.setEnabled(true);
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("email", email);
                        userMap.put("name", name);
                        userMap.put("enrollmentNo", enrollment);
                        userMap.put("department", department);
                        userMap.put("year", year);
                        userMap.put("mobile", mobile);
                        userMap.put("role", "student");
                        userMap.put("createdAt", System.currentTimeMillis());

                        db.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this, "Profile save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                    finish();
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void goToLogin(View view)
    {
        startActivity(new Intent(this, LoginActivity.class));
    }
}
