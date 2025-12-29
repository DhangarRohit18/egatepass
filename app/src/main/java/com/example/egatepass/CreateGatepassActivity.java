package com.example.egatepass;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateGatepassActivity extends AppCompatActivity {

    private TextInputEditText etReason, etDate, tvOutTime, tvInTime;
    private Button btnSubmit;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String selectedDate = "";
    private String selectedOutTime = "";
    private String selectedInTime = "";

    private final SimpleDateFormat fireDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_gatepass);

        etReason = findViewById(R.id.etReason);
        etDate = findViewById(R.id.etDate);
        tvOutTime = findViewById(R.id.tvOutTime);
        tvInTime = findViewById(R.id.tvInTime);
        btnSubmit = findViewById(R.id.btnSubmit);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Calendar c = Calendar.getInstance();
        selectedDate = fireDateFormat.format(c.getTime());
        etDate.setText(selectedDate);

        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(CreateGatepassActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar picked = Calendar.getInstance();
                        picked.set(year, month, dayOfMonth);
                        selectedDate = fireDateFormat.format(picked.getTime());
                        etDate.setText(selectedDate);
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvOutTime.setOnClickListener(v -> openTimePicker(true));
        tvInTime.setOnClickListener(v -> openTimePicker(false));

        btnSubmit.setOnClickListener(v -> submitGatepass());
    }

    private void openTimePicker(boolean isOut) {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            if (isOut) {
                selectedOutTime = time;
                tvOutTime.setText(time);
            } else {
                selectedInTime = time;
                tvInTime.setText(time);
            }
        }, h, m, true).show();
    }

    private void submitGatepass() {
        String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";
        String destination = "Local (Daily Pass)";

        if (TextUtils.isEmpty(reason)) {
            etReason.setError("Reason required");
            return;
        }
        if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(this, "Select date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedOutTime)) {
            Toast.makeText(this, "Select Out Time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            Map<String, Object> gp = new HashMap<>();
            gp.put("studentId", uid);
            gp.put("name", doc != null && doc.exists() ? doc.getString("name") : "");
            gp.put("enrollmentNo", doc != null && doc.exists() ? doc.getString("enrollmentNo") : "");
            gp.put("department", doc != null && doc.exists() ? doc.getString("department") : "");
            gp.put("year", doc != null && doc.exists() ? doc.getString("year") : "");
            gp.put("mobile", doc != null && doc.exists() ? doc.getString("mobile") : "");

            gp.put("type", "daily");
            gp.put("reason", reason);
            gp.put("fromDate", selectedDate);
            gp.put("toDate", selectedDate);
            gp.put("destination", destination);
            gp.put("date", selectedDate); // For compatibility
            gp.put("outTime", selectedOutTime);
            gp.put("inTime", selectedInTime != null ? selectedInTime : "");
            gp.put("status", "pending");
            gp.put("qrCodeData", "");
            gp.put("createdAt", System.currentTimeMillis());
            gp.put("updatedAt", System.currentTimeMillis());

            db.collection("requests").add(gp).addOnSuccessListener(ref -> {
                ref.update("requestId", ref.getId());
                Toast.makeText(CreateGatepassActivity.this, "Submitted", Toast.LENGTH_LONG).show();
                finish();
            }).addOnFailureListener(e -> {
                btnSubmit.setEnabled(true);
                Toast.makeText(CreateGatepassActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            // fallback minimal
            Map<String, Object> gp = new HashMap<>();
            gp.put("studentId", uid);
            gp.put("type", "daily");
            gp.put("reason", reason);
            gp.put("fromDate", selectedDate);
            gp.put("toDate", selectedDate);
            gp.put("destination", destination);
            gp.put("date", selectedDate);
            gp.put("outTime", selectedOutTime);
            gp.put("inTime", selectedInTime != null ? selectedInTime : "");
            gp.put("status", "pending");
            gp.put("qrCodeData", "");
            gp.put("createdAt", System.currentTimeMillis());
            gp.put("updatedAt", System.currentTimeMillis());

            db.collection("requests").add(gp).addOnSuccessListener(ref -> {
                ref.update("requestId", ref.getId());
                Toast.makeText(CreateGatepassActivity.this, "Submitted", Toast.LENGTH_LONG).show();
                finish();
            }).addOnFailureListener(e2 -> {
                btnSubmit.setEnabled(true);
                Toast.makeText(CreateGatepassActivity.this, "Error: " + e2.getMessage(), Toast.LENGTH_LONG).show();
            });
        });
    }
}
