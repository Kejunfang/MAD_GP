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
    private LinearLayout layoutEmojis;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private RecyclerView rvDailyTips;
    private DailyTipsAdapter tipsAdapter;
    private List<DailyTip> tipsList;

    private LineChart moodChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvGreeting = findViewById(R.id.tvGreeting);
        tvDate = findViewById(R.id.tvDate);
        titleEmotion = findViewById(R.id.titleEmotion);
        layoutEmojis = findViewById(R.id.layoutEmojis);

        moodChart = findViewById(R.id.moodChart);

        rvDailyTips = findViewById(R.id.rvDailyTips);
        rvDailyTips.setLayoutManager(new LinearLayoutManager(this));

        tipsList = new ArrayList<>();
        tipsAdapter = new DailyTipsAdapter(this, tipsList);
        rvDailyTips.setAdapter(tipsAdapter);

        FrameLayout cardBreathe = findViewById(R.id.cardBreathe);
        FrameLayout cardMusic = findViewById(R.id.cardMusic);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        updateGreetingUI();
        updateDate();
        loadAllDailyTips();

        setupMoodListeners();

        checkIfMoodLoggedToday();

        setupRealtimeMoodChart();

        cardBreathe.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, BrathingExercise.class);
            startActivity(intent);
        });

        cardMusic.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, MusicList.class);
            startActivity(intent);
        });

        navHome.setOnClickListener(v -> {});
        navEvent.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, Event.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0); // 【关键】取消跳转动画
        });

        navSocial.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, CommunityFeed.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0); // 【关键】取消跳转动画
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, ProfilePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0); // 【关键】取消跳转动画
        });
    }

    private void checkIfMoodLoggedToday() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        db.collection("users").document(user.getUid())
                .collection("mood_logs")
                .whereEqualTo("date", todayDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Today already click
                        hideMoodInputUI();
                    } else {
                        // if not found
                        showMoodInputUI();
                    }
                })
                .addOnFailureListener(e -> {
                    // default show
                    showMoodInputUI();
                });
    }

    private void hideMoodInputUI() {
        if (layoutEmojis != null) {
            layoutEmojis.setVisibility(View.GONE);
        }
        if (titleEmotion != null) {
            titleEmotion.setText("Check-in complete for today! \nSee you tomorrow.");
        }
    }

    private void showMoodInputUI() {
        if (layoutEmojis != null) {
            layoutEmojis.setVisibility(View.VISIBLE);
        }
        if (titleEmotion != null) {
            titleEmotion.setText("How are you feeling today?");
        }
    }

    private void setupRealtimeMoodChart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || moodChart == null) return;

        db.collection("users").document(user.getUid())
                .collection("mood_logs")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(7) //
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
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                Long val = doc.getLong("moodValue");
                                if (val != null) {
                                    entries.add(new Entry(index, val.floatValue()));

                                    String dateStr = doc.getString("date");
                                    if (dateStr != null && dateStr.length() >= 5) {
                                        xLabels.add(dateStr.substring(5));
                                    } else {
                                        xLabels.add("");
                                    }
                                    index++;
                                }
                            }
                            // update the graph
                            displayChart(entries, xLabels);

                            checkIfMoodLoggedToday();

                        } else {
                            moodChart.clear();
                            moodChart.setNoDataText("No mood data yet.");
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

        moodChart.setVisibleXRangeMaximum(7);

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
                    hideMoodInputUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomePage.this, "Failed to log mood", Toast.LENGTH_SHORT).show();
                });
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
}