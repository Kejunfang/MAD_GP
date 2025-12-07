package com.example.mad_gp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private Context context;
    private List<Appointment> appointmentList;

    public AppointmentAdapter(Context context, List<Appointment> appointmentList) {
        this.context = context;
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_appoinment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appt = appointmentList.get(position);

        holder.tvName.setText(appt.getCounsellorName());
        holder.tvDate.setText(appt.getDate() + " • " + appt.getTime());
        holder.tvLocation.setText(appt.getLocation());

        // 加载图片
        String imgName = appt.getCounsellorImage();
        if (imgName != null && !imgName.isEmpty()) {
            int resId = context.getResources().getIdentifier(imgName, "drawable", context.getPackageName());
            if (resId != 0) holder.ivImage.setImageResource(resId);
            else holder.ivImage.setImageResource(R.drawable.counsellor1);
        } else {
            holder.ivImage.setImageResource(R.drawable.counsellor1);
        }
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvLocation;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvApptName);
            tvDate = itemView.findViewById(R.id.tvApptDate);
            tvLocation = itemView.findViewById(R.id.tvApptLocation);
            ivImage = itemView.findViewById(R.id.ivApptCounsellor);
        }
    }
}