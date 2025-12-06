package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Workshop1Detail extends AppCompatActivity {

    ImageView backBtn, favoriteBtn;
    TextView workshopTitle, workshopMode, workshopDescription;
    Button registerButton;

    boolean isFavorite = false; // toggle state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop1_detail);

        // --- Initialize Views ---
        backBtn = findViewById(R.id.backBtn);
        favoriteBtn = findViewById(R.id.favoriteBtn);
        workshopTitle = findViewById(R.id.workshopTitle);
        workshopMode = findViewById(R.id.workshopMode);
        workshopDescription = findViewById(R.id.workshopDescription);
        registerButton = findViewById(R.id.registerButton);

        // --- Set Static Texts (optional if not sent by Intent) ---
        workshopTitle.setText("Stress & Anxiety Management");
        workshopMode.setText("Online");
        workshopDescription.setText(
                "Learn practical tools to reduce anxiety and calm the mind through mindful habits, " +
                        "breathing exercises, and simple mental resets you can use anytime."
        );

        // --- Back Button Function ---
        backBtn.setOnClickListener(v -> {
            finish(); // go back to previous activity
        });

        // --- Favorite Button Toggle ---
        favoriteBtn.setOnClickListener(v -> toggleFavorite());

        // --- Register Button ---
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(Workshop1Detail.this, WorkshopRegistration.class);
            startActivity(intent);
        });

    }

    private void toggleFavorite() {
        if (!isFavorite) {
            favoriteBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.favourite_filled));
            isFavorite = true;
        } else {
            favoriteBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.favourite));
            isFavorite = false;
        }
    }
}
