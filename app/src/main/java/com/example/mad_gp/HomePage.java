package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);


        FrameLayout cardBreathe = findViewById(R.id.cardBreathe);
        FrameLayout cardMusic = findViewById(R.id.cardMusic);
        MaterialCardView cardFeaturedArticle = findViewById(R.id.cardFeaturedArticle);
        ImageView chartPlaceholder = findViewById(R.id.chartPlaceholder);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);



        cardBreathe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, BrathingExercise.class);
                startActivity(intent);
            }
        });

        cardMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MusicPage.class);
                startActivity(intent);
            }
        });

        cardFeaturedArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, DailyTipsDetails.class);
                startActivity(intent);
            }
        });

        chartPlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Opening Mood Trend Details...", Toast.LENGTH_SHORT).show();
                // 如果你有 MoodTrendActivity，取消下面两行的注释：
                // Intent intent = new Intent(HomepageActivity.this, MoodTrendActivity.class);
                // startActivity(intent);
            }
        });



        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Home clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        navEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Event clicked! (Feature coming soon)", Toast.LENGTH_SHORT).show();
                // 未来在这里添加跳转代码：
                // Intent intent = new Intent(HomepageActivity.this, EventActivity.class);
                // startActivity(intent);
            }
        });

        navSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到社区页面 (Community Feed)
                Toast.makeText(HomePage.this, "Social clicked!", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(HomePage.this, CommunityFeedActivity.class);
                //startActivity(intent);
            }
        });

        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到个人资料页面 (Profile)
                // 假设你的 profile 页面目前可能叫 AppointmentBooking? 或者你可以新建一个 ProfileActivity
                // 这里暂时用 Toast
                Toast.makeText(HomePage.this, "Profile clicked!", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(HomepageActivity.this, ProfileActivity.class);
                // startActivity(intent);
            }
        });
    }
}