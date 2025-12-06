package com.example.mad_gp; // 记得检查包名

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class UserProfile extends AppCompatActivity {

    private boolean isFollowing = false; // 记录是否已关注

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 确保这里对应的是你之前创建的他人主页 XML 文件名
        setContentView(R.layout.activity_user_profile);

        // --- 1. 初始化控件 ---
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvUserName = findViewById(R.id.tvUserName);
        MaterialButton btnFollow = findViewById(R.id.btnFollow);
        MaterialButton btnMessage = findViewById(R.id.btnMessage);

        // (可选) 如果是从其他页面跳过来并传了名字，可以在这里接收
        // String name = getIntent().getStringExtra("USER_NAME");
        // if (name != null) tvUserName.setText(name);

        // --- 2. 返回按钮 ---
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 关闭当前页面
            }
        });

        // --- 3. 关注按钮逻辑 (模拟切换状态) ---
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing) {
                    // 如果已经是关注状态 -> 取消关注
                    isFollowing = false;
                    btnFollow.setText("Follow");
                    // 恢复成绿色背景
                    btnFollow.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.sage_green)));
                    btnFollow.setTextColor(getResources().getColor(R.color.white));
                    Toast.makeText(UserProfile.this, "Unfollowed", Toast.LENGTH_SHORT).show();
                } else {
                    // 如果没关注 -> 变成已关注
                    isFollowing = true;
                    btnFollow.setText("Following");
                    // 变成灰色背景，表示已操作
                    btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
                    btnFollow.setTextColor(getResources().getColor(R.color.text_main));
                    Toast.makeText(UserProfile.this, "Following!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // --- 4. 聊天按钮逻辑 ---
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到聊天页面 (ChatPage)
                Intent intent = new Intent(UserProfile.this, Chat.class);
                // 可选：把名字传给聊天页，让聊天页标题显示 "Sarah Tan"
                intent.putExtra("USER_NAME", tvUserName.getText().toString());
                startActivity(intent);
            }
        });
    }
}