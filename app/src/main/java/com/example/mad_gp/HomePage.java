package com.example.mad_gp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomePage extends AppCompatActivity {

    private TextView tvGreeting, tvDate, titleEmotion;
    private LinearLayout layoutEmojis; // 需要控制这个的显示/隐藏
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
        titleEmotion = findViewById(R.id.titleEmotion);
        layoutEmojis = findViewById(R.id.layoutEmojis); // 记得在 XML 里确认这个 ID

        // 绑定图表控件
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

        // 设置表情点击事件
        setupMoodListeners();

        // 【关键修改】检查今天是否已打卡
        checkIfMoodLoggedToday();

        // 【关键修改】开启实时图表监听
        setupRealtimeMoodChart();

        // --- 6. 设置点击事件 ---
        cardBreathe.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, BrathingExercise.class);
            startActivity(intent);
        });

        cardMusic.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, MusicList.class);
            startActivity(intent);
        });

        navHome.setOnClickListener(v -> {});
        navEvent.setOnClickListener(v -> startActivity(new Intent(HomePage.this, Event.class)));
        navSocial.setOnClickListener(v -> startActivity(new Intent(HomePage.this, CommunityFeed.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(HomePage.this, ProfilePage.class)));
    }

    // --- 新功能：检查今天是否已经记录过心情 ---
    private void checkIfMoodLoggedToday() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // 获取今天的日期字符串
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        // 查询数据库里 date 字段等于今天的数据
        db.collection("users").document(user.getUid())
                .collection("mood_logs")
                .whereEqualTo("date", todayDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // 如果找到了记录，说明今天已经打卡了
                        hideMoodInputUI();
                    } else {
                        // 没找到，显示输入框
                        showMoodInputUI();
                    }
                })
                .addOnFailureListener(e -> {
                    // 查询失败默认显示，以免卡死
                    showMoodInputUI();
                });
    }

    private void hideMoodInputUI() {
        if (layoutEmojis != null) {
            layoutEmojis.setVisibility(View.GONE); // 隐藏表情栏
        }
        if (titleEmotion != null) {
            titleEmotion.setText("Check-in complete for today! \nSee you tomorrow."); // 修改标题
        }
    }

    private void showMoodInputUI() {
        if (layoutEmojis != null) {
            layoutEmojis.setVisibility(View.VISIBLE); // 显示表情栏
        }
        if (titleEmotion != null) {
            titleEmotion.setText("How are you feeling today?"); // 恢复标题
        }
    }

    // --- 修改功能：实时监听图表数据 (取代了原来的 loadMoodTrendChart) ---
    private void setupRealtimeMoodChart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || moodChart == null) return;

        // 使用 addSnapshotListener 替代 get()
        // 这样只要后台数据有变化（包括你在后台删除数据），这里会立刻收到通知
        db.collection("users").document(user.getUid())
                .collection("mood_logs")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(7) // 修改：获取最近30条数据，而不是7条
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            moodChart.setNoDataText("Error loading data");
                            return;
                        }

                        if (value != null && !value.isEmpty()) {
                            List<Entry> entries = new ArrayList<>();
                            List<String> xLabels = new ArrayList<>();

                            int index = 0;
                            // 遍历数据
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                Long val = doc.getLong("moodValue");
                                if (val != null) {
                                    entries.add(new Entry(index, val.floatValue()));

                                    String dateStr = doc.getString("date");
                                    if (dateStr != null && dateStr.length() >= 5) {
                                        xLabels.add(dateStr.substring(5)); // 取 MM-dd
                                    } else {
                                        xLabels.add("");
                                    }
                                    index++;
                                }
                            }
                            // 刷新图表
                            displayChart(entries, xLabels);

                            // 再次检查一下今天的数据状态（处理用户在后台删除了今天数据的情况）
                            // 如果用户在后台删了今天的记录，我们希望App能重新允许打卡
                            checkIfMoodLoggedToday();

                        } else {
                            // 数据为空（比如全删光了）
                            moodChart.clear(); // 清空图表
                            moodChart.setNoDataText("No mood data yet.");
                            // 如果没有数据，肯定也没打卡，显示输入框
                            showMoodInputUI();
                        }
                    }
                });
    }

    private void displayChart(List<Entry> entries, List<String> xLabels) {
        if (moodChart == null) return;

        LineDataSet dataSet = new LineDataSet(entries, "Mood Trend");

        dataSet.setColor(Color.parseColor("#6200EE"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(Color.parseColor("#3700B3"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#BB86FC"));
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        moodChart.setData(lineData);

        XAxis xAxis = moodChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        moodChart.getAxisRight().setEnabled(false);
        moodChart.getAxisLeft().setAxisMinimum(0f);
        moodChart.getAxisLeft().setAxisMaximum(6f);

        moodChart.getDescription().setEnabled(false);
        moodChart.getLegend().setEnabled(false);

        // --- 关键修改：让图表可以水平滑动，显示更多数据 ---
        // 每次屏幕只显示最近的 7 个点，用户可以往左滑看以前的
        moodChart.setVisibleXRangeMaximum(7);
        // 自动移动到最右边（最新的数据）
        moodChart.moveViewToX(entries.size());

        moodChart.notifyDataSetChanged();
        moodChart.invalidate();
    }

    private void setupMoodListeners() {
        ImageView ivShock = findViewById(R.id.ivShock);
        ImageView ivDisgusted = findViewById(R.id.ivDisgusted);
        ImageView ivCute = findViewById(R.id.ivCute);
        ImageView ivSmile = findViewById(R.id.ivSmile);
        ImageView ivHappy = findViewById(R.id.ivHappy);

        if (ivShock == null) return;

        ivShock.setOnClickListener(v -> saveMoodToFirestore(1, "Shock"));
        ivDisgusted.setOnClickListener(v -> saveMoodToFirestore(2, "Disgusted"));
        ivCute.setOnClickListener(v -> saveMoodToFirestore(3, "Neutral"));
        ivSmile.setOnClickListener(v -> saveMoodToFirestore(4, "Smile"));
        ivHappy.setOnClickListener(v -> saveMoodToFirestore(5, "Happy"));
    }

    private void saveMoodToFirestore(int value, String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        Map<String, Object> moodData = new HashMap<>();
        moodData.put("moodValue", value);
        moodData.put("moodName", name);
        moodData.put("date", todayDate);
        moodData.put("timestamp", Timestamp.now());

        db.collection("users").document(user.getUid())
                .collection("mood_logs")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(HomePage.this, "Mood logged: " + name, Toast.LENGTH_SHORT).show();
                    // 保存成功后，立即隐藏输入框
                    hideMoodInputUI();
                    // 注意：不需要手动调用 updateChart，因为上面的 SnapshotListener 会自动监听到数据变化并刷新
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomePage.this, "Failed to log mood", Toast.LENGTH_SHORT).show();
                });
    }

    // --- 原有逻辑保持不变 ---
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
}