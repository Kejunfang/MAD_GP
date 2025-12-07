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

import java.text.SimpleDateFormat; // 导入日期格式化工具
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomePage extends AppCompatActivity {

    private TextView tvGreeting, tvDate; // 新增 tvDate
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
        tvGreeting = findViewById(R.id.tvGreeting);
        tvDate = findViewById(R.id.tvDate); // 绑定日期控件

        FrameLayout cardBreathe = findViewById(R.id.cardBreathe);
        FrameLayout cardMusic = findViewById(R.id.cardMusic);
        MaterialCardView cardFeaturedArticle = findViewById(R.id.cardFeaturedArticle);
        ImageView chartPlaceholder = findViewById(R.id.chartPlaceholder);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // --- 3. 更新界面数据 (问候语 + 日期) ---
        updateGreetingUI();
        updateDate(); // 新增：更新日期

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
                // 当前页，无需跳转
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

    // --- 新增方法：获取并显示今天日期 ---
    private void updateDate() {
        // 格式示例: "October 26, Thursday"
        // MMMM = 全写月份, dd = 日, EEEE = 全写星期
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, EEEE", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        tvDate.setText(currentDate);
    }

    // --- 更新问候语逻辑 ---
    private void updateGreetingUI() {
        String timeGreeting = getTimeBasedGreeting();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name == null || name.isEmpty()) {
                                name = "Friend";
                            }
                            tvGreeting.setText(timeGreeting + ",\n" + name + ".");
                        } else {
                            tvGreeting.setText(timeGreeting + ",\nFriend.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvGreeting.setText(timeGreeting + ".");
                    });
        } else {
            tvGreeting.setText(timeGreeting + ".");
        }
    }

    // --- 辅助方法：判断时间段 ---
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