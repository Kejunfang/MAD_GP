package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CommunityFeed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_feed);

        // --- 1. 初始化控件 ---

        // 顶部聊天按钮 & 悬浮发布按钮
        ImageButton btnChat = findViewById(R.id.btnChat);
        FloatingActionButton fabPost = findViewById(R.id.fabPost);

        // 底部导航栏
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // 【新增】获取动态里的头像 (请确保你在 XML 里加了 ID)
        View ivAvatar1 = findViewById(R.id.ivAvatar1); // Alex Chen 的头像

        // --- 2. 设置头像点击跳转 (UserProfile) ---

        if (ivAvatar1 != null) {
            ivAvatar1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CommunityFeed.this, UserProfile.class);
                    // (可选) 传递用户名给下一个页面，这样 UserProfile 可以显示不同的名字
                    intent.putExtra("USER_NAME", "Alex Chen");
                    startActivity(intent);
                }
            });
        }


        // --- 3. 设置其他功能按钮点击事件 ---

        // 聊天按钮
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到聊天列表页
                Intent intent = new Intent(CommunityFeed.this, ChatList.class);
                startActivity(intent);
            }
        });

        // 发布动态按钮 (FAB)
        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到发布页面
                // 假设你还没有 CreatePostActivity，这里先保留 Toast 或者跳转逻辑
                Intent intent = new Intent(CommunityFeed.this, CreatePost.class); // 如果还没建这个文件会报错，请先建好
                startActivity(intent);
            }
        });


        // --- 4. 设置底部导航栏跳转逻辑 ---

        // 跳转到 Home
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityFeed.this, HomePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // 跳转到 Event
        navEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityFeed.this, Event.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // Social (当前页面)
        navSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 已经在当前页
            }
        });

        // 跳转到 Profile (我的个人主页)
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityFeed.this, ProfilePage.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}