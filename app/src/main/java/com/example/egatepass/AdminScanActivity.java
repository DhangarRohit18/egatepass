package com.example.egatepass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminScanActivity extends AppCompatActivity {
    private Button btnStartScan;
    private TextView tvScanResult, tvScanDetails;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RolePref.isAdmin(this)) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        setContentView(R.layout.activity_admin_scan);
        btnStartScan = findViewById(R.id.btnStartScan);
        tvScanResult = findViewById(R.id.tvScanResult);
        tvScanDetails = findViewById(R.id.tvScanDetails);
        db = FirebaseFirestore.getInstance();
        btnStartScan.setOnClickListener(v -> startQrScan());
    }

    private void startQrScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan Gatepass QR");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                handleQrResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleQrResult(String qrContent) {
        tvScanResult.setText(qrContent);
        db.collection("requests").document(qrContent).get().addOnSuccessListener(this::onRequestSnapshot);
    }

    private void onRequestSnapshot(DocumentSnapshot doc) {
        if (!doc.exists()) {
            tvScanDetails.setText("Request not found");
            return;
        }
        String status = doc.getString("status");
        String student = doc.getString("studentId");
        if ("approved".equals(status)) {
            tvScanDetails.setText("Approved Gatepass of " + student);
        } else {
            tvScanDetails.setText("Request not approved");
        }
    }
}
