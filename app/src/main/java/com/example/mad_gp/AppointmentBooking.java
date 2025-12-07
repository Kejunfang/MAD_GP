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

import java.util.HashMap;
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
        // 请确保这里的布局文件名和你 XML 的名字一致 (例如 activity_appointment_booking1)
        setContentView(R.layout.activity_appointment_booking);

        // 初始化 Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // --- 1. 初始化控件 ---
        initViews();

        // --- 2. 接收上一页传来的咨询师数据 ---
        // 这一步非常重要，不然页面不知道你是要预约谁
        currentCounsellor = (Counsellor) getIntent().getSerializableExtra("COUNSELLOR_DATA");
        if (currentCounsellor != null) {
            setupCounsellorInfo();
        }

        // --- 3. 设置日期点击事件 ---
        setupDateClickListeners();

        // --- 4. 设置时间点击事件 ---
        setupTimeClickListeners();

        // --- 5. 按钮逻辑 ---
        backbtn.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish()); // 顶部的返回箭头

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
        // 把传过来的数据显示在界面上
        counsellorName.setText(currentCounsellor.getName());
        counsellorTitle.setText(currentCounsellor.getTitle());
        counsellorLocation.setText(currentCounsellor.getLocation());

        String imgName = currentCounsellor.getImageName();
        if (imgName != null && !imgName.isEmpty()) {
            int resId = getResources().getIdentifier(imgName, "drawable", getPackageName());
            if (resId != 0) counsellorImage.setImageResource(resId);
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

            // 3. 记录选择
            if (v == dateMon) selectedDate = "Mon, 11th";
            else if (v == dateTue) selectedDate = "Tue, 12th";
            else if (v == dateWed) selectedDate = "Wed, 13th";
            else if (v == dateThu) selectedDate = "Thu, 14th";
            else if (v == dateFri) selectedDate = "Fri, 15th";
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
        // 1. 验证是否都选了
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

        // 2. 准备数据
        bookbtn.setEnabled(false);
        bookbtn.setText("Booking...");

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("userId", userId);
        appointment.put("counsellorId", currentCounsellor.getId());
        appointment.put("counsellorName", currentCounsellor.getName());
        appointment.put("counsellorImage", currentCounsellor.getImageName());
        appointment.put("date", selectedDate);
        appointment.put("time", selectedTime);
        appointment.put("location", selectedLocation);
        appointment.put("status", "upcoming");
        appointment.put("timestamp", FieldValue.serverTimestamp());

        // 3. 写入 Firestore
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