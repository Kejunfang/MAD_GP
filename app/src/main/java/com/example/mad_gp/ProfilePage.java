package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy; // 导入缓存策略
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

    // --- 利用 onResume 生命周期 ---
    // 这个方法非常重要：当你从 EditProfile 页面按返回键回来时，onCreate 不会运行，但 onResume 会运行。
    // 所以我们需要在这里刷新数据。
    @Override
    protected void onResume() {
        super.onResume();
        // 每次页面显示出来，都重新拉取数据
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

                            // 3. 处理头像逻辑
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                // 使用 Glide 加载
                                Glide.with(ProfilePage.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_person_add) // 加载中显示的图
                                        .error(R.drawable.ic_person_add)       // 错误时显示的图
                                        .centerCrop()
                                        // --- 关键修改：强制跳过缓存 ---
                                        // 这样每次回到页面，Glide 都会认为图片可能变了，从而重新下载
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(ivProfileImage);
                            } else {
                                ivProfileImage.setImageResource(R.drawable.ic_person_add);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 加载失败的处理，通常不需要弹窗打扰用户，除非调试
                        // Toast.makeText(ProfilePage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // 如果未登录，跳转回登录页
            startActivity(new Intent(ProfilePage.this, Login.class));
            finish();
        }
    }
}