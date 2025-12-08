package com.example.mad_gp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log; // 引入 Log
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
import java.util.List;

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
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");

        if (targetUserId == null || targetUserId.isEmpty()) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserInfo();
        loadUserPosts(); // ★ 重点看这里
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

    // ★★★ 修改后的加载帖子方法 (带报错提示) ★★★
    private void loadUserPosts() {
        // 为了调试，我们在 Logcat 打印一下 ID
        Log.d("UserProfile", "Loading posts for user: " + targetUserId);

        db.collection("community_posts")
                .whereEqualTo("userId", targetUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userPostList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        // 情况 1: 查询成功，但这个人没发过贴
                        Toast.makeText(this, "This user has no posts.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 情况 2: 查询成功，有帖子
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
                    // ★★★ 情况 3: 查询失败 (最可能是缺少 Index) ★★★
                    // 把错误打在屏幕上，方便你直接看
                    Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("UserProfile", "Error loading posts", e);
                });
    }

    private void setupButtons() {
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid().equals(targetUserId)) {
            btnFollow.setVisibility(View.GONE);
            btnMessage.setVisibility(View.GONE);
        }

        btnFollow.setOnClickListener(v -> {
            if (isFollowing) {
                isFollowing = false;
                btnFollow.setText("Follow");
                btnFollow.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.sage_green)));
                btnFollow.setTextColor(getResources().getColor(R.color.white));
            } else {
                isFollowing = true;
                btnFollow.setText("Following");
                btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
                btnFollow.setTextColor(getResources().getColor(R.color.text_main));
            }
        });

        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, Chat.class);
            intent.putExtra("USER_NAME", tvUserName.getText().toString());
            intent.putExtra("TARGET_USER_ID", targetUserId);
            startActivity(intent);
        });
    }
}