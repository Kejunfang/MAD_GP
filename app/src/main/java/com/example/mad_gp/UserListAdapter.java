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

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> userList;

    public UserListAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.tvName.setText(user.getName());

        String avatar = user.getProfileImageUrl();
        if (avatar != null && !avatar.isEmpty()) {
            if (avatar.startsWith("http")) {
                Glide.with(context).load(avatar).into(holder.ivAvatar);
            } else {
                int resId = context.getResources().getIdentifier(avatar, "drawable", context.getPackageName());
                if (resId != 0) holder.ivAvatar.setImageResource(resId);
                else holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        // 点击跳转到该用户的 Profile
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfile.class);
            intent.putExtra("TARGET_USER_ID", user.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}