package com.example.mad_gp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CounsellorList extends AppCompatActivity {

    private RecyclerView rvCounsellors;
    private CounsellorAdapter adapter;
    private List<Counsellor> counsellorList;
    private FirebaseFirestore db;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counsellor_list);

        db = FirebaseFirestore.getInstance();

        // 初始化控件
        ImageView btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        rvCounsellors = findViewById(R.id.rvCounsellors);

        // 设置 RecyclerView
        rvCounsellors.setLayoutManager(new LinearLayoutManager(this));
        counsellorList = new ArrayList<>();
        adapter = new CounsellorAdapter(this, counsellorList);
        rvCounsellors.setAdapter(adapter);

        // 加载数据
        loadCounsellors();

        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 简单的搜索功能 (可选)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 如果需要搜索，你需要写一个过滤逻辑
                // 这里暂时不写，先保证能加载列表
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadCounsellors() {
        db.collection("counsellors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        counsellorList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String title = doc.getString("title");
                            String location = doc.getString("location");
                            String imageName = doc.getString("imageName");

                            counsellorList.add(new Counsellor(id, name, title, location, imageName));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No counsellors found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}