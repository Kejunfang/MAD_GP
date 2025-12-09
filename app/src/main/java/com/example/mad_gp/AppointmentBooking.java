package com.example.mad_gp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppointmentBooking extends AppCompatActivity {

    // btn variable
    private ImageView counsellorImage, btnBack;
    private TextView counsellorName, counsellorTitle, counsellorLocation;
    private Button bookbtn, backbtn;

    // date variable
    private TextView dateMon, dateTue, dateWed, dateThu, dateFri;
    private TextView[] dateViews;

    // time varibale
    private TextView slotMorning, slotAfternoon, slotEvening;
    private TextView[] slotViews;

    // data variable
    private Counsellor currentCounsellor;
    private String selectedDate = "";
    private String selectedTime = "";
    private String selectedLocation = "";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_appointment_booking);

        // initial firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // initial view
        initViews();

        //Catch the data of the counsellor from last page
        currentCounsellor = (Counsellor) getIntent().getSerializableExtra("COUNSELLOR_DATA");
        if (currentCounsellor != null) {
            setupCounsellorInfo();
        }

        //  Set up the real time data
        setupRealDates();

        //  Set up the click event
        setupDateClickListeners();
        setupTimeClickListeners();

        //  Set up the logic of the Btn
        backbtn.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        bookbtn.setOnClickListener(v -> handleBooking());
    }

    private void initViews() {
        counsellorImage = findViewById(R.id.counsellorImage);
        counsellorName = findViewById(R.id.counsellorName);
        counsellorTitle = findViewById(R.id.counsellorTitle);
        counsellorLocation = findViewById(R.id.counsellorLocation);
        bookbtn = findViewById(R.id.bookbtn);
        backbtn = findViewById(R.id.backbtn);
        btnBack = findViewById(R.id.btnBack);

        // Date Views
        dateMon = findViewById(R.id.dateMon);
        dateTue = findViewById(R.id.dateTue);
        dateWed = findViewById(R.id.dateWed);
        dateThu = findViewById(R.id.dateThu);
        dateFri = findViewById(R.id.dateFri);
        dateViews = new TextView[]{dateMon, dateTue, dateWed, dateThu, dateFri};

        // Time Views
        slotMorning = findViewById(R.id.slotMorning);
        slotAfternoon = findViewById(R.id.slotAfternoon);
        slotEvening = findViewById(R.id.slotEvening);
        slotViews = new TextView[]{slotMorning, slotAfternoon, slotEvening};
    }

    private void setupCounsellorInfo() {
        counsellorName.setText(currentCounsellor.getName());
        counsellorTitle.setText(currentCounsellor.getTitle());
        counsellorLocation.setText(currentCounsellor.getLocation());

        String imgName = currentCounsellor.getImageName();
        if (imgName != null && !imgName.isEmpty()) {
            int resId = getResources().getIdentifier(imgName, "drawable", getPackageName());
            if (resId != 0) counsellorImage.setImageResource(resId);
        }
    }

    // Calculate and show the date of this week
    private void setupRealDates() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        //  Date format tools
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());

        for (TextView dateView : dateViews) {

            dateView.setText(dayNumberFormat.format(calendar.getTime()));

            dateView.setTag(fullDateFormat.format(calendar.getTime()));


            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    //   Choose Date Logic
    private void setupDateClickListeners() {
        View.OnClickListener dateListener = v -> {
            //  Set up the layout of all the date
            for (TextView view : dateViews) {
                view.setBackgroundResource(R.drawable.date_circle);
                view.setTextColor(Color.parseColor("#444444"));
            }

            //  Change the btn color to green
            TextView clickedView = (TextView) v;
            clickedView.setBackgroundResource(R.drawable.date_selected_circle);
            clickedView.setTextColor(Color.WHITE);

            //  record the choice
            if (v.getTag() != null) {
                selectedDate = v.getTag().toString();
            }
        };

        for (TextView view : dateViews) {
            view.setOnClickListener(dateListener);
        }
    }

    //  Time choice logic
    private void setupTimeClickListeners() {
        View.OnClickListener timeListener = v -> {
            //  Reset all the time
            for (TextView view : slotViews) {
                view.setBackgroundResource(R.drawable.time_unselected);
                view.setTextColor(Color.BLACK);
            }

            //   Set up the click view
            TextView clickedView = (TextView) v;
            clickedView.setBackgroundResource(R.drawable.time_selected);
            clickedView.setTextColor(Color.WHITE);

            //  record the choice
            if (v == slotMorning) {
                selectedTime = "9 - 12 AM";
                selectedLocation = "BrightPath Mental Care Centre";
            } else if (v == slotAfternoon) {
                selectedTime = "3 - 5 PM";
                selectedLocation = "BrightPath Mental Care Centre";
            } else if (v == slotEvening) {
                selectedTime = "6 - 8 PM";
                selectedLocation = "Online";
            }
        };

        for (TextView view : slotViews) {
            view.setOnClickListener(timeListener);
        }
    }

    //  Submit to the firebase
    private void handleBooking() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        bookbtn.setEnabled(false);
        bookbtn.setText("Booking...");

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("userId", userId);
        appointment.put("counsellorId", currentCounsellor.getId());
        appointment.put("counsellorName", currentCounsellor.getName());
        appointment.put("counsellorImage", currentCounsellor.getImageName());
        appointment.put("date", selectedDate);
        appointment.put("time", selectedTime);
        appointment.put("location", selectedLocation);
        appointment.put("status", "upcoming");
        appointment.put("timestamp", FieldValue.serverTimestamp());

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AppointmentBooking.this, "Booking Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AppointmentBooking.this, HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    bookbtn.setEnabled(true);
                    bookbtn.setText("Book Now");
                    Toast.makeText(AppointmentBooking.this, "Booking Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}