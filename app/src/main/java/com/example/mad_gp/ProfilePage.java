package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfilePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 确保这里的布局文件名和你 res/layout/ 下的一致 (例如 activity_profile.xml)
        setContentView(R.layout.activity_profile_page);

        // --- 初始化底部导航栏控件 ---
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // --- 设置点击事件 ---

        // 1. 跳转到 Home (首页)
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePage.this, HomePage.class);
                // 这是一个好习惯：如果首页已经在下面，就回到首页，而不是再开一个新的
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                // 可选：finish(); // 如果你想让用户按返回键直接退出应用，可以加上 finish()
            }
        });

        // 2. 跳转到 Event (活动页)
        navEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确保你已经创建了 Event.java
                Intent intent = new Intent(ProfilePage.this, Event.class);
                startActivity(intent);
            }
        });

        // 3. 跳转到 Social (社区页)
        navSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 假设你的社交页面叫 CommunityFeed (根据你的文件结构)
                // 如果你的社交页面叫 Social.java，请把 CommunityFeed.class 改成 Social.class
                //Intent intent = new Intent(Profile.this, CommunityFeed.class);
                //startActivity(intent);
            }
        });

        // 4. Profile (当前页)
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 因为已经在 Profile 页面了，所以这里什么都不用做，或者可以做个刷新
                Toast.makeText(ProfilePage.this, "You are already here!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}