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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Workshop1Detail.class);

            intent.putExtra("WORKSHOP_DATA", workshop);

            context.startActivity(intent);
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Workshop1Detail.class);

            intent.putExtra("WORKSHOP_DATA", workshop);

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