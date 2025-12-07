package com.example.mad_gp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppointmentBooking extends AppCompatActivity {

    // 控件变量
    private ImageView counsellorImage, btnBack;
    private TextView counsellorName, counsellorTitle, counsellorLocation;
    private Button bookbtn, backbtn;

    // 日期控件
    private TextView dateMon, dateTue, dateWed, dateThu, dateFri;
    private TextView[] dateViews; // 数组方便管理

    // 时间段控件
    private TextView slotMorning, slotAfternoon, slotEvening;
    private TextView[] slotViews; // 数组方便管理

    // 数据变量
    private Counsellor currentCounsellor;
    private String selectedDate = ""; // 记录用户选的日期
    private String selectedTime = ""; // 记录用户选的时间
    private String selectedLocation = ""; // 记录对应的地点

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 确保这里的布局文件名和你 XML 的名字一致
        setContentView(R.layout.activity_appointment_booking);

        // 初始化 Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // --- 1. 初始化控件 ---
        initViews();

        // --- 2. 接收上一页传来的咨询师数据 ---
        currentCounsellor = (Counsellor) getIntent().getSerializableExtra("COUNSELLOR_DATA");
        if (currentCounsellor != null) {
            setupCounsellorInfo();
        }

        // --- 3. 设置真实的日期数据 (新增功能) ---
        setupRealDates();

        // --- 4. 设置点击事件 ---
        setupDateClickListeners();
        setupTimeClickListeners();

        // --- 5. 按钮逻辑 ---
        backbtn.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        bookbtn.setOnClickListener(v -> handleBooking());
    }

    private void initViews() {
        counsellorImage = findViewById(R.id.counsellorImage);
        counsellorName = findViewById(R.id.counsellorName);
        counsellorTitle = findViewById(R.id.counsellorTitle);
        counsellorLocation = findViewById(R.id.counsellorLocation);
        bookbtn = findViewById(R.id.bookbtn);
        backbtn = findViewById(R.id.backbtn);
        btnBack = findViewById(R.id.btnBack);

        // 日期 Views
        dateMon = findViewById(R.id.dateMon);
        dateTue = findViewById(R.id.dateTue);
        dateWed = findViewById(R.id.dateWed);
        dateThu = findViewById(R.id.dateThu);
        dateFri = findViewById(R.id.dateFri);
        dateViews = new TextView[]{dateMon, dateTue, dateWed, dateThu, dateFri};

        // 时间 Views
        slotMorning = findViewById(R.id.slotMorning);
        slotAfternoon = findViewById(R.id.slotAfternoon);
        slotEvening = findViewById(R.id.slotEvening);
        slotViews = new TextView[]{slotMorning, slotAfternoon, slotEvening};
    }

    private void setupCounsellorInfo() {
        counsellorName.setText(currentCounsellor.getName());
        counsellorTitle.setText(currentCounsellor.getTitle());
        counsellorLocation.setText(currentCounsellor.getLocation());

        String imgName = currentCounsellor.getImageName();
        if (imgName != null && !imgName.isEmpty()) {
            int resId = getResources().getIdentifier(imgName, "drawable", getPackageName());
            if (resId != 0) counsellorImage.setImageResource(resId);
        }
    }

    // --- 新增：计算并显示本周日期 ---
    private void setupRealDates() {
        Calendar calendar = Calendar.getInstance();

        // 设置为本周一 (这样 Mon 对应周一，Tue 对应周二...)
        // 注意：如果你希望显示的是“未来5天”而不是“本周一到周五”，可以把下面这一行删掉
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // 日期格式化工具
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("d", Locale.getDefault()); // 只获取数字 "12"
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, d MMM", Locale.getDefault()); // 获取完整 "Mon, 12 Oct"

        // 循环填充5个格子
        for (TextView dateView : dateViews) {
            // 1. 设置显示的数字 (例如 "12")
            dateView.setText(dayNumberFormat.format(calendar.getTime()));

            // 2. 把完整日期 (例如 "Mon, 12 Oct") 藏在 tag 里，方便点击时获取
            dateView.setTag(fullDateFormat.format(calendar.getTime()));

            // 3. 如果这一天是“今天”，可以给个默认高亮 (可选)
            // 这里我们暂时不默认高亮，保持清爽，让用户自己点

            // 天数加 1，准备处理下一个格子
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    // --- 日期选择逻辑 ---
    private void setupDateClickListeners() {
        View.OnClickListener dateListener = v -> {
            // 1. 重置所有日期样式 (变灰)
            for (TextView view : dateViews) {
                view.setBackgroundResource(R.drawable.date_circle);
                view.setTextColor(Color.parseColor("#444444"));
            }

            // 2. 设置当前点击的样式 (变绿)
            TextView clickedView = (TextView) v;
            clickedView.setBackgroundResource(R.drawable.date_selected_circle);
            clickedView.setTextColor(Color.WHITE);

            // 3. 记录选择 (从 Tag 里取出我们刚才存的真实日期)
            if (v.getTag() != null) {
                selectedDate = v.getTag().toString();
            }
        };

        for (TextView view : dateViews) {
            view.setOnClickListener(dateListener);
        }
    }

    // --- 时间选择逻辑 ---
    private void setupTimeClickListeners() {
        View.OnClickListener timeListener = v -> {
            // 1. 重置所有时间样式
            for (TextView view : slotViews) {
                view.setBackgroundResource(R.drawable.time_unselected);
                view.setTextColor(Color.BLACK);
            }

            // 2. 设置当前点击的样式
            TextView clickedView = (TextView) v;
            clickedView.setBackgroundResource(R.drawable.time_selected);
            clickedView.setTextColor(Color.WHITE);

            // 3. 记录选择
            if (v == slotMorning) {
                selectedTime = "9 - 12 AM";
                selectedLocation = "BrightPath Mental Care Centre";
            } else if (v == slotAfternoon) {
                selectedTime = "3 - 5 PM";
                selectedLocation = "BrightPath Mental Care Centre";
            } else if (v == slotEvening) {
                selectedTime = "6 - 8 PM";
                selectedLocation = "Online";
            }
        };

        for (TextView view : slotViews) {
            view.setOnClickListener(timeListener);
        }
    }

    // --- 提交预约到 Firebase ---
    private void handleBooking() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        bookbtn.setEnabled(false);
        bookbtn.setText("Booking...");

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("userId", userId);
        appointment.put("counsellorId", currentCounsellor.getId());
        appointment.put("counsellorName", currentCounsellor.getName());
        appointment.put("counsellorImage", currentCounsellor.getImageName());
        appointment.put("date", selectedDate); // 这里现在存的是真实日期
        appointment.put("time", selectedTime);
        appointment.put("location", selectedLocation);
        appointment.put("status", "upcoming");
        appointment.put("timestamp", FieldValue.serverTimestamp());

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AppointmentBooking.this, "Booking Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AppointmentBooking.this, HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    bookbtn.setEnabled(true);
                    bookbtn.setText("Book Now");
                    Toast.makeText(AppointmentBooking.this, "Booking Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}