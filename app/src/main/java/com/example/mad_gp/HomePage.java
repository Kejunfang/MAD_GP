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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomePage extends AppCompatActivity {

    private TextView tvGreeting, tvDate;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // --- 新增：RecyclerView 相关变量 ---
    private RecyclerView rvDailyTips;
    private DailyTipsAdapter tipsAdapter;
    private List<DailyTip> tipsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        // --- 1. 初始化 Firebase ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- 2. 绑定基本控件 ---
        tvGreeting = findViewById(R.id.tvGreeting);
        tvDate = findViewById(R.id.tvDate);

        // --- 3. 初始化 RecyclerView ---
        rvDailyTips = findViewById(R.id.rvDailyTips);
        rvDailyTips.setLayoutManager(new LinearLayoutManager(this));

        // 初始化列表和适配器
        tipsList = new ArrayList<>();
        tipsAdapter = new DailyTipsAdapter(this, tipsList);
        rvDailyTips.setAdapter(tipsAdapter);

        // --- 4. 其他卡片控件 ---
        FrameLayout cardBreathe = findViewById(R.id.cardBreathe);
        FrameLayout cardMusic = findViewById(R.id.cardMusic);
        ImageView chartPlaceholder = findViewById(R.id.chartPlaceholder);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // --- 5. 加载数据 ---
        updateGreetingUI();
        updateDate();
        loadAllDailyTips(); // 改名：加载所有 Tips

        // --- 6. 设置点击事件 ---

        cardBreathe.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, BrathingExercise.class);
            startActivity(intent);
        });

        cardMusic.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, MusicList.class);
            startActivity(intent);
        });

        chartPlaceholder.setOnClickListener(v -> Toast.makeText(HomePage.this, "Opening Mood Trend Details...", Toast.LENGTH_SHORT).show());

        navHome.setOnClickListener(v -> {});
        navEvent.setOnClickListener(v -> startActivity(new Intent(HomePage.this, Event.class)));
        navSocial.setOnClickListener(v -> startActivity(new Intent(HomePage.this, CommunityFeed.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(HomePage.this, ProfilePage.class)));
    }

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, EEEE", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvDate.setText(currentDate);
    }

    private void updateGreetingUI() {
        String timeGreeting = getTimeBasedGreeting();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name == null || name.isEmpty()) name = "Friend";
                            tvGreeting.setText(timeGreeting + ",\n" + name + ".");
                        } else {
                            tvGreeting.setText(timeGreeting + ",\nFriend.");
                        }
                    })
                    .addOnFailureListener(e -> tvGreeting.setText(timeGreeting + "."));
        } else {
            tvGreeting.setText(timeGreeting + ".");
        }
    }

    // --- 修改后：加载所有文章 ---
    private void loadAllDailyTips() {
        db.collection("daily_tips")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        tipsList.clear(); // 清空旧数据
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String title = doc.getString("title");
                            String subtitle = doc.getString("subtitle");
                            String imageUrl = doc.getString("imageUrl");

                            // 创建对象并加入列表
                            tipsList.add(new DailyTip(id, title, subtitle, imageUrl));
                        }
                        // 通知适配器刷新界面
                        tipsAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomePage.this, "Failed to load tips", Toast.LENGTH_SHORT).show();
                });
    }

    private String getTimeBasedGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay >= 5 && timeOfDay < 12) return "Good Morning";
        else if (timeOfDay >= 12 && timeOfDay < 18) return "Good Afternoon";
        else return "Good Evening";
    }
}