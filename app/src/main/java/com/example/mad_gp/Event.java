package com.example.mad_gp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Event extends AppCompatActivity {

    ImageView btnBack;
    LinearLayout cardAppointment, cardWorkshop;
    LinearLayout navHome, navEvent, navSocial, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        cardAppointment = findViewById(R.id.cardAppointment);
        cardWorkshop = findViewById(R.id.cardWorkshop);

        navHome = findViewById(R.id.navHome);
        navEvent = findViewById(R.id.navEvent);
        navSocial = findViewById(R.id.navSocial);
        navProfile = findViewById(R.id.navProfile);



        // ---------------- CARD NAVIGATION ----------------

        // Book Appointment Card
        cardAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(Event.this, CounsellorList.class);
            startActivity(intent);
        });

        // Workshop Card
        cardWorkshop.setOnClickListener(v -> {
            Intent intent = new Intent(Event.this, WorkshopList.class);
            startActivity(intent);
        });


        // ---------------- BOTTOM NAVIGATION ----------------

        // Home
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(Event.this, HomePage.class);
            startActivity(intent);
        });

        // Event
        navEvent.setOnClickListener(v -> {
            // Do nothing or reload
        });

        // Social
        navSocial.setOnClickListener(v -> {
            Intent intent = new Intent(Event.this, Social.class);
            startActivity(intent);
        });

        // Profile
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Event.this, ProfilePage.class);
            startActivity(intent);
        });
    }
}
