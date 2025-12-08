package com.example.mad_gp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // 确保你引入了 Glide
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.PostViewHolder> {

    private Context context;
    private List<CommunityPost> postList;
    private String currentUserId;
    private FirebaseFirestore db;
    private OnPostActionListener actionListener; // 点击事件接口

    // 定义接口
    public interface OnPostActionListener {
        void onCommentClick(CommunityPost post);
        void onShareClick(CommunityPost post);
    }

    // 构造函数
    public CommunityPostAdapter(Context context, List<CommunityPost> postList, OnPostActionListener listener) {
        this.context = context;
        this.postList = postList;
        this.actionListener = listener;
        this.db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    // 为了兼容旧的构造调用 (ProfilePage 可能会用到)，保留这个构造函数
    public CommunityPostAdapter(Context context, List<CommunityPost> postList) {
        this(context, postList, null);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_community_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPost post = postList.get(position);

        // 1. 设置基本文本
        holder.tvName.setText(post.getUserName());
        holder.tvTime.setText(post.getTimeAgo());
        holder.tvContent.setText(post.getContent());
        holder.tvLikeCount.setText(String.valueOf(post.getLikesCount()));
        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

        // 2. 加载头像 (实时获取最新头像逻辑)
        String authorId = post.getUserId();
        if (authorId != null && !authorId.isEmpty()) {
            db.collection("users").document(authorId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String latestAvatar = documentSnapshot.getString("profileImageUrl");
                            String latestName = documentSnapshot.getString("name");

                            if (latestName != null) holder.tvName.setText(latestName);
                            loadAvatar(holder.ivAvatar, latestAvatar);
                        } else {
                            loadAvatar(holder.ivAvatar, post.getUserAvatar());
                        }
                    });
        } else {
            loadAvatar(holder.ivAvatar, post.getUserAvatar());
        }

        // 3. 加载帖子图片
        if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
            holder.cardPostImage.setVisibility(View.VISIBLE);
            // 使用 Glide 加载 URL (推荐)
            if (post.getPostImage().startsWith("http")) {
                Glide.with(context).load(post.getPostImage()).into(holder.ivPostImage);
            } else {
                // 兼容本地资源名
                int resId = context.getResources().getIdentifier(post.getPostImage(), "drawable", context.getPackageName());
                if (resId != 0) holder.ivPostImage.setImageResource(resId);
            }
        } else {
            holder.cardPostImage.setVisibility(View.GONE);
        }

        // 4. 点赞状态
        boolean isLiked = post.getLikedBy().contains(currentUserId);
        if (isLiked) {
            holder.ivLike.setImageResource(R.drawable.favourite_filled);
            holder.ivLike.setColorFilter(Color.parseColor("#F44336"));
        } else {
            holder.ivLike.setImageResource(R.drawable.ic_favorite_border);
            holder.ivLike.setColorFilter(Color.parseColor("#7D8C9A"));
        }

        // 5. 点击事件绑定
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

        // 评论点击
        if (holder.ivComment != null) {
            holder.ivComment.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onCommentClick(post);
            });
        }

        // 分享点击
        if (holder.ivShare != null) {
            holder.ivShare.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onShareClick(post);
            });
        }

        // 点击头像/名字跳转个人主页
        View.OnClickListener profileListener = v -> {
            Intent intent = new Intent(context, UserProfile.class);
            intent.putExtra("TARGET_USER_ID", post.getUserId());
            context.startActivity(intent);
        };
        holder.ivAvatar.setOnClickListener(profileListener);
        holder.tvName.setOnClickListener(profileListener);
    }

    // 辅助方法：加载头像
    private void loadAvatar(ImageView iv, String imgSource) {
        if (imgSource == null || imgSource.isEmpty()) {
            iv.setImageResource(R.drawable.ic_default_avatar);
            return;
        }
        if (imgSource.startsWith("http")) {
            Glide.with(context).load(imgSource).placeholder(R.drawable.ic_default_avatar).circleCrop().into(iv);
        } else {
            int resId = context.getResources().getIdentifier(imgSource, "drawable", context.getPackageName());
            if (resId != 0) iv.setImageResource(resId);
            else iv.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // ViewHolder 类
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvContent, tvLikeCount, tvCommentCount;
        ImageView ivAvatar, ivPostImage, ivLike, ivComment, ivShare;
        View btnLike;
        MaterialCardView cardPostImage;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // 这里一定要对应 XML 里的 ID
            tvName = itemView.findViewById(R.id.tvPostUserName);
            tvTime = itemView.findViewById(R.id.tvPostTime);
            tvContent = itemView.findViewById(R.id.tvPostContent); // 之前报错就是这里名字不对
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);

            ivAvatar = itemView.findViewById(R.id.ivPostAvatar);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivLike = itemView.findViewById(R.id.ivLike);

            // 新增的 ID，必须在 XML 里加上
            ivComment = itemView.findViewById(R.id.ivComment);
            ivShare = itemView.findViewById(R.id.ivShare);

            btnLike = itemView.findViewById(R.id.btnLike);
            cardPostImage = itemView.findViewById(R.id.cardPostImage);
        }
    }
}