package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // 如果未来用网络图片，这个还是需要的
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilePage extends AppCompatActivity {

    // 1. 声明 UI 控件变量
    private TextView tvUserName, tvUserBio, tvEventCount, tvPostCount;
    private ImageView ivProfileImage, btnEditProfile;

    // 2. 声明 Firebase 变量
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        // --- 初始化 Firebase ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- 绑定 UI 控件 ---
        tvUserName = findViewById(R.id.tvUserName);
        tvUserBio = findViewById(R.id.tvUserBio);
        tvEventCount = findViewById(R.id.tvEventCount);
        tvPostCount = findViewById(R.id.tvPostCount);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnEditProfile = findViewById(R.id.btnEditProfile); // 绑定编辑按钮

        // --- 编辑按钮点击事件 ---
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, EditProfile.class);
            startActivity(intent);
        });

        // --- 初始化底部导航栏控件 ---
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // --- 设置导航栏点击事件 ---
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        navEvent.setOnClickListener(v -> {
            startActivity(new Intent(ProfilePage.this, Event.class));
        });

        navSocial.setOnClickListener(v -> {
            startActivity(new Intent(ProfilePage.this, CommunityFeed.class));
        });

        navProfile.setOnClickListener(v -> {
            Toast.makeText(ProfilePage.this, "You are already here!", Toast.LENGTH_SHORT).show();
        });
    }

    // --- 关键生命周期：onResume ---
    // 当从 EditProfile 页面返回时，这个方法会自动运行，刷新数据
    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    // --- 从 Firestore 读取用户数据并更新 UI ---
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // 1. 获取名字
                            String name = documentSnapshot.getString("name");
                            tvUserName.setText(name != null ? name : "No Name");

                            // 2. 获取简介 (Bio)
                            String bio = documentSnapshot.getString("bio");
                            if (bio != null && !bio.isEmpty()) {
                                tvUserBio.setText(bio);
                            } else {
                                tvUserBio.setText("This user hasn't written a bio yet.");
                            }

                            // 3. --- 处理头像 (无 Storage 版) ---
                            // 我们读取的是字符串代号，例如 "counsellor1"
                            String avatarTag = documentSnapshot.getString("profileImageUrl");

                            if (avatarTag != null && !avatarTag.isEmpty()) {
                                // 兼容性检查：如果是 http 开头的链接 (以防万一以后用了网络图)，用 Glide
                                if (avatarTag.startsWith("http")) {
                                    Glide.with(ProfilePage.this)
                                            .load(avatarTag)
                                            .placeholder(R.drawable.ic_default_avatar)
                                            .centerCrop()
                                            .into(ivProfileImage);
                                } else {
                                    // 核心逻辑：如果是代号，直接查找本地资源 ID 并设置
                                    // 这里调用了 EditProfile 里我们写好的静态方法
                                    int resId = EditProfile.getAvatarResourceId(avatarTag);
                                    ivProfileImage.setImageResource(resId);
                                }
                            } else {
                                // 如果没数据，显示默认图
                                ivProfileImage.setImageResource(R.drawable.ic_default_avatar);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 加载失败 (通常不用弹窗打扰用户，除非调试)
                        // Toast.makeText(ProfilePage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // 如果未登录，跳转回登录页
            startActivity(new Intent(ProfilePage.this, Login.class));
            finish();
        }
    }
}