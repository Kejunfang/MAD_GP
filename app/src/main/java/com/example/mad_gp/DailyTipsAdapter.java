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

import com.bumptech.glide.Glide;

import java.util.List;

public class DailyTipsAdapter extends RecyclerView.Adapter<DailyTipsAdapter.TipViewHolder> {

    private Context context;
    private List<DailyTip> tipList;

    public DailyTipsAdapter(Context context, List<DailyTip> tipList) {
        this.context = context;
        this.tipList = tipList;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_daily_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        DailyTip tip = tipList.get(position);

        holder.tvTitle.setText(tip.getTitle());
        holder.tvSubtitle.setText(tip.getSubtitle());

        // 图片加载逻辑 (兼容本地资源名和网络 URL)
        String imageUrl = tip.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("http")) {
                // 网络图片
                Glide.with(context).load(imageUrl).placeholder(R.drawable.studydesk).centerCrop().into(holder.ivImage);
            } else {
                // 本地资源名 (例如 "studydesk")
                int resId = context.getResources().getIdentifier(imageUrl, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.ivImage.setImageResource(resId);
                } else {
                    holder.ivImage.setImageResource(R.drawable.studydesk);
                }
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.studydesk);
        }

        // 点击跳转
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DailyTipsDetails.class);
            intent.putExtra("TIP_ID", tip.getId()); // 传递 ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tipList.size();
    }

    public static class TipViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivImage;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTipTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTipSubtitle);
            ivImage = itemView.findViewById(R.id.ivTipImage);
        }
    }
}