package com.example.mad_gp;

import android.content.Intent;
import android.graphics.Color; // 用于图表颜色
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections; // 用于排序列表
import java.util.Comparator; // 用于排序比较器
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomePage extends AppCompatActivity {

    private TextView tvGreeting, tvDate;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // RecyclerView 相关变量
    private RecyclerView rvDailyTips;
    private DailyTipsAdapter tipsAdapter;
    private List<DailyTip> tipsList;

    // 图表变量
    private LineChart moodChart;

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

        // 绑定图表控件 (请确保 XML 中已经替换为 LineChart)
        moodChart = findViewById(R.id.moodChart);

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

        // 底部导航栏
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        // --- 5. 加载数据 ---
        updateGreetingUI();
        updateDate();
        loadAllDailyTips();

        // 新增：设置表情点击事件和加载图表
        setupMoodListeners();
        loadMoodTrendChart();

        // --- 6. 设置点击事件 ---

        cardBreathe.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, BrathingExercise.class);
            startActivity(intent);
        });

        cardMusic.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, MusicList.class);
            startActivity(intent);
        });

        // 注意：原先的 chartPlaceholder 点击事件已移除，因为现在是真实的图表了

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

    private void loadAllDailyTips() {
        db.collection("daily_tips")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        tipsList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String title = doc.getString("title");
                            String subtitle = doc.getString("subtitle");
                            String imageUrl = doc.getString("imageUrl");

                            tipsList.add(new DailyTip(id, title, subtitle, imageUrl));
                        }
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

    // --- 新增功能：表情监听 ---
    private void setupMoodListeners() {
        // 绑定控件 (请确保 XML 中这些 ImageView 有对应的 ID)
        ImageView ivShock = findViewById(R.id.ivShock);
        ImageView ivDisgusted = findViewById(R.id.ivDisgusted);
        ImageView ivCute = findViewById(R.id.ivCute);
        ImageView ivSmile = findViewById(R.id.ivSmile);
        ImageView ivHappy = findViewById(R.id.ivHappy);

        // 如果找不到控件 (比如 XML 还没改)，为了防止闪退，加个判空
        if (ivShock == null) return;

        // 设置点击事件 (分数对应：Shock=1, Disgusted=2, Neutral=3, Smile=4, Happy=5)
        ivShock.setOnClickListener(v -> saveMoodToFirestore(1, "Shock"));
        ivDisgusted.setOnClickListener(v -> saveMoodToFirestore(2, "Disgusted"));
        ivCute.setOnClickListener(v -> saveMoodToFirestore(3, "Neutral"));
        ivSmile.setOnClickListener(v -> saveMoodToFirestore(4, "Smile"));
        ivHappy.setOnClickListener(v -> saveMoodToFirestore(5, "Happy"));
    }

    // --- 新增功能：保存心情到 Firebase ---
    private void saveMoodToFirestore(int value, String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        // 准备数据
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("moodValue", value);
        moodData.put("moodName", name);
        moodData.put("date", todayDate);
        moodData.put("timestamp", Timestamp.now());

        // 保存到 users -> userId -> mood_logs -> (自动ID)
        db.collection("users").document(user.getUid())
                .collection("mood_logs")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(HomePage.this, "Mood logged: " + name, Toast.LENGTH_SHORT).show();
                    // 保存成功后，刷新图表
                    loadMoodTrendChart();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomePage.this, "Failed to log mood", Toast.LENGTH_SHORT).show();
                });
    }

    // --- 新增功能：加载心情趋势图表 ---
    private void loadMoodTrendChart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || moodChart == null) return;

        // 获取过去的数据
        db.collection("users").document(user.getUid())
                .collection("mood_logs")
                .orderBy("timestamp", Query.Direction.ASCENDING) // 按时间正序
                .limit(7) // 只拿最近7次记录
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Entry> entries = new ArrayList<>();
                    List<String> xLabels = new ArrayList<>();

                    int index = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Long val = doc.getLong("moodValue");
                        if (val != null) {
                            // X轴用 index (0, 1, 2...)，Y轴用 moodValue
                            entries.add(new Entry(index, val.floatValue()));

                            // 获取日期用于 X轴标签
                            String dateStr = doc.getString("date");
                            if (dateStr != null && dateStr.length() >= 5) {
                                xLabels.add(dateStr.substring(5)); // 只取 MM-dd
                            } else {
                                xLabels.add("D" + (index + 1));
                            }
                            index++;
                        }
                    }

                    if (!entries.isEmpty()) {
                        displayChart(entries, xLabels);
                    } else {
                        moodChart.setNoDataText("No mood data yet. Tap an emoji!");
                        moodChart.invalidate();
                    }
                })
                .addOnFailureListener(e -> {
                    if (moodChart != null) moodChart.setNoDataText("Error loading data.");
                });
    }

    // --- 新增功能：配置并显示图表 ---
    private void displayChart(List<Entry> entries, List<String> xLabels) {
        if (moodChart == null) return;

        LineDataSet dataSet = new LineDataSet(entries, "Mood Trend");

        // 设置线条样式
        dataSet.setColor(Color.parseColor("#6200EE")); // 使用你的主题色
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(Color.parseColor("#3700B3"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false); // 不在线上显示具体数值
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // 设置为平滑曲线

        // 填充颜色 (可选)
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#BB86FC"));
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        moodChart.setData(lineData);

        // 设置 X 轴
        XAxis xAxis = moodChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f); // 确保标签不重叠
        xAxis.setDrawGridLines(false);

        // 设置 Y 轴
        moodChart.getAxisRight().setEnabled(false); // 隐藏右侧 Y 轴
        moodChart.getAxisLeft().setAxisMinimum(0f); // Y轴最小值为0
        moodChart.getAxisLeft().setAxisMaximum(6f); // Y轴最大值为6 (因为心情是1-5)

        // 其他设置
        moodChart.getDescription().setEnabled(false); // 隐藏描述
        moodChart.getLegend().setEnabled(false); // 隐藏图例
        moodChart.animateY(1000); // 增加动画效果
        moodChart.invalidate(); // 刷新图表
    }
}