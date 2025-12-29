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

public class HistoryActivity extends AppCompatActivity implements RequestAdapter.OnItemClickListener {

    private RecyclerView rv;
    private RequestAdapter adapter;
    private List<RequestModel> list = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rv = findViewById(R.id.rvRequests);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new RequestAdapter(this, list, this);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        registration = db.collection("requests")
                .whereEqualTo("studentId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap == null) return;

                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        DocumentSnapshot ds = dc.getDocument();
                        RequestModel req = ds.toObject(RequestModel.class);
                        req = ensureRequestId(req, ds.getId());

                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            list.add(0, req);
                            adapter.notifyItemInserted(0);
                        } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            for (int i = 0; i < list.size(); i++) {
                                if (ds.getId().equals(list.get(i).getRequestId())) {
                                    list.set(i, req);
                                    adapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                        } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                            for (int i = 0; i < list.size(); i++) {
                                if (ds.getId().equals(list.get(i).getRequestId())) {
                                    list.remove(i);
                                    adapter.notifyItemRemoved(i);
                                    break;
                                }
                            }
                        }
                    }

                    if (snap.getDocumentChanges().isEmpty()) {
                        list.clear();
                        for (DocumentSnapshot ds : snap.getDocuments()) {
                            RequestModel r = ds.toObject(RequestModel.class);
                            r = ensureRequestId(r, ds.getId());
                            list.add(r);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private RequestModel ensureRequestId(RequestModel r, String id) {
        if (r == null) r = new RequestModel();
        try {
            java.lang.reflect.Method m = r.getClass().getMethod("setRequestId", String.class);
            m.invoke(r, id);
            return r;
        } catch (Exception ex) {
            try {
                java.lang.reflect.Field f = r.getClass().getDeclaredField("requestId");
                f.setAccessible(true);
                f.set(r, id);
            } catch (Exception ignore) { }
            return r;
        }
    }

    @Override
    public void onItemClick(RequestModel item) {
        Intent i = new Intent(this, RequestDetailActivity.class);
        i.putExtra("requestId", item.getRequestId());
        startActivity(i);
    }

    @Override
    public void onQrClick(RequestModel item) {
        // No QR operations in history: leave empty!
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) registration.remove();
    }
}
