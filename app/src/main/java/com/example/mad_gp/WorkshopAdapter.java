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

public class WorkshopAdapter extends RecyclerView.Adapter<WorkshopAdapter.ViewHolder> {

    private Context context;
    private List<Workshop> workshopList;

    public WorkshopAdapter(Context context, List<Workshop> workshopList) {
        this.context = context;
        this.workshopList = workshopList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_workshop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workshop workshop = workshopList.get(position);

        holder.tvTitle.setText(workshop.getTitle());
        holder.tvDesc.setText(workshop.getDescription());
        holder.tvLocation.setText(workshop.getLocation());

        String imgName = workshop.getImageName();
        if (imgName != null && !imgName.isEmpty()) {
            int resId = context.getResources().getIdentifier(imgName, "drawable", context.getPackageName());
            if (resId != 0) holder.ivImage.setImageResource(resId);
        }

        // 点击跳转到详情页
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Workshop1Detail.class);
            intent.putExtra("WORKSHOP_DATA", workshop);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return workshopList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvLocation;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvWorkshopTitle);
            tvDesc = itemView.findViewById(R.id.tvWorkshopDesc);
            tvLocation = itemView.findViewById(R.id.tvWorkshopLocation);
            ivImage = itemView.findViewById(R.id.ivWorkshopImage);
        }
    }
}