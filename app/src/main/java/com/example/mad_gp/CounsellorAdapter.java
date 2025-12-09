package com.example.mad_gp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CounsellorAdapter extends RecyclerView.Adapter<CounsellorAdapter.ViewHolder> {

    private Context context;
    private List<Counsellor> counsellorList;

    public CounsellorAdapter(Context context, List<Counsellor> counsellorList) {
        this.context = context;
        this.counsellorList = counsellorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_counsellor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Counsellor counsellor = counsellorList.get(position);

        holder.tvName.setText(counsellor.getName());
        holder.tvTitle.setText(counsellor.getTitle());
        holder.tvLocation.setText(counsellor.getLocation());

        String imgName = counsellor.getImageName();
        if (imgName != null && !imgName.isEmpty()) {
            int resId = context.getResources().getIdentifier(imgName, "drawable", context.getPackageName());
            if (resId != 0) {
                holder.ivImage.setImageResource(resId);
            } else {
                holder.ivImage.setImageResource(R.drawable.counsellor1);
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.counsellor1);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AppointmentBooking.class);
            intent.putExtra("COUNSELLOR_DATA", counsellor);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return counsellorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTitle, tvLocation;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCounsellorName);
            tvTitle = itemView.findViewById(R.id.tvCounsellorTitle);
            tvLocation = itemView.findViewById(R.id.tvCounsellorLocation);
            ivImage = itemView.findViewById(R.id.ivCounsellorImage);
        }
    }
}