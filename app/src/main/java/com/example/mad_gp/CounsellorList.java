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
    private List<Counsellor> counsellorList; // 这个是专门用来显示在界面上的列表 (会变)
    private List<Counsellor> fullCounsellorList; // 新增：这个是完整的备份列表 (不会变)
    private FirebaseFirestore db;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counsellor_list);

        db = FirebaseFirestore.getInstance();

        // 初始化列表
        counsellorList = new ArrayList<>();
        fullCounsellorList = new ArrayList<>(); // 初始化备份列表

        // 初始化控件
        ImageView btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        rvCounsellors = findViewById(R.id.rvCounsellors);

        // 设置 RecyclerView
        rvCounsellors.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CounsellorAdapter(this, counsellorList);
        rvCounsellors.setAdapter(adapter);

        // 加载数据
        loadCounsellors();

        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // --- 搜索功能 ---
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 只要文字一变，就调用筛选方法
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // --- 筛选逻辑 ---
    private void filter(String text) {
        // 1. 先清空显示列表
        counsellorList.clear();

        // 2. 如果搜索框是空的，就显示所有人
        if (text.isEmpty()) {
            counsellorList.addAll(fullCounsellorList);
        } else {
            // 3. 否则，把输入变小写（为了忽略大小写）
            text = text.toLowerCase();

            // 4. 遍历备份列表，找匹配的人
            for (Counsellor item : fullCounsellorList) {
                // 如果名字包含搜索词，或者职位、地点包含搜索词
                if (item.getName().toLowerCase().contains(text) ||
                        item.getTitle().toLowerCase().contains(text) ||
                        item.getLocation().toLowerCase().contains(text)) {
                    counsellorList.add(item);
                }
            }
        }

        // 5. 通知适配器刷新
        adapter.notifyDataSetChanged();
    }

    private void loadCounsellors() {
        db.collection("counsellors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        counsellorList.clear();
                        fullCounsellorList.clear(); // 记得也清空备份

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String title = doc.getString("title");
                            String location = doc.getString("location");
                            String imageName = doc.getString("imageName");

                            Counsellor counsellor = new Counsellor(id, name, title, location, imageName);

                            counsellorList.add(counsellor);
                            fullCounsellorList.add(counsellor); // 同时加到备份列表里
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