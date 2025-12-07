package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView; // 记得导入 ImageView
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // 记得导入 Glide
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilePage extends AppCompatActivity {

    // 1. 声明 UI 控件变量
    private TextView tvUserName, tvUserBio, tvEventCount, tvPostCount;
    private ImageView ivProfileImage; // 新增头像控件声明

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
        ivProfileImage = findViewById(R.id.ivProfileImage); // 绑定头像控件

        // --- 加载用户数据 ---
        loadUserProfile();

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

                            // 3. --- 处理头像逻辑 (重点修改在这里) ---
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            // 判断 URL 是否存在且不为空字符串
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                // 如果有 URL，用 Glide 加载
                                Glide.with(ProfilePage.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_person_add) // 加载过程中显示的默认图
                                        .error(R.drawable.ic_person_add)       // 加载失败显示的默认图
                                        .centerCrop()                   // 确保图片填满圆形
                                        .into(ivProfileImage);
                            } else {
                                // 如果 URL 为空，直接设置默认图
                                ivProfileImage.setImageResource(R.drawable.ic_person_add);
                            }

                        } else {
                            Toast.makeText(ProfilePage.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfilePage.this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfilePage.this, Login.class));
            finish();
        }
    }
}