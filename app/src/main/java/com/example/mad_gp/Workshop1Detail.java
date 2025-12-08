package com.example.mad_gp;

import android.content.Intent;
import android.graphics.Color; // ★ 1. 新增导入
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Workshop1Detail extends AppCompatActivity {

    private Workshop currentWorkshop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_detail);

        // 绑定控件
        ImageView ivImage = findViewById(R.id.ivWorkshopImage);
        TextView tvTitle = findViewById(R.id.workshopTitle);
        TextView tvMode = findViewById(R.id.workshopMode);
        TextView tvPrice = findViewById(R.id.workshopPrice);
        TextView tvDesc = findViewById(R.id.workshopDescription);
        TextView tvAgenda = findViewById(R.id.tvWorkshopAgenda);
        Button btnRegister = findViewById(R.id.registerButton);
        ImageView btnBack = findViewById(R.id.backBtn);

        // 接收数据
        currentWorkshop = (Workshop) getIntent().getSerializableExtra("WORKSHOP_DATA");

        // ★ 2. 接收“是否已报名”的信号 (默认为 false)
        boolean isRegistered = getIntent().getBooleanExtra("IS_REGISTERED", false);

        if (currentWorkshop != null) {
            tvTitle.setText(currentWorkshop.getTitle());
            tvMode.setText(currentWorkshop.getLocation());
            tvPrice.setText(currentWorkshop.getPrice());
            tvDesc.setText(currentWorkshop.getFullDescription());

            // 处理 Agenda
            if (currentWorkshop.getAgenda() != null) {
                tvAgenda.setText(currentWorkshop.getAgenda().replace("\\n", "\n"));
            }

            // 图片
            String imgName = currentWorkshop.getImageName();
            if (imgName != null) {
                int resId = getResources().getIdentifier(imgName, "drawable", getPackageName());
                if (resId != 0) ivImage.setImageResource(resId);
            }
        }

        btnBack.setOnClickListener(v -> finish());

        // ★ 3. 根据信号处理按钮逻辑
        if (isRegistered) {
            // --- 情况 A: 已经报名 (从 Profile Page 点进来的) ---
            btnRegister.setText("Registered");        // 改文字
            btnRegister.setBackgroundColor(Color.GRAY);  // 改颜色 (变灰)
            btnRegister.setEnabled(false);            // 禁止点击
        } else {
            // --- 情况 B: 还没报名 (从 Event Page 点进来的) ---
            // 保持原本的逻辑
            btnRegister.setOnClickListener(v -> {
                if (currentWorkshop != null) {
                    Intent intent = new Intent(Workshop1Detail.this, WorkshopRegistration.class);
                    intent.putExtra("WORKSHOP_TITLE", currentWorkshop.getTitle());
                    intent.putExtra("WORKSHOP_ID", currentWorkshop.getId());
                    startActivity(intent);
                }
            });
        }
    }
}