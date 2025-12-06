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
        // 确保这里的 XML 文件名和你 res/layout/ 下的一致
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


        // --- 2. 设置功能按钮点击事件 ---

        // 聊天按钮
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 暂时弹窗，未来可以跳转到 ChatListActivity
                Intent intent = new Intent(CommunityFeed.this,ChatList.class);
                startActivity(intent);
                Toast.makeText(CommunityFeed.this, "Opening Chats...", Toast.LENGTH_SHORT).show();
            }
        });

        // 发布动态按钮 (FAB)
        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityFeed.this, CreatePost.class);
                startActivity(intent);
                Toast.makeText(CommunityFeed.this, "Create a new post", Toast.LENGTH_SHORT).show();
            }
        });


        // --- 3. 设置底部导航栏跳转逻辑 ---

        // 跳转到 Home
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityFeed.this, HomePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 跳转到 Event
        navEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityFeed.this, Event.class);
                startActivity(intent);
            }
        });

        // Social (当前页面)
        navSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 已经在当前页，不做操作，或者可以刷新列表
                // Toast.makeText(CommunityFeed.this, "Refreshing feed...", Toast.LENGTH_SHORT).show();
            }
        });

        // 跳转到 Profile
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityFeed.this, ProfilePage.class);
                startActivity(intent);
            }
        });
    }
}