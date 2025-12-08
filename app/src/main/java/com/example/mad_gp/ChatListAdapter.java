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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private Context context;
    private List<ChatRoom> chatRooms;
    private String currentUserId;

    public ChatListAdapter(Context context, List<ChatRoom> chatRooms) {
        this.context = context;
        this.chatRooms = chatRooms;
        // 修复点 1: 安全获取当前用户 ID，防止空指针
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_chat_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);

        // 显示最后一条消息
        holder.tvLastMessage.setText(room.getLastMessage() != null ? room.getLastMessage() : "");

        // 核心：找出对方的 ID 并加载信息
        List<String> participants = room.getParticipants();
        String targetUserId = null;

        if (participants != null && currentUserId != null) {
            for (String id : participants) {
                if (!id.equals(currentUserId)) {
                    targetUserId = id;
                    break;
                }
            }
        }

        if (targetUserId != null) {
            final String finalTargetId = targetUserId;

            // 加载对方信息
            FirebaseFirestore.getInstance().collection("users").document(targetUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            // 修复点 2: 这里使用的是修正后的 holder.tvName
                            holder.tvName.setText(name != null ? name : "Unknown");

                            String avatarUrl = documentSnapshot.getString("profileImageUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                if (avatarUrl.startsWith("http")) {
                                    Glide.with(context).load(avatarUrl).into(holder.ivAvatar);
                                } else {
                                    int resId = context.getResources().getIdentifier(avatarUrl, "drawable", context.getPackageName());
                                    if (resId != 0) holder.ivAvatar.setImageResource(resId);
                                    else holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                                }
                            } else {
                                holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                            }

                            holder.itemView.setOnClickListener(v -> {
                                Intent intent = new Intent(context, Chat.class);
                                intent.putExtra("TARGET_USER_ID", finalTargetId);
                                intent.putExtra("USER_NAME", name != null ? name : "User");
                                context.startActivity(intent);
                            });
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // 修复点 3: 变量名改为 tvName 以匹配 XML，防止混淆
        public TextView tvName, tvLastMessage;
        public ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 修复点 4: ID 必须匹配 XML (activity_chat_row.xml 用的是 tvName)
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}