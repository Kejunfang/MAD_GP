package com.example.mad_gp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
// ★ 引入 DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.ViewHolder> {

    private Context context;
    private List<CommunityPost> postList;
    private String currentUserId;
    private FirebaseFirestore db; // ★ 引入 Firestore

    public CommunityPostAdapter(Context context, List<CommunityPost> postList) {
        this.context = context;
        this.postList = postList;
        this.db = FirebaseFirestore.getInstance(); // ★ 初始化 DB
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_community_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityPost post = postList.get(position);

        holder.tvName.setText(post.getUserName());
        holder.tvTime.setText(post.getTimeAgo());
        holder.tvContent.setText(post.getContent());
        holder.tvLikeCount.setText(String.valueOf(post.getLikesCount()));
        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

        // --- ★★★ 核心修改：实时获取最新头像 ★★★ ---
        // 我们不直接用 post.getUserAvatar()，而是去 users 表里查最新的
        String authorId = post.getUserId();
        if (authorId != null && !authorId.isEmpty()) {
            db.collection("users").document(authorId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // 获取最新的 profileImageUrl
                            String latestAvatar = documentSnapshot.getString("profileImageUrl");
                            // 如果有名字变动，也可以顺便更新名字
                            String latestName = documentSnapshot.getString("name");
                            if (latestName != null) holder.tvName.setText(latestName);

                            // 更新头像 UI
                            setLocalImage(holder.ivAvatar, latestAvatar);
                        } else {
                            // 如果查不到用户，回退使用帖子里的旧数据
                            setLocalImage(holder.ivAvatar, post.getUserAvatar());
                        }
                    });
        } else {
            setLocalImage(holder.ivAvatar, post.getUserAvatar());
        }
        // ---------------------------------------------

        // 设置帖子图片 (这个不用实时查，因为帖子图片一般不变)
        if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
            holder.cardPostImage.setVisibility(View.VISIBLE);
            setLocalImage(holder.ivPostImage, post.getPostImage());
        } else {
            holder.cardPostImage.setVisibility(View.GONE);
        }

        // 点赞逻辑 (保持不变)
        boolean isLiked = post.getLikedBy().contains(currentUserId);
        if (isLiked) {
            holder.ivLike.setImageResource(R.drawable.favourite_filled);
            holder.ivLike.setColorFilter(Color.parseColor("#F44336"));
        } else {
            holder.ivLike.setImageResource(R.drawable.ic_favorite_border);
            holder.ivLike.setColorFilter(Color.parseColor("#7D8C9A"));
        }

        holder.btnLike.setOnClickListener(v -> {
            if (currentUserId == null) return;
            if (isLiked) {
                db.collection("community_posts").document(post.getPostId())
                        .update("likesCount", FieldValue.increment(-1),
                                "likedBy", FieldValue.arrayRemove(currentUserId));
            } else {
                db.collection("community_posts").document(post.getPostId())
                        .update("likesCount", FieldValue.increment(1),
                                "likedBy", FieldValue.arrayUnion(currentUserId));
            }
        });

        holder.ivAvatar.setOnClickListener(v -> {
            // 创建跳转意图
            Intent intent = new Intent(context, UserProfile.class);
            // 关键一步：把发帖人的 ID 传过去！
            intent.putExtra("TARGET_USER_ID", post.getUserId());
            context.startActivity(intent);
        });

// 为了体验更好，点击名字也应该能跳过去
        holder.tvName.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfile.class);
            intent.putExtra("TARGET_USER_ID", post.getUserId());
            context.startActivity(intent);
        });
    }

    // 辅助方法：加载本地 drawable 资源
    private void setLocalImage(ImageView iv, String imgName) {
        if (imgName == null || imgName.isEmpty()) {
            iv.setImageResource(R.drawable.ic_default_avatar); // 给个默认图防止空白
            return;
        }
        // 处理可能存在的 http URL (虽然你说只用本地图片，但为了保险)
        if (imgName.startsWith("http")) {
            // 如果你有 Glide: Glide.with(context).load(imgName).into(iv);
            // 如果没有，就忽略或者处理
        } else {
            int resId = context.getResources().getIdentifier(imgName, "drawable", context.getPackageName());
            if (resId != 0) iv.setImageResource(resId);
            else iv.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // ... ViewHolder 代码保持不变 ...
        TextView tvName, tvTime, tvContent, tvLikeCount, tvCommentCount;
        ImageView ivAvatar, ivPostImage, ivLike;
        View btnLike;
        MaterialCardView cardPostImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPostUserName);
            tvTime = itemView.findViewById(R.id.tvPostTime);
            tvContent = itemView.findViewById(R.id.tvPostContent);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            ivAvatar = itemView.findViewById(R.id.ivPostAvatar);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivLike = itemView.findViewById(R.id.ivLike);
            btnLike = itemView.findViewById(R.id.btnLike);
            cardPostImage = itemView.findViewById(R.id.cardPostImage);
        }
    }
}