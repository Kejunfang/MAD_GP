package com.example.mad_gp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class BrathingExercise extends AppCompatActivity {

    private ImageButton btnClose;
    private MaterialButton btnFinishExercise;
    private View breathingCircleView;
    private TextView tvBreathingStatus;
    private TextView tvBreathingSubStatus;

    private ObjectAnimator breathingAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brathing_exercise);

        btnClose = findViewById(R.id.btnClose);
        btnFinishExercise = findViewById(R.id.btnFinishExercise);
        breathingCircleView = findViewById(R.id.breathingCircleView);
        tvBreathingStatus = findViewById(R.id.tvBreathingStatus);
        tvBreathingSubStatus = findViewById(R.id.tvBreathingSubStatus);


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnFinishExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BrathingExercise.this, "Great job! Feeling relaxed?", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        startBreathingAnimation();
    }

    private void startBreathingAnimation() {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.5f);

        breathingAnimator = ObjectAnimator.ofPropertyValuesHolder(breathingCircleView, scaleX, scaleY);

        breathingAnimator.setDuration(4000);

        breathingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathingAnimator.setRepeatMode(ValueAnimator.REVERSE);

        breathingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        breathingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);

                if (tvBreathingStatus.getText().toString().equals("Breathe In")) {
                    tvBreathingStatus.setText("Breathe Out");
                    tvBreathingSubStatus.setText("Release your stress");
                } else {
                    tvBreathingStatus.setText("Breathe In");
                    tvBreathingSubStatus.setText("Focus on the circle");
                }
            }
        });

        tvBreathingStatus.setText("Breathe In");
        tvBreathingSubStatus.setText("Focus on the circle");

        breathingAnimator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (breathingAnimator != null) {
            breathingAnimator.cancel();
        }
    }
}