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
    private List<Counsellor> fullCounsellorList;
    private FirebaseFirestore db;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counsellor_list);

        db = FirebaseFirestore.getInstance();

        counsellorList = new ArrayList<>();
        fullCounsellorList = new ArrayList<>();

        ImageView btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        rvCounsellors = findViewById(R.id.rvCounsellors);

        rvCounsellors.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CounsellorAdapter(this, counsellorList);
        rvCounsellors.setAdapter(adapter);

        loadCounsellors();

        btnBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        counsellorList.clear();

        if (text.isEmpty()) {
            counsellorList.addAll(fullCounsellorList);
        } else {
            text = text.toLowerCase();

            for (Counsellor item : fullCounsellorList) {
                if (item.getName().toLowerCase().contains(text) ||
                        item.getTitle().toLowerCase().contains(text) ||
                        item.getLocation().toLowerCase().contains(text)) {
                    counsellorList.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadCounsellors() {
        db.collection("counsellors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        counsellorList.clear();
                        fullCounsellorList.clear();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            String title = doc.getString("title");
                            String location = doc.getString("location");
                            String imageName = doc.getString("imageName");

                            Counsellor counsellor = new Counsellor(id, name, title, location, imageName);

                            counsellorList.add(counsellor);
                            fullCounsellorList.add(counsellor);
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