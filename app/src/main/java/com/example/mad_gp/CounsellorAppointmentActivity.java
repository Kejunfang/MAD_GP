package com.example.mad_gp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CounsellorAppointmentActivity extends AppCompatActivity {

    TextView dateMon, dateTue, dateWed, dateThu, dateFri;
    TextView slotMorning, slotAfternoon, slotEvening;
    Button btnBook;

    String selectedDate = "13";
    String selectedSlot = "9 - 12 AM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counsellor_appointment);

        dateMon = findViewById(R.id.dateMon);
        dateTue = findViewById(R.id.dateTue);
        dateWed = findViewById(R.id.dateWed);
        dateThu = findViewById(R.id.dateThu);
        dateFri = findViewById(R.id.dateFri);

        slotMorning = findViewById(R.id.slotMorning);
        slotAfternoon = findViewById(R.id.slotAfternoon);
        slotEvening = findViewById(R.id.slotEvening);

        btnBook = findViewById(R.id.btnBook);

        setupDateClick(dateMon, "11");
        setupDateClick(dateTue, "12");
        setupDateClick(dateWed, "13");
        setupDateClick(dateThu, "14");
        setupDateClick(dateFri, "15");

        setupSlotClick(slotMorning, "9 - 12 AM");
        setupSlotClick(slotAfternoon, "3 - 5 PM");
        setupSlotClick(slotEvening, "6 - 8 PM");

        btnBook.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Booked on " + selectedDate + " (" + selectedSlot + ")",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDateClick(TextView dateView, String date) {
        dateView.setOnClickListener(v -> {
            resetAllDates();
            dateView.setBackgroundResource(R.drawable.date_selected_circle);
            dateView.setTextColor(Color.WHITE);
            selectedDate = date;
        });
    }

    private void resetAllDates() {
        dateMon.setBackgroundResource(R.drawable.date_circle);
        dateTue.setBackgroundResource(R.drawable.date_circle);
        dateWed.setBackgroundResource(R.drawable.date_circle);
        dateThu.setBackgroundResource(R.drawable.date_circle);
        dateFri.setBackgroundResource(R.drawable.date_circle);

        dateMon.setTextColor(Color.BLACK);
        dateTue.setTextColor(Color.BLACK);
        dateWed.setTextColor(Color.BLACK);
        dateThu.setTextColor(Color.BLACK);
        dateFri.setTextColor(Color.BLACK);
    }

    private void setupSlotClick(TextView slotView, String slot) {
        slotView.setOnClickListener(v -> {
            resetSlots();
            slotView.setBackgroundResource(R.drawable.time_selected);
            slotView.setTextColor(Color.WHITE);
            selectedSlot = slot;
        });
    }

    private void resetSlots() {
        slotMorning.setBackgroundResource(R.drawable.time_unselected);
        slotAfternoon.setBackgroundResource(R.drawable.time_unselected);
        slotEvening.setBackgroundResource(R.drawable.time_unselected);

        slotMorning.setTextColor(Color.parseColor("#2E6CF6"));
        slotAfternoon.setTextColor(Color.parseColor("#2E6CF6"));
        slotEvening.setTextColor(Color.parseColor("#2E6CF6"));
    }
}
