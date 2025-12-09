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

        holder.tvLastMessage.setText(room.getLastMessage() != null ? room.getLastMessage() : "");

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

            FirebaseFirestore.getInstance().collection("users").document(targetUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");

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
        public TextView tvName, tvLastMessage;
        public ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}