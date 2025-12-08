package com.example.mad_gp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private Context context;
    private List<ChatRoom> chatRoomList;
    private String currentUserId;

    public ChatListAdapter(Context context, List<ChatRoom> chatRoomList) {
        this.context = context;
        this.chatRoomList = chatRoomList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ★★★ 这里加载你原来的 activity_chat_row.xml ★★★
        // 注意：activity_chat_row.xml 的根布局不需要 id="messageContainer" 了，那是给气泡用的
        View view = LayoutInflater.from(context).inflate(R.layout.activity_chat_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoom room = chatRoomList.get(position);
        holder.tvLastMsg.setText(room.getLastMessage());

        // 处理时间显示 (简化版)
        if (room.getLastMessageTime() != null) {
            holder.tvTime.setText(room.getLastMessageTime().toDate().toString().substring(11, 16));
        }

        // --- 关键：找出“对方”是谁 ---
        // participants 列表里有两个 ID，不等于 currentUserId 的那个就是对方
        String otherUserId = null;
        for (String id : room.getParticipants()) {
            if (!id.equals(currentUserId)) {
                otherUserId = id;
                break;
            }
        }

        // 去 Users 表查对方的名字和头像
        if (otherUserId != null) {
            String finalOtherUserId = otherUserId;
            FirebaseFirestore.getInstance().collection("users").document(otherUserId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            holder.tvName.setText(name);
                            // 这里可以加 Glide 加载头像...

                            // 点击跳转
                            holder.itemView.setOnClickListener(v -> {
                                Intent intent = new Intent(context, Chat.class);
                                intent.putExtra("TARGET_USER_ID", finalOtherUserId);
                                intent.putExtra("USER_NAME", name);
                                context.startActivity(intent);
                            });
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMsg, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 绑定 activity_chat_row.xml 里的 ID
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMsg = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}