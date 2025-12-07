package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class HomePage extends AppCompatActivity {

    private TextView tvGreeting;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        // --- 1. 初始化 Firebase ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- 2. 初始化控件 ---
        tvGreeting = findViewById(R.id.tvGreeting); // 绑定问候语的 TextView
        FrameLayout cardBreathe = findViewById(R.id.cardBreathe);
        FrameLayout cardMusic = findViewById(R.id.cardMusic);
        MaterialCardView cardFeaturedArticle = findViewById(R.id.cardFeaturedArticle);
        ImageView chartPlaceholder = findViewById(R.id.chartPlaceholder);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // --- 3. 更新问候语和名字 ---
        updateGreetingUI();

        // --- 4. 设置点击事件 ---

        cardBreathe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, BrathingExercise.class);
                startActivity(intent);
            }
        });

        cardMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MusicList.class);
                startActivity(intent);
            }
        });

        cardFeaturedArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, DailyTipsDetails.class);
                startActivity(intent);
            }
        });

        chartPlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Opening Mood Trend Details...", Toast.LENGTH_SHORT).show();
            }
        });

        // 底部导航栏逻辑
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当前就在 HomePage，不需要跳转
            }
        });

        navEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Event.class);
                startActivity(intent);
            }
        });

        navSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, CommunityFeed.class);
                startActivity(intent);
            }
        });

        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, ProfilePage.class);
                startActivity(intent);
            }
        });
    }

    // --- 新增方法：更新问候语逻辑 ---
    private void updateGreetingUI() {
        // 1. 获取当前时间段的问候语
        String timeGreeting = getTimeBasedGreeting();

        // 2. 获取当前用户
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            // 3. 从 Firestore 读取名字
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // 获取数据库里的名字
                            String name = documentSnapshot.getString("name");
                            if (name == null || name.isEmpty()) {
                                name = "Friend"; // 如果没名字，默认叫 Friend
                            }
                            // 组合文字：例如 "Good Morning,\nJunYi."
                            tvGreeting.setText(timeGreeting + ",\n" + name + ".");
                        } else {
                            tvGreeting.setText(timeGreeting + ",\nFriend.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 如果读取失败，至少显示个问候
                        tvGreeting.setText(timeGreeting + ".");
                    });
        } else {
            // 如果没登录
            tvGreeting.setText(timeGreeting + ".");
        }
    }

    // --- 辅助方法：判断时间 ---
    private String getTimeBasedGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 5 && timeOfDay < 12) {
            return "Good Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 18) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }
}