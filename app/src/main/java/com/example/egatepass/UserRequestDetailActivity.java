package com.example.egatepass;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserRequestDetailActivity extends AppCompatActivity {

    private TextView tvStatus, tvReason, tvDate, tvToDate;
    private Button btnGenerateQr;
    private ImageView ivQr;

    private FirebaseFirestore db;
    private String reqId;
    private RequestModel currentRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request_detail);

        tvStatus = findViewById(R.id.tvDetailStatus);
        tvReason = findViewById(R.id.tvDetailReason);
        tvDate = findViewById(R.id.tvDetailDate);
        tvToDate = findViewById(R.id.tvDetailOut); // some layouts reuse fields
        btnGenerateQr = findViewById(R.id.btnGenerateQr);
        ivQr = findViewById(R.id.ivQr);

        db = FirebaseFirestore.getInstance();

        reqId = getIntent().getStringExtra("requestId");
        if (reqId == null) { Toast.makeText(this, "No request id", Toast.LENGTH_SHORT).show(); finish(); return; }

        loadRequest();

        btnGenerateQr.setOnClickListener(v -> {
            if (currentRequest != null && "approved".equalsIgnoreCase(currentRequest.getStatus())) {

                String payload = "{\"id\":\"" + (currentRequest.getQrData() != null && !currentRequest.getQrData().isEmpty()
                        ? currentRequest.getQrData() : currentRequest.getRequestId()) + "\"}";

                generateAndShowQr(payload);

                // persist qrData if not stored
                if (currentRequest.getQrData() == null || currentRequest.getQrData().isEmpty()) {
                    db.collection("requests").document(reqId).update("qrCodeData", payload);
                }

            } else {
                Toast.makeText(this, "QR can be generated only when approved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRequest(){
        db.collection("requests").document(reqId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) { Toast.makeText(this,"Request not found", Toast.LENGTH_SHORT).show(); finish(); return; }
            RequestModel r = doc.toObject(RequestModel.class);
            if (r != null) {
                if (r.getRequestId() == null || r.getRequestId().isEmpty()) r.setRequestId(doc.getId());
                currentRequest = r;
                populateUI(r);
            }
        }).addOnFailureListener(e -> Toast.makeText(this,"Error: "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void populateUI(RequestModel r){
        tvReason.setText("Reason: " + (r.getReason()!=null? r.getReason() : "-"));

        long fromMillis = r.getFromDateMillis();
        long toMillis = r.getToDateMillis();

        tvDate.setText("From: " + (fromMillis > 0 ? formatDate(fromMillis) : (r.getFromDate()!=null? r.getFromDate() : "-")));
        tvToDate.setText("To: " + (toMillis > 0 ? formatDate(toMillis) : (r.getToDate()!=null? r.getToDate() : "-")));

        tvStatus.setText("Status: " + (r.getStatus()!=null? r.getStatus() : "pending"));

        if ("approved".equalsIgnoreCase(r.getStatus())) {
            String payload = "{\"id\":\"" + (r.getQrData() != null && !r.getQrData().isEmpty() ? r.getQrData() : r.getRequestId()) + "\"}";
            generateAndShowQr(payload);
        }
    }

    private String formatDate(long millis){
        try {
            return new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).format(new Date(millis));
        } catch (Exception e) {
            return "-";
        }
    }

    private void generateAndShowQr(String data) {
        try {
            Bitmap bmp = QrUtil.generateQrBitmap(data, 600);
            ivQr.setImageBitmap(bmp);
        } catch (Exception e) {
            Toast.makeText(this, "QR gen failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
