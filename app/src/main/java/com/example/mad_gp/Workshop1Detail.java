package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Workshop1Detail extends AppCompatActivity {

    private Workshop currentWorkshop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop1_detail);

        // 绑定控件
        ImageView ivImage = findViewById(R.id.ivWorkshopImage);
        TextView tvTitle = findViewById(R.id.workshopTitle);
        TextView tvMode = findViewById(R.id.workshopMode);
        TextView tvPrice = findViewById(R.id.workshopPrice); // 新增价格显示
        TextView tvDesc = findViewById(R.id.workshopDescription);
        TextView tvAgenda = findViewById(R.id.tvWorkshopAgenda);
        Button btnRegister = findViewById(R.id.registerButton);
        ImageView btnBack = findViewById(R.id.backBtn);

        // 接收数据
        currentWorkshop = (Workshop) getIntent().getSerializableExtra("WORKSHOP_DATA");

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

        // 注册按钮：传数据给注册页
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Workshop1Detail.this, WorkshopRegistration.class);
            intent.putExtra("WORKSHOP_TITLE", currentWorkshop.getTitle());
            intent.putExtra("WORKSHOP_ID", currentWorkshop.getId());
            startActivity(intent);
        });
    }
}