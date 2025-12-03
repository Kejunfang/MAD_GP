package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AppointmentBooking extends AppCompatActivity {

    TextView dateMon, dateTue, dateWed, dateThu, dateFri;
    TextView slotMorning, slotAfternoon, slotEvening;
    Button bookBtn, backBtn;

    String selectedDate = "13"; // default selected (Wed)
    String selectedTime = "9 - 12 AM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_booking);

        // Initialize Date TextViews
        dateMon = findViewById(R.id.dateMon);
        dateTue = findViewById(R.id.dateTue);
        dateWed = findViewById(R.id.dateWed);
        dateThu = findViewById(R.id.dateThu);
        dateFri = findViewById(R.id.dateFri);

        // Time slots
        slotMorning = findViewById(R.id.slotMorning);
        slotAfternoon = findViewById(R.id.slotAfternoon);
        slotEvening = findViewById(R.id.slotEvening);

        // Buttons
        bookBtn = findViewById(R.id.bookbtn);
        backBtn = findViewById(R.id.backbtn);

        // --- DATE CLICK LISTENERS ---
        setDateClick(dateMon, "11");
        setDateClick(dateTue, "12");
        setDateClick(dateWed, "13");
        setDateClick(dateThu, "14");
        setDateClick(dateFri, "15");

        // --- TIME SLOT CLICK LISTENERS ---
        setTimeClick(slotMorning, "9 - 12 AM");
        setTimeClick(slotAfternoon, "3 - 5 PM");
        setTimeClick(slotEvening, "6 - 8 PM");

        // --- BOOK NOW BUTTON ---
        bookBtn.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Booked on " + selectedDate + " at " + selectedTime,
                    Toast.LENGTH_LONG).show();

            // If you want to open another page:
            // Intent i = new Intent(Event.this, Confirmation.class);
            // startActivity(i);
        });

        // --- BACK BUTTON ---
        backBtn.setOnClickListener(v -> finish());
    }

    // ----------- HELPER FUNCTIONS -----------

    private void setDateClick(TextView dateView, String dateValue) {
        dateView.setOnClickListener(v -> {
            selectedDate = dateValue;

            // Reset all dates to unselected
            resetAllDates();

            // Set selected style
            dateView.setBackgroundResource(R.drawable.date_selected_circle);
            dateView.setTextColor(getResources().getColor(android.R.color.white));
        });
    }

    private void resetAllDates() {
        resetDateStyle(dateMon);
        resetDateStyle(dateTue);
        resetDateStyle(dateWed);
        resetDateStyle(dateThu);
        resetDateStyle(dateFri);
    }

    private void resetDateStyle(TextView v) {
        v.setBackgroundResource(R.drawable.date_circle);
        v.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void setTimeClick(TextView slotView, String timeValue) {
        slotView.setOnClickListener(v -> {
            selectedTime = timeValue;

            // Reset all slots
            resetAllSlots();

            // Set selected slot design
            slotView.setBackgroundResource(R.drawable.time_selected);
            slotView.setTextColor(getResources().getColor(android.R.color.white));
        });
    }

    private void resetAllSlots() {
        resetSlotStyle(slotMorning);
        resetSlotStyle(slotAfternoon);
        resetSlotStyle(slotEvening);
    }

    private void resetSlotStyle(TextView slot) {
        slot.setBackgroundResource(R.drawable.time_unselected);
        slot.setTextColor(getResources().getColor(android.R.color.black));
    }
}