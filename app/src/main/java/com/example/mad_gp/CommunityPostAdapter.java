package com.example.mad_gp;

import android.content.Context;
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

import java.util.List;

public class CommunityPostAdapter extends RecyclerView.Adapter<CommunityPostAdapter.ViewHolder> {

    private Context context;
    private List<CommunityPost> postList;
    private String currentUserId;

    public CommunityPostAdapter(Context context, List<CommunityPost> postList) {
        this.context = context;
        this.postList = postList;
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

        // 设置头像
        setLocalImage(holder.ivAvatar, post.getUserAvatar());

        // 设置帖子图片 (如果有)
        if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
            holder.cardPostImage.setVisibility(View.VISIBLE);
            setLocalImage(holder.ivPostImage, post.getPostImage());
        } else {
            holder.cardPostImage.setVisibility(View.GONE);
        }

        // --- 点赞状态判断 ---
        boolean isLiked = post.getLikedBy().contains(currentUserId);
        if (isLiked) {
            holder.ivLike.setImageResource(R.drawable.favourite_filled); // 实心红心
            holder.ivLike.setColorFilter(Color.parseColor("#F44336")); // 红色
        } else {
            holder.ivLike.setImageResource(R.drawable.ic_favorite_border); // 空心
            holder.ivLike.setColorFilter(Color.parseColor("#7D8C9A")); // 灰色
        }

        // --- 点赞点击事件 ---
        holder.btnLike.setOnClickListener(v -> {
            if (currentUserId == null) return;

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            if (isLiked) {
                // 取消点赞: 数量-1，从数组移除ID
                db.collection("community_posts").document(post.getPostId())
                        .update("likesCount", FieldValue.increment(-1),
                                "likedBy", FieldValue.arrayRemove(currentUserId));
            } else {
                // 点赞: 数量+1，添加ID到数组
                db.collection("community_posts").document(post.getPostId())
                        .update("likesCount", FieldValue.increment(1),
                                "likedBy", FieldValue.arrayUnion(currentUserId));
            }
            // 界面会自动刷新，因为我们在 Activity 里监听了 snapshot
        });
    }

    private void setLocalImage(ImageView iv, String imgName) {
        if (imgName == null || imgName.isEmpty()) return;
        int resId = context.getResources().getIdentifier(imgName, "drawable", context.getPackageName());
        if (resId != 0) iv.setImageResource(resId);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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