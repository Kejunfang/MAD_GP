package com.example.mad_gp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class DailyTipsDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_tips_details);

        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

        AppBarLayout appBarLayout = findViewById(R.id.appBar);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvArticleTitle = findViewById(R.id.tvArticleTitle);

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
                }
                else if (verticalOffset == 0 || isShow) {
                    collapsingToolbar.setTitle("");
                    toolbar.setBackgroundColor(Color.TRANSPARENT);
                    btnBack.setColorFilter(getResources().getColor(R.color.text_main));
                    isShow = false;
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}