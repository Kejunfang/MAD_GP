package com.example.mad_gp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfile extends AppCompatActivity {

    private ImageView ivUserAvatar;
    private TextView tvUserName, tvUserBio;
    private MaterialButton btnFollow, btnMessage;
    private RecyclerView rvUserPosts;

    private CommunityPostAdapter postAdapter;
    private List<CommunityPost> userPostList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String targetUserId;
    private String currentUserId; // 新增：当前登录用户的ID
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 获取 Intent 传来的目标用户 ID
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");

        // 获取当前登录用户 ID
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        if (targetUserId == null || targetUserId.isEmpty()) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserInfo();
        loadUserPosts();
        setupButtons(); // 这里面包含了检查关注状态的逻辑
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserBio = findViewById(R.id.tvUserBio);
        btnFollow = findViewById(R.id.btnFollow);
        btnMessage = findViewById(R.id.btnMessage);
        rvUserPosts = findViewById(R.id.rvUserPosts);

        btnBack.setOnClickListener(v -> finish());

        rvUserPosts.setLayoutManager(new LinearLayoutManager(this));
        userPostList = new ArrayList<>();
        postAdapter = new CommunityPostAdapter(this, userPostList);
        rvUserPosts.setAdapter(postAdapter);
    }

    private void loadUserInfo() {
        db.collection("users").document(targetUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvUserName.setText(name != null ? name : "Unknown");

                        String bio = documentSnapshot.getString("bio");
                        tvUserBio.setText(bio != null && !bio.isEmpty() ? bio : "This user hasn't written a bio yet.");

                        String avatarTag = documentSnapshot.getString("profileImageUrl");
                        if (avatarTag != null) {
                            if (avatarTag.startsWith("http")) {
                                Glide.with(this).load(avatarTag).into(ivUserAvatar);
                            } else {
                                int resId = getResources().getIdentifier(avatarTag, "drawable", getPackageName());
                                if (resId != 0) ivUserAvatar.setImageResource(resId);
                                else ivUserAvatar.setImageResource(R.drawable.ic_default_avatar);
                            }
                        }
                    }
                });
    }

    private void loadUserPosts() {
        Log.d("UserProfile", "Loading posts for user: " + targetUserId);

        db.collection("community_posts")
                .whereEqualTo("userId", targetUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userPostList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Toast.makeText(this, "This user has no posts.", Toast.LENGTH_SHORT).show();
                        // 很多人没发过贴，这里不弹 Toast 体验更好，或者显示一个 Empty View
                    } else {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            CommunityPost post = doc.toObject(CommunityPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                userPostList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("UserProfile", "Error loading posts", e);
                });
    }

    // ★★★ 核心修改：检查数据库状态并根据操作写入数据库 ★★★
    private void setupButtons() {
        // 1. 如果是看自己的主页，隐藏关注和私信按钮
        if (currentUserId != null && currentUserId.equals(targetUserId)) {
            btnFollow.setVisibility(View.GONE);
            btnMessage.setVisibility(View.GONE);
            return;
        }

        // 2. 初始检查：我在数据库里是否已经关注了他？
        checkFollowStatus();

        // 3. 点击关注/取消关注逻辑
        btnFollow.setOnClickListener(v -> {
            if (currentUserId == null) return;

            btnFollow.setEnabled(false); // 防止快速连点

            if (isFollowing) {
                // --- 执行取消关注 ---
                // 删除我的 following 记录
                db.collection("users").document(currentUserId)
                        .collection("following").document(targetUserId)
                        .delete();

                // 删除对方的 followers 记录
                db.collection("users").document(targetUserId)
                        .collection("followers").document(currentUserId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            isFollowing = false;
                            updateFollowButtonUI();
                            Toast.makeText(UserProfile.this, "Unfollowed", Toast.LENGTH_SHORT).show();
                            btnFollow.setEnabled(true);
                        })
                        .addOnFailureListener(e -> btnFollow.setEnabled(true));

            } else {
                // --- 执行关注 ---
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", com.google.firebase.Timestamp.now());

                // 写入我的 following
                db.collection("users").document(currentUserId)
                        .collection("following").document(targetUserId)
                        .set(data);

                // 写入对方的 followers
                db.collection("users").document(targetUserId)
                        .collection("followers").document(currentUserId)
                        .set(data)
                        .addOnSuccessListener(aVoid -> {
                            isFollowing = true;
                            updateFollowButtonUI();
                            Toast.makeText(UserProfile.this, "Followed!", Toast.LENGTH_SHORT).show();
                            btnFollow.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(UserProfile.this, "Failed to follow", Toast.LENGTH_SHORT).show();
                            btnFollow.setEnabled(true);
                        });
            }
        });

        // 4. 私信按钮逻辑 (保持不变)
        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, Chat.class);
            intent.putExtra("USER_NAME", tvUserName.getText().toString());
            intent.putExtra("TARGET_USER_ID", targetUserId);
            startActivity(intent);
        });
    }

    // 辅助方法：去数据库查状态
    private void checkFollowStatus() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .collection("following").document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFollowing = documentSnapshot.exists();
                    updateFollowButtonUI(); // 根据查到的结果刷新按钮样子
                });
    }

    // 辅助方法：只负责更新按钮样式
    private void updateFollowButtonUI() {
        if (isFollowing) {
            btnFollow.setText("Following");
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0"))); // 灰色
            btnFollow.setTextColor(getResources().getColor(R.color.text_main));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.sage_green))); // 绿色
            btnFollow.setTextColor(getResources().getColor(R.color.white));
        }
    }
}