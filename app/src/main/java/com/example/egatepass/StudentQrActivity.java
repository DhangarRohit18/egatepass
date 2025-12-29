package com.example.egatepass;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.OutputStream;

public class StudentQrActivity extends AppCompatActivity {
    private ImageView ivQr;
    private TextView tvQrInfo;
    private Button btnSave, btnShare;
    private FirebaseFirestore db;
    private String requestId;
    private Bitmap currentQrBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_qr);
        ivQr = findViewById(R.id.ivQr);
        tvQrInfo = findViewById(R.id.tvQrInfo);
        btnSave = findViewById(R.id.btnSaveQr);
        btnShare = findViewById(R.id.btnShareQr);
        db = FirebaseFirestore.getInstance();
        requestId = getIntent().getStringExtra("requestId");
        if (requestId == null || requestId.isEmpty()) {
            finish();
            return;
        }
        db.collection("requests").document(requestId).get().addOnSuccessListener(this::onRequestLoaded);
        btnSave.setOnClickListener(v -> saveQrToGallery());
        btnShare.setOnClickListener(v -> shareQr());
    }

    private void onRequestLoaded(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String studentId = doc.getString("studentId");
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!currentUid.equals(studentId) && !RolePref.isAdmin(this)) {
            Toast.makeText(this, "Not authorized to view this QR", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String payloadId = doc.getId();
        try {
            Bitmap bmp = QrUtil.generateQrBitmap(payloadId, 800);
            ivQr.setImageBitmap(bmp);
            currentQrBitmap = bmp;
            tvQrInfo.setText("Gatepass Request: " + payloadId);
        } catch (Exception e) {
            Toast.makeText(this, "QR generation failed " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveQrToGallery() {
        if (currentQrBitmap == null) return;
        String filename = "egatepass_qr_" + requestId + ".png";
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = getContentResolver().openOutputStream(uri);
            } else {
                fos = openFileOutput(filename, MODE_PRIVATE);
            }
            currentQrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "QR saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareQr() {
        if (currentQrBitmap == null) return;
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), currentQrBitmap, "egatepass_qr", null);
        Uri uri = Uri.parse(path);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share QR"));
    }
}
