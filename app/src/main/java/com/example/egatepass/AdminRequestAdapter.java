package com.example.egatepass;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminRequestAdapter extends RecyclerView.Adapter<AdminRequestAdapter.VH> {

    public interface OnItemActionListener {
        void onAccept(RequestModel request);
        void onReject(RequestModel request);
        void onItemClick(RequestModel request);
    }

    private final Context ctx;
    private List<RequestModel> list;
    private final OnItemActionListener listener;
    private final SimpleDateFormat readableDateFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public AdminRequestAdapter(Context ctx, List<RequestModel> list, OnItemActionListener listener) {
        this.ctx = ctx;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_admin_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        RequestModel r = list.get(pos);
        if (r == null) return;

        // name / enrollment (handle missing)
        String name = !TextUtils.isEmpty(r.getName()) ? r.getName() : "Unknown Student";
        h.tvName.setText(name);

        String enrol = !TextUtils.isEmpty(r.getEnrollmentNo()) ? r.getEnrollmentNo() : "-";
        h.tvEnrollment.setText(enrol);

        String reason = !TextUtils.isEmpty(r.getReason()) ? r.getReason() : "-";
        h.tvReason.setText("Reason: " + reason);

        // Date / From-To
        String dateText = "-";
        if (!TextUtils.isEmpty(r.getFromDate()) || !TextUtils.isEmpty(r.getToDate())) {
            String from = !TextUtils.isEmpty(r.getFromDate()) ? r.getFromDate() : "";
            String to = !TextUtils.isEmpty(r.getToDate()) ? r.getToDate() : from;
            dateText = from + (to.isEmpty() || from.equals(to) ? "" : " - " + to);
        } else if (!TextUtils.isEmpty(r.getDate())) {
            dateText = r.getDate();
        } else if (r.getCreatedAt() > 0) {
            dateText = readableDateFmt.format(new Date(r.getCreatedAt()));
        }
        String outStr = !TextUtils.isEmpty(r.getOutTime()) ? " • Out " + r.getOutTime() : "";
        h.tvDates.setText(dateText + outStr);

        String status = !TextUtils.isEmpty(r.getStatus()) ? r.getStatus() : "pending";
        h.tvStatus.setText(status.toUpperCase());

        // set listeners (using the IDs you provided)
        h.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(r);
        });
        h.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(r);
        });
        h.btnDetails.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(r);
        });

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void updateList(List<RequestModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvEnrollment, tvReason, tvStatus, tvDates;
        MaterialButton btnAccept, btnReject, btnDetails;

        public VH(@NonNull View item) {
            super(item);
            tvName = item.findViewById(R.id.tvStudentName);
            tvEnrollment = item.findViewById(R.id.tvEnrollment);
            tvReason = item.findViewById(R.id.tvReason);
            tvStatus = item.findViewById(R.id.tvStatus);
            tvDates = item.findViewById(R.id.tvDates);

            btnAccept = item.findViewById(R.id.btnAccept);
            btnReject = item.findViewById(R.id.btnReject);
            btnDetails = item.findViewById(R.id.btnDetails);
        }
    }
}
