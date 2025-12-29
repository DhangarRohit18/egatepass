package com.example.egatepass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity implements RequestAdapter.OnItemClickListener {

    RecyclerView rv;
    RequestAdapter adapter;
    List<RequestModel> list = new ArrayList<>();
    FirebaseFirestore db;
    FirebaseAuth auth;
    ListenerRegistration registration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        rv = findViewById(R.id.rvRequests);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new RequestAdapter(this, list, this);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) { Toast.makeText(this,"Not logged in", Toast.LENGTH_SHORT).show(); finish(); return; }

        String uid = auth.getCurrentUser().getUid();

        registration = db.collection("requests")
                .whereEqualTo("studentId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { Toast.makeText(MyRequestsActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show(); return; }
                    if (snap == null) return;

                    // Handle document changes
                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        DocumentSnapshot ds = dc.getDocument();
                        RequestModel req = ds.toObject(RequestModel.class);
                        if (req != null) req.setRequestId(ds.getId());

                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            list.add(0, req);
                            adapter.notifyItemInserted(0);
                        } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            // find and update
                            for (int i=0;i<list.size();i++){
                                if (list.get(i).getRequestId().equals(req.getRequestId())){
                                    String oldStatus = list.get(i).getStatus();
                                    list.set(i, req);
                                    adapter.notifyItemChanged(i);
                                    String newStatus = req.getStatus();
                                    if (oldStatus != null && !oldStatus.equals(newStatus)) {
                                        // show local notification and toast
                                        LocalNotificationHelper.show(this, "Gatepass " + newStatus.toUpperCase(), "Your request for " + formatDate(req.getCreatedAt())  + " is " + newStatus);
                                    }
                                    break;
                                }
                            }
                        } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                            for (int i=0;i<list.size();i++){
                                if (list.get(i).getRequestId().equals(req.getRequestId())){
                                    list.remove(i);
                                    adapter.notifyItemRemoved(i);
                                    break;
                                }
                            }
                        }
                    }

                    // initial load fallback when no doc changes reported (rare)
                    if (snap.getDocumentChanges().isEmpty()){
                        list.clear();
                        for (DocumentSnapshot ds : snap.getDocuments()){
                            RequestModel r = ds.toObject(RequestModel.class);
                            if (r != null) r.setRequestId(ds.getId());
                            list.add(r);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
    @Override
    public void onQrClick(RequestModel item) {
        if (!"approved".equalsIgnoreCase(item.getStatus())) {
            Toast.makeText(this, "QR only for approved requests", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, StudentQrActivity.class);
        i.putExtra("requestId", item.getRequestId());
        startActivity(i);
    }

    @Override
    public void onItemClick(RequestModel item) {
        android.content.Intent i = new android.content.Intent(this, RequestDetailActivity.class);
        i.putExtra("requestId", item.getRequestId());
        startActivity(i);
    }
    private String formatDate(long millis){
        return new java.text.SimpleDateFormat("dd MMM yyyy hh:mm a", java.util.Locale.getDefault()).format(millis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) registration.remove();
    }
}
