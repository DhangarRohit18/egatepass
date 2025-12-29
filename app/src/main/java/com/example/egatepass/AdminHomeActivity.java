package com.example.egatepass;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminHomeActivity extends AppCompatActivity {

    private static final String TAG = "AdminRequestsAct";

    private RecyclerView rv;
    private AdminRequestAdapter adapter;
    private List<RequestModel> list = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration registration;

    private TextView tvCount;
    private Button btnPendingFilter, btnAllFilter;

    private boolean showingPendingOnly = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        ImageButton btnLogout = toolbar.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        rv = findViewById(R.id.rvRequests);
        tvCount = findViewById(R.id.tvCount);
        btnPendingFilter = findViewById(R.id.btnPendingFilter);
        btnAllFilter = findViewById(R.id.btnAllFilter);

        db = FirebaseFirestore.getInstance();

        adapter = new AdminRequestAdapter(this, list, new AdminRequestAdapter.OnItemActionListener() {
            @Override
            public void onAccept(RequestModel request) {
                approveRequest(request);
            }

            @Override
            public void onReject(RequestModel request) {
                declineRequest(request);
            }

            @Override
            public void onItemClick(RequestModel request) {
                Intent i = new Intent(AdminHomeActivity.this, RequestDetailActivity.class);
                // RequestDetailActivity expects "reqId" in your version, ensure it matches there
                i.putExtra("reqId", request.getRequestId());
                startActivity(i);
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnPendingFilter.setOnClickListener(v -> {
            showingPendingOnly = true;
            attachListener();
        });
        btnAllFilter.setOnClickListener(v -> {
            showingPendingOnly = false;
            attachListener();
        });

        attachListener();
    }

    private void attachListener() {
        if (registration != null) registration.remove();

        Query q;
        if (showingPendingOnly) {
            q = db.collection("requests")
                    .whereEqualTo("status", "pending")
                    .orderBy("createdAt", Query.Direction.DESCENDING);
        } else {
            q = db.collection("requests").orderBy("createdAt", Query.Direction.DESCENDING);
        }

        registration = q.addSnapshotListener((snap, e) -> {
            if (e != null) {
                String msg = e.getMessage();
                Log.w(TAG, "Listen failed.", e);
                if (msg != null && msg.contains("requires an index")) {
                    Toast.makeText(AdminHomeActivity.this,
                            "Query requires a Firestore index. Open console and create the suggested composite index.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminHomeActivity.this, "Listen failed: " + msg, Toast.LENGTH_LONG).show();
                }
                return;
            }
            if (snap == null) return;

            List<RequestModel> tmp = new ArrayList<>();
            for (DocumentSnapshot ds : snap.getDocuments()) {
                RequestModel r = ds.toObject(RequestModel.class);
                if (r == null) continue;
                if (r.getRequestId() == null || r.getRequestId().isEmpty()) r.setRequestId(ds.getId());
                tmp.add(r);
            }
            this.list = tmp;
            adapter.updateList(list);
            tvCount.setText(String.valueOf(list.size()));
        });
    }

    private void approveRequest(RequestModel request) {
        if (request == null) return;
        String id = request.getRequestId();
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Invalid request id", Toast.LENGTH_SHORT).show();
            return;
        }
        String adminUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "admin";

        String qrJson = "{\"id\":\"" + id + "\"}";

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "approved");
        updates.put("qrCodeData", qrJson);
        updates.put("approvedBy", adminUid);
        updates.put("approvedAt", System.currentTimeMillis());
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("requests").document(id)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Request approved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Approve failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void declineRequest(RequestModel request) {
        if (request == null) return;
        String id = request.getRequestId();
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Invalid request id", Toast.LENGTH_SHORT).show();
            return;
        }
        String adminUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "admin";

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "declined"); // standardized value
        updates.put("approvedBy", adminUid);
        updates.put("approvedAt", System.currentTimeMillis());
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("requests").document(id)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Request declined", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Decline failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        if (registration != null) registration.remove();
        super.onDestroy();
    }
}
