package com.example.egatepass;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RequestDetailActivity extends AppCompatActivity {

    private TextView tvStatus, tvName, tvEnrollment, tvDepartment, tvYear, tvMobile;
    private TextView tvReason, tvDestination, tvFromTo, tvCreatedAt, tvApprovedBy;
    private Button btnApprove, btnReject;

    private FirebaseFirestore db;
    private String reqId;
    private DocumentReference reqRef;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_detail_activity);

        tvStatus = findViewById(R.id.tvStatus);
        tvName = findViewById(R.id.tvName);
        tvEnrollment = findViewById(R.id.tvEnrollment);
        tvDepartment = findViewById(R.id.tvDepartment);
        tvYear = findViewById(R.id.tvYear);
        tvMobile = findViewById(R.id.tvMobile);

        tvReason = findViewById(R.id.tvReason);
        tvDestination = findViewById(R.id.tvDestination);
        tvFromTo = findViewById(R.id.tvFromTo);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvApprovedBy = findViewById(R.id.tvApprovedBy);

        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

        db = FirebaseFirestore.getInstance();

        reqId = getIntent().getStringExtra("reqId");
        if (TextUtils.isEmpty(reqId)) {
            Toast.makeText(this, "No request id provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        reqRef = db.collection("requests").document(reqId);

        // listen to realtime changes (so UI updates when admin approves/declines)
        registration = reqRef.addSnapshotListener((snap, error) -> {
            if (error != null) {
                Toast.makeText(RequestDetailActivity.this, "Failed to load: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (snap == null || !snap.exists()) {
                Toast.makeText(RequestDetailActivity.this, "Request not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            populateUI(snap.getData());
        });

        btnApprove.setOnClickListener(v -> confirmAction("approved"));
        btnReject.setOnClickListener(v -> confirmAction("declined"));
    }

    private void populateUI(Map<String, Object> data) {
        if (data == null) return;

        String status = data.get("status") != null ? data.get("status").toString() : "pending";
        tvStatus.setText(status.toUpperCase());
        switch (status) {
            case "approved":
                tvStatus.setBackgroundResource(R.drawable.bg_status_approved);
                break;
            case "declined":
                tvStatus.setBackgroundResource(R.drawable.bg_status_declined);
                break;
            default:
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                break;
        }

        String name = data.get("name") != null ? data.get("name").toString() : "-";
        String enrollment = data.get("enrollmentNo") != null ? data.get("enrollmentNo").toString() : "-";
        String department = data.get("department") != null ? data.get("department").toString() : "-";
        String year = data.get("year") != null ? data.get("year").toString() : "-";
        String mobile = data.get("mobile") != null ? data.get("mobile").toString() : "-";

        tvName.setText("Name: " + name);
        tvEnrollment.setText("Enrollment No: " + enrollment);
        tvDepartment.setText("Department: " + department);
        tvYear.setText("Year: " + year);
        tvMobile.setText("Mobile: " + mobile);

        tvReason.setText("Reason: " + (data.get("reason") != null ? data.get("reason").toString() : "-"));
        tvDestination.setText("Destination: " + (data.get("destination") != null ? data.get("destination").toString() : "-"));
        String from = data.get("fromDate") != null ? data.get("fromDate").toString() : "-";
        String to = data.get("toDate") != null ? data.get("toDate").toString() : "-";
        tvFromTo.setText("Date: " + from + " - " + to);

        // createdAt may be long or Timestamp
        Object createdAtObj = data.get("createdAt");
        String createdAtStr = "-";
        if (createdAtObj instanceof Long) {
            createdAtStr = DateFormat.getDateTimeInstance().format(new Date((Long) createdAtObj));
        } else if (createdAtObj instanceof com.google.firebase.Timestamp) {
            createdAtStr = DateFormat.getDateTimeInstance().format(((com.google.firebase.Timestamp) createdAtObj).toDate());
        }
        tvCreatedAt.setText("Submitted: " + createdAtStr);

        Object ab = data.get("approvedBy");
        Object aa = data.get("approvedAt");
        String approvedBy = ab != null ? ab.toString() : "-";
        String approvedAtStr = "-";
        if (aa instanceof Long) {
            approvedAtStr = DateFormat.getDateTimeInstance().format(new Date((Long) aa));
        } else if (aa instanceof com.google.firebase.Timestamp) {
            approvedAtStr = DateFormat.getDateTimeInstance().format(((com.google.firebase.Timestamp) aa).toDate());
        }
        tvApprovedBy.setText("Approved By: " + (approvedBy.equals("-") ? "-" : approvedBy + " at " + approvedAtStr));

        // if student fields are missing, try to fetch from users collection
        boolean missingEnrollment = enrollment == null || enrollment.equals("-") || enrollment.isEmpty();
        boolean missingDept = department == null || department.equals("-") || department.isEmpty();
        boolean missingYear = year == null || year.equals("-") || year.isEmpty();
        boolean missingMobile = mobile == null || mobile.equals("-") || mobile.isEmpty();

        if ((missingEnrollment || missingDept || missingYear || missingMobile) && data.get("studentId") != null) {
            fetchUserProfile(data.get("studentId").toString());
        }
    }

    private void fetchUserProfile(String studentId) {
        db.collection("users").document(studentId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String enrol = doc.getString("enrollmentNo");
                        String dept = doc.getString("department");
                        String yr = doc.getString("year");
                        String mob = doc.getString("mobile");

                        if (!TextUtils.isEmpty(enrol)) tvEnrollment.setText("Enrollment No: " + enrol);
                        if (!TextUtils.isEmpty(dept)) tvDepartment.setText("Department: " + dept);
                        if (!TextUtils.isEmpty(yr)) tvYear.setText("Year: " + yr);
                        if (!TextUtils.isEmpty(mob)) tvMobile.setText("Mobile: " + mob);
                    }
                })
                .addOnFailureListener(e -> {
                    // ignore silently
                });
    }

    private void confirmAction(String action) {
        String title = action.equals("approved") ? "Approve request" : "Decline request";
        String message = "Are you sure you want to " + action + " this request?";
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> performUpdate(action))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // IMPORTANT: per your choice B — only update status / approvedBy / approvedAt here.
    private void performUpdate(String newStatus) {
        if (reqRef == null) return;
        String adminUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "admin";

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("approvedBy", adminUid);
        updates.put("approvedAt", System.currentTimeMillis());
        updates.put("updatedAt", System.currentTimeMillis());

        reqRef.update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(RequestDetailActivity.this, "Request " + newStatus, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(RequestDetailActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        if (registration != null) registration.remove();
        super.onDestroy();
    }
}
