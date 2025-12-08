package com.example.mad_gp;

import android.content.Context;
import android.content.Intent; // 1. 导入 Intent
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParticipatedWorkshopAdapter extends RecyclerView.Adapter<ParticipatedWorkshopAdapter.ViewHolder> {

    private Context context;
    private List<Workshop> workshopList;

    public ParticipatedWorkshopAdapter(Context context, List<Workshop> workshopList) {
        this.context = context;
        this.workshopList = workshopList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_participated_workshop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workshop workshop = workshopList.get(position);

        holder.tvTitle.setText(workshop.getTitle());

        String imgName = workshop.getImageName();
        if (imgName != null && !imgName.isEmpty()) {
            int resId = context.getResources().getIdentifier(imgName, "drawable", context.getPackageName());
            if (resId != 0) holder.ivImage.setImageResource(resId);
            else holder.ivImage.setImageResource(R.drawable.stress);
        } else {
            holder.ivImage.setImageResource(R.drawable.stress);
        }

        // --- 2. 新增：点击跳转逻辑 ---
        holder.itemView.setOnClickListener(v -> {
            // 注意：如果你实际的文件名是 Workshop1Detail1，请将下面的 Workshop1Detail.class 改为 Workshop1Detail1.class
            Intent intent = new Intent(context, Workshop1Detail.class);

            // 传递整个 Workshop 对象 (前提是 Workshop 类实现了 Serializable)
            intent.putExtra("WORKSHOP_DATA", workshop);

            context.startActivity(intent);
        });
        // 在 ParticipatedWorkshopAdapter.java 的 onBindViewHolder 里
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Workshop1Detail.class); // 记得确认类名是否为 Workshop1Detail1

            // 1. 传递 Workshop 数据
            intent.putExtra("WORKSHOP_DATA", workshop);

            // 2. ★★★ 新增：传递一个信号，表示“我是已报名的状态”
            intent.putExtra("IS_REGISTERED", true);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return workshopList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvWorkshopTitle);
            ivImage = itemView.findViewById(R.id.ivWorkshopImage);
        }
    }
}