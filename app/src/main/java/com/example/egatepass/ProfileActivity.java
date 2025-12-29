package com.example.egatepass;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    TextInputEditText etName, etEnrollment, etDepartment, etYear, etMobile;
    Button btnSave;
    FirebaseFirestore db;
    String uid;

    private static final String PROFILE_COLLECTION = "users";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        etEnrollment = findViewById(R.id.etEnrollment);
        etDepartment = findViewById(R.id.etDepartment);
        etYear = findViewById(R.id.etYear);
        etMobile = findViewById(R.id.etMobile);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile(){
        if(uid==null) return;

        db.collection(PROFILE_COLLECTION).document(uid).get().addOnSuccessListener(doc -> {
            if(doc.exists()){
                etName.setText(doc.getString("name"));
                etEnrollment.setText(doc.getString("enrollmentNo"));
                etDepartment.setText(doc.getString("department"));
                etYear.setText(doc.getString("year"));
                etMobile.setText(doc.getString("mobile"));
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfile(){
        if(uid==null) return;

        Map<String,Object> map = new HashMap<>();
        map.put("name", etName.getText().toString());
        map.put("enrollmentNo", etEnrollment.getText().toString());
        map.put("department", etDepartment.getText().toString());
        map.put("year", etYear.getText().toString());
        map.put("mobile", etMobile.getText().toString());

        db.collection(PROFILE_COLLECTION).document(uid).set(map, SetOptions.merge()).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
