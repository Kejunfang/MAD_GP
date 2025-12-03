package com.example.mad_gp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CounsellorList extends AppCompatActivity {

    ImageView btnBack;
    EditText etSearch;

    LinearLayout card1, card2, card3, card4, card5;
    TextView name1, name2, name3, name4, name5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counsellor_list);

        // Back Button
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Search Box
        etSearch = findViewById(R.id.etSearch);

        // Cards
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);
        card5 = findViewById(R.id.card5);

        // Names for filtering
        name1 = findViewById(R.id.name1);
        name2 = findViewById(R.id.name2);
        name3 = findViewById(R.id.name3);
        name4 = findViewById(R.id.name4);
        name5 = findViewById(R.id.name5);

        // Card Click Events → Go to appointment page
        card1.setOnClickListener(v -> openDetails("Mr. John Lee"));
        card2.setOnClickListener(v -> openDetails("Dr. Sarah Tan"));
        card3.setOnClickListener(v -> openDetails("Dr. Amir Rahman"));
        card4.setOnClickListener(v -> openDetails("Ms. Nicole Tan"));
        card5.setOnClickListener(v -> openDetails("Mr. Daniel Lim"));

        // Search Filter (Optional)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Open next activity when card clicked
    private void openDetails(String counsellorName) {
        Intent intent = new Intent(CounsellorList.this, AppointmentBooking.class);
        intent.putExtra("counsellorName", counsellorName);
        startActivity(intent);
    }

    // Real-time search filtering
    private void filterList(String text) {
        text = text.toLowerCase();

        card1.setVisibility(name1.getText().toString().toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
        card2.setVisibility(name2.getText().toString().toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
        card3.setVisibility(name3.getText().toString().toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
        card4.setVisibility(name4.getText().toString().toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
        card5.setVisibility(name5.getText().toString().toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
    }
}