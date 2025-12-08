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
    private String currentUserId;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");

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
        setupButtons();
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
        // 这里的 Context 传 this 即可
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

    // ★★★ 核心修复：改用 addSnapshotListener 实现实时刷新 ★★★
    private void loadUserPosts() {
        Log.d("UserProfile", "Loading posts for user: " + targetUserId);

        // 使用 addSnapshotListener 替换 get()
        // 传入 this 是为了让监听器跟随 Activity 生命周期自动销毁，防止内存泄漏
        db.collection("community_posts")
                .whereEqualTo("userId", targetUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Load failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("UserProfile", "Error loading posts", error);
                        return;
                    }

                    userPostList.clear();
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CommunityPost post = doc.toObject(CommunityPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId()); // 这一步很重要，否则点赞找不到文档ID
                                userPostList.add(post);
                            }
                        }
                    }
                    // 刷新列表，此时 Adapter 会重新判断 isLiked，红心就会变色
                    postAdapter.notifyDataSetChanged();
                });
    }

    private void setupButtons() {
        if (currentUserId != null && currentUserId.equals(targetUserId)) {
            btnFollow.setVisibility(View.GONE);
            btnMessage.setVisibility(View.GONE);
            return;
        }

        checkFollowStatus();

        btnFollow.setOnClickListener(v -> {
            if (currentUserId == null) return;

            btnFollow.setEnabled(false);

            if (isFollowing) {
                // 取消关注
                db.collection("users").document(currentUserId)
                        .collection("following").document(targetUserId)
                        .delete();

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
                // 关注
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", com.google.firebase.Timestamp.now());

                db.collection("users").document(currentUserId)
                        .collection("following").document(targetUserId)
                        .set(data);

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

        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, Chat.class);
            intent.putExtra("USER_NAME", tvUserName.getText().toString());
            intent.putExtra("TARGET_USER_ID", targetUserId);
            startActivity(intent);
        });
    }

    private void checkFollowStatus() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .collection("following").document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFollowing = documentSnapshot.exists();
                    updateFollowButtonUI();
                });
    }

    private void updateFollowButtonUI() {
        if (isFollowing) {
            btnFollow.setText("Following");
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            btnFollow.setTextColor(getResources().getColor(R.color.text_main));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.sage_green)));
            btnFollow.setTextColor(getResources().getColor(R.color.white));
        }
    }
}