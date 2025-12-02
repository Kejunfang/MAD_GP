package com.example.mad_gp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class AppointmentBooking extends AppCompatActivity {
    EditText etSearch;
    LinearLayout card1, card2, card3, card4, card5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_booking);

        etSearch = findViewById(R.id.etSearch);
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);
        card5 = findViewById(R.id.card5);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCards(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCards(String text) {
        text = text.toLowerCase();

        card1.setVisibility(text.isEmpty() || "mr. john lee mental health counsellor".toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
        card2.setVisibility(text.isEmpty() || "dr. sarah tan psychologist".toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
        card3.setVisibility(text.isEmpty() || "dr. amir rahman psychologist".toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
        card4.setVisibility(text.isEmpty() || "ms. nicole tan mental health counsellor".toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
        card5.setVisibility(text.isEmpty() || "mr. daniel lim mental health counsellor".toLowerCase().contains(text) ? View.VISIBLE : View.GONE);
    }
}