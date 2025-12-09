package com.example.mad_gp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.FirebaseFirestore;

public class DailyTipsDetails extends AppCompatActivity {

    private TextView tvArticleTitle, tvArticleContent, tvArticleAuthor, tvArticleDate;
    private ImageView ivArticleImage;
    private CollapsingToolbarLayout collapsingToolbar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_tips_details);

        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

        db = FirebaseFirestore.getInstance();

        AppBarLayout appBarLayout = findViewById(R.id.appBar);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageButton btnBack = findViewById(R.id.btnBack);

        ivArticleImage = findViewById(R.id.ivArticleImage);
        tvArticleTitle = findViewById(R.id.tvArticleTitle);
        tvArticleContent = findViewById(R.id.tvArticleContent);
        tvArticleAuthor = findViewById(R.id.tvArticleAuthor);
        tvArticleDate = findViewById(R.id.tvArticleDate);

        collapsingToolbar.setTitle("");
        toolbar.setBackgroundColor(Color.TRANSPARENT);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(tvArticleTitle.getText());
                    collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.text_main));
                    toolbar.setBackgroundColor(getResources().getColor(R.color.white));
                    btnBack.setColorFilter(getResources().getColor(R.color.text_main));
                    isShow = true;
                } else if (verticalOffset == 0 || isShow) {
                    collapsingToolbar.setTitle("");
                    toolbar.setBackgroundColor(Color.TRANSPARENT);
                    btnBack.setColorFilter(getResources().getColor(R.color.text_main));
                    isShow = false;
                }
            }
        });

        btnBack.setOnClickListener(v -> finish());

        String tipId = getIntent().getStringExtra("TIP_ID");

        if (tipId != null) {
            loadTipDetails(tipId);
        } else {
            Toast.makeText(this, "Error: No tip ID found.", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadTipDetails(String tipId) {
        db.collection("daily_tips").document(tipId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String content = documentSnapshot.getString("content");
                        String author = documentSnapshot.getString("author");
                        String date = documentSnapshot.getString("date");

                        String imageUrl = documentSnapshot.getString("imageUrl");

                        if (content != null) {
                            content = content.replace("\\n", "\n");
                        }

                        tvArticleTitle.setText(title);
                        tvArticleContent.setText(content);
                        tvArticleAuthor.setText(author != null ? author : "MentaLeaf Team");
                        tvArticleDate.setText("•  " + (date != null ? date : ""));

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.studydesk)
                                    .error(R.drawable.studydesk)
                                    .centerCrop()
                                    .into(ivArticleImage);
                        } else {
                            ivArticleImage.setImageResource(R.drawable.studydesk);
                        }
                    } else {
                        Toast.makeText(this, "Article not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load article: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}