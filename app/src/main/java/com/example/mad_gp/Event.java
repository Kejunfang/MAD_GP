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
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Event
        navEvent.setOnClickListener(v -> {

        });

        // Social
        navSocial.setOnClickListener(v -> {
            Intent intent = new Intent(Event.this, CommunityFeed.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Profile
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Event.this, ProfilePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }
}
