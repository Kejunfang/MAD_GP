package com.example.mad_gp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WorkshopRegistration extends AppCompatActivity {

    EditText inputName, inputEmail, inputPhone, inputMotivation;
    Spinner spinnerWorkshopTitle;
    Button submitButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_registration);

        // INITIALIZE VIEWS
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputMotivation = findViewById(R.id.inputMotivation);

        spinnerWorkshopTitle = findViewById(R.id.spinnerWorkshopTitle);

        submitButton = findViewById(R.id.submitButton);
        cancelButton = findViewById(R.id.cancelButton);

        // POPULATE DROPDOWNS

        // Workshop Title List
        String[] workshopTitles = {
                "Stress & Anxiety Management",
                "Emotional Regulation",
                "Overthinking Control",
                "Mindfulness &amp; Meditation"
        };

        ArrayAdapter<String> titleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                workshopTitles
        );
        spinnerWorkshopTitle.setAdapter(titleAdapter);

        // SUBMIT BUTTON CLICK
        submitButton.setOnClickListener(v -> {

            // Get field values
            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            String motivation = inputMotivation.getText().toString().trim();

            String selectedWorkshop = spinnerWorkshopTitle.getSelectedItem().toString();

            // Validation
            if (name.isEmpty()) {
                inputName.setError("Name is required");
                return;
            }
            if (email.isEmpty()) {
                inputEmail.setError("Email is required");
                return;
            }
            if (phone.isEmpty()) {
                inputPhone.setError("Phone number is required");
                return;
            }
            if (motivation.isEmpty()) {
                inputMotivation.setError("Please enter your motivation");
                return;
            }

            // SUCCESS — SHOW TOAST
            Toast.makeText(
                    this,
                    "Registration Submitted:\n" +
                            "Name: " + name + "\n" +
                            "Email: " + email + "\n" +
                            "Phone: " + phone + "\n" +
                            "Workshop: " + selectedWorkshop + "\n",
                    Toast.LENGTH_LONG
            ).show();
        });


        // CANCEL BUTTON CLICK
        cancelButton.setOnClickListener(v -> {
            finish();  // Close this page and go back
        });
    }
}