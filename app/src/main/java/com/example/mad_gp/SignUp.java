package com.example.mad_gp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class SignUp extends AppCompatActivity {

    private EditText nameEditText, emailEditText, birthDateEditText, phoneEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton, googleSignUpButton;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        birthDateEditText = findViewById(R.id.birthDateEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        googleSignUpButton = findViewById(R.id.googleSignUpButton);
        loginText = findViewById(R.id.loginText);

        // Click listener for Sign Up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSignUp();
            }
        });

        // Click listener for Google Sign Up button
        googleSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SignUp.this, "Google Sign Up clicked", Toast.LENGTH_SHORT).show();
                // Implement Google Sign-In here if needed
            }
        });

        // Click listener for Log In text
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to Login activity
                Intent intent = new Intent(SignUp.this, HomePage.class);
                startActivity(intent);
                finish();
            }
        });

        // Birth date picker
        birthDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
    }

    private void handleSignUp() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String birthDate = birthDateEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Basic validation
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name required");
            nameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Valid email required");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(birthDate)) {
            birthDateEditText.setError("Birth date required");
            birthDateEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone number required");
            phoneEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password required");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Success (e.g., save to database or Firebase)
        Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(SignUp.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                        String selectedDate = String.format("%02d/%02d/%04d", d, m + 1, y);
                        birthDateEditText.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}