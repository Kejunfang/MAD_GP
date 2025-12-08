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
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 请确保你有一个 item_chat_room.xml 或者类似的布局文件
        View view = LayoutInflater.from(context).inflate(R.layout.activity_chat_row, parent, false);
        // 注意：activity_chat_row 这个名字可能需要根据你实际的 item 布局文件名修改
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);

        // 显示最后一条消息
        holder.tvLastMessage.setText(room.getLastMessage() != null ? room.getLastMessage() : "");

        // ★★★ 核心：找出对方的 ID 并加载信息 ★★★
        List<String> participants = room.getParticipants();
        String targetUserId = null;

        if (participants != null) {
            for (String id : participants) {
                if (!id.equals(currentUserId)) {
                    targetUserId = id;
                    break;
                }
            }
        }

        if (targetUserId != null) {
            // 1. 先把 targetUserId 存入 holder，方便点击事件使用
            final String finalTargetId = targetUserId;

            // 2. 去数据库查这个人的信息
            FirebaseFirestore.getInstance().collection("users").document(targetUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // 获取名字
                            String name = documentSnapshot.getString("name");
                            holder.tvUserName.setText(name != null ? name : "Unknown");

                            // 获取头像
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

                            // 3. 设置点击跳转事件 (只有拿到名字后跳转体验才好)
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
        public TextView tvUserName, tvLastMessage;
        public ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 绑定 item 布局里的控件 ID
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}