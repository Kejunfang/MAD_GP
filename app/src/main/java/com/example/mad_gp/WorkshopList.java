package com.example.mad_gp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class WorkshopList extends AppCompatActivity {

    private RecyclerView rvWorkshops;
    private WorkshopAdapter adapter;
    private List<Workshop> workshopList;
    private List<Workshop> fullList;
    private FirebaseFirestore db;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_list);

        db = FirebaseFirestore.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        rvWorkshops = findViewById(R.id.rvWorkshops);

        rvWorkshops.setLayoutManager(new LinearLayoutManager(this));
        workshopList = new ArrayList<>();
        fullList = new ArrayList<>();
        adapter = new WorkshopAdapter(this, workshopList);
        rvWorkshops.setAdapter(adapter);

        loadWorkshops();

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
        workshopList.clear();
        if (text.isEmpty()) {
            workshopList.addAll(fullList);
        } else {
            text = text.toLowerCase();
            for (Workshop item : fullList) {
                if (item.getTitle().toLowerCase().contains(text) || item.getDescription().toLowerCase().contains(text)) {
                    workshopList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadWorkshops() {
        db.collection("workshops").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                workshopList.clear();
                fullList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    String id = doc.getId();
                    String title = doc.getString("title");
                    String desc = doc.getString("description");
                    String fullDesc = doc.getString("fullDescription");
                    String loc = doc.getString("location");
                    String price = doc.getString("price");
                    String img = doc.getString("imageName");
                    String agenda = doc.getString("agenda");

                    Workshop workshop = new Workshop(id, title, desc, fullDesc, loc, price, img, agenda);
                    workshopList.add(workshop);
                    fullList.add(workshop);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "No workshops found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}