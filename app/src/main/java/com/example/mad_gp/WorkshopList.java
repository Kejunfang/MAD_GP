package com.example.mad_gp;

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
        setContentView(R.layout.activity_workshop);

        // ----------- FIND VIEWS -----------
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);

        workshop1 = findViewById(R.id.workshop1);
        workshop2 = findViewById(R.id.workshop2);
        workshop3 = findViewById(R.id.workshop3);
        workshop4 = findViewById(R.id.workshop4);


        // ----------- BACK BUTTON -----------
        btnBack.setOnClickListener(v -> finish());


        // ----------- WORKSHOP CLICK EVENTS -----------

        workshop1.setOnClickListener(v ->
                Toast.makeText(this, "Opening Stress & Anxiety Workshop", Toast.LENGTH_SHORT).show()
        );

        workshop2.setOnClickListener(v ->
                Toast.makeText(this, "Opening Emotional Regulation Workshop", Toast.LENGTH_SHORT).show()
        );

        workshop3.setOnClickListener(v ->
                Toast.makeText(this, "Opening Overthinking Control Workshop", Toast.LENGTH_SHORT).show()
        );

        workshop4.setOnClickListener(v ->
                Toast.makeText(this, "Opening Mindfulness & Meditation Workshop", Toast.LENGTH_SHORT).show()
        );


        // ----------- SEARCH FILTER FUNCTION -----------
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


    // ----------- FILTERING LOGIC -----------
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
