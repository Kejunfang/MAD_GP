package com.example.mad_gp;

import android.content.Intent; // 记得导入这个
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class WorkshopList extends AppCompatActivity {

    ImageView btnBack;
    EditText etSearch;

    LinearLayout workshop1, workshop2, workshop3, workshop4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_list);

        // ----------- FIND VIEWS -----------
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);

        // 你的 ID 是 workshop1, workshop2... 我这里保持不变
        workshop1 = findViewById(R.id.workshop1);
        workshop2 = findViewById(R.id.workshop2);
        workshop3 = findViewById(R.id.workshop3);
        workshop4 = findViewById(R.id.workshop4);


        // ----------- BACK BUTTON -----------
        btnBack.setOnClickListener(v -> finish());


        // ----------- WORKSHOP CLICK EVENTS -----------

        // 【修改点】Workshop 1: 点击跳转到 Workshop1Detail
        workshop1.setOnClickListener(v -> {
            // 你原来的 Toast 可以留着测试，也可以注释掉
            // Toast.makeText(this, "Opening Stress & Anxiety Workshop", Toast.LENGTH_SHORT).show();

            // 添加跳转代码
            Intent intent = new Intent(WorkshopList.this, Workshop1Detail.class);
            startActivity(intent);
        });

        // 其他 Workshop 保持原来的 Toast，或者你以后也可以照样改成跳转
        workshop2.setOnClickListener(v ->
                Toast.makeText(this, "Opening Emotional Regulation Workshop", Toast.LENGTH_SHORT).show()
        );

        workshop3.setOnClickListener(v ->
                Toast.makeText(this, "Opening Overthinking Control Workshop", Toast.LENGTH_SHORT).show()
        );

        workshop4.setOnClickListener(v ->
                Toast.makeText(this, "Opening Mindfulness & Meditation Workshop", Toast.LENGTH_SHORT).show()
        );


        // ----------- SEARCH FILTER FUNCTION (完全保留你的逻辑) -----------
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWorkshops(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }


    // ----------- FILTERING LOGIC (完全保留) -----------
    private void filterWorkshops(String text) {
        text = text.toLowerCase();

        // Workshop 1
        if ("stress & anxiety management".toLowerCase().contains(text) ||
                "stress".contains(text) || "anxiety".contains(text)) {
            workshop1.setVisibility(LinearLayout.VISIBLE);
        } else {
            workshop1.setVisibility(LinearLayout.GONE);
        }

        // Workshop 2
        if ("emotional regulation workshop".toLowerCase().contains(text) ||
                "emotional".contains(text)) {
            workshop2.setVisibility(LinearLayout.VISIBLE);
        } else {
            workshop2.setVisibility(LinearLayout.GONE);
        }

        // Workshop 3
        if ("overthinking control".toLowerCase().contains(text) ||
                "overthinking".contains(text)) {
            workshop3.setVisibility(LinearLayout.VISIBLE);
        } else {
            workshop3.setVisibility(LinearLayout.GONE);
        }

        // Workshop 4
        if ("mindfulness & meditation".toLowerCase().contains(text) ||
                "meditation".contains(text) || "mindfulness".contains(text)) {
            workshop4.setVisibility(LinearLayout.VISIBLE);
        } else {
            workshop4.setVisibility(LinearLayout.GONE);
        }
    }
}