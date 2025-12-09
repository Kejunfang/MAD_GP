package com.example.mad_gp;

import android.content.Intent;
import android.graphics.Color;
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

        ImageView ivImage = findViewById(R.id.ivWorkshopImage);
        TextView tvTitle = findViewById(R.id.workshopTitle);
        TextView tvMode = findViewById(R.id.workshopMode);
        TextView tvPrice = findViewById(R.id.workshopPrice);
        TextView tvDesc = findViewById(R.id.workshopDescription);
        TextView tvAgenda = findViewById(R.id.tvWorkshopAgenda);
        Button btnRegister = findViewById(R.id.registerButton);
        ImageView btnBack = findViewById(R.id.backBtn);

        currentWorkshop = (Workshop) getIntent().getSerializableExtra("WORKSHOP_DATA");

        boolean isRegistered = getIntent().getBooleanExtra("IS_REGISTERED", false);

        if (currentWorkshop != null) {
            tvTitle.setText(currentWorkshop.getTitle());
            tvMode.setText(currentWorkshop.getLocation());
            tvPrice.setText(currentWorkshop.getPrice());
            tvDesc.setText(currentWorkshop.getFullDescription());

            if (currentWorkshop.getAgenda() != null) {
                tvAgenda.setText(currentWorkshop.getAgenda().replace("\\n", "\n"));
            }

            String imgName = currentWorkshop.getImageName();
            if (imgName != null) {
                int resId = getResources().getIdentifier(imgName, "drawable", getPackageName());
                if (resId != 0) ivImage.setImageResource(resId);
            }
        }

        btnBack.setOnClickListener(v -> finish());

        if (isRegistered) {
            btnRegister.setText("Registered");
            btnRegister.setBackgroundColor(Color.GRAY);
            btnRegister.setEnabled(false);
        } else {
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