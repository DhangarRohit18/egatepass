package com.example.egatepass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.VH> {

    public interface OnItemClickListener {
        void onQrClick(RequestModel item);
        void onItemClick(RequestModel item);
    }

    private final Context ctx;
    private final List<RequestModel> items;
    private final OnItemClickListener listener;
    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());

    public RequestAdapter(Context ctx, List<RequestModel> items, OnItemClickListener listener){
        this.ctx = ctx;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position){
        RequestModel r = items.get(position);
        if (r == null) return;

        // Reason
        holder.tvReason.setText(r.getReason() != null ? r.getReason() : "No reason");

        // Created date (fallback safe)
        long created = r.getCreatedAt();
        if (created > 0) holder.tvDate.setText(df.format(new Date(created)));
        else holder.tvDate.setText(r.getDate() != null ? r.getDate() : "-");

        // From / To (fromDateMillis / toDateMillis)
        long from = r.getFromDateMillis();
        long to = r.getToDateMillis();
        if (from > 0 && to > 0) {
            holder.tvOut.setText("From: " + df.format(new Date(from)) + "\nTo: " + df.format(new Date(to)));
        } else {
            // fallback to string fields if millis not present
            String fromStr = r.getFromDate() != null ? r.getFromDate() : "-";
            String toStr = r.getToDate() != null ? r.getToDate() : "-";
            holder.tvOut.setText("From: " + fromStr + "\nTo: " + toStr);
        }

        // Status styling
        String status = r.getStatus() != null ? r.getStatus().toLowerCase() : "pending";
        holder.tvStatus.setText(status.toUpperCase());
        int bgRes;
        switch (status) {
            case "approved": bgRes = R.drawable.bg_status_approved; break;
            case "declined": bgRes = R.drawable.bg_status_declined; break;
            default: bgRes = R.drawable.bg_status_pending; break;
        }
        holder.tvStatus.setBackgroundResource(bgRes);

        // clicks
        holder.card.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(r);
        });
        holder.tvStatus.setOnClickListener(v -> {
            if (listener != null) listener.onQrClick(r);
        });
    }

    @Override
    public int getItemCount(){ return items != null ? items.size() : 0; }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvReason, tvDate, tvOut, tvStatus;
        CardView card;
        VH(View v){
            super(v);
            card = v.findViewById(R.id.cardRoot);
            tvReason = v.findViewById(R.id.tvReason);
            tvDate   = v.findViewById(R.id.tvDate);
            tvOut    = v.findViewById(R.id.tvOut);
            tvStatus = v.findViewById(R.id.chipStatus); // in item_my_request chipStatus used for status
            if (tvStatus == null) {
                // fallback to tvStatus id (some layouts use different id)
                tvStatus = v.findViewById(R.id.tvStatus);
            }
        }
    }
}
