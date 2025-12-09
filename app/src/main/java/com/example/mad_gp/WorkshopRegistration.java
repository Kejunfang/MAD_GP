package com.example.mad_gp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WorkshopRegistration extends AppCompatActivity {

    private EditText inputName, inputEmail, inputPhone, inputMotivation, etWorkshopTitle;
    private Button submitButton, cancelButton;
    private String workshopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_registration);

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputMotivation = findViewById(R.id.inputMotivation);
        etWorkshopTitle = findViewById(R.id.etWorkshopTitle);
        submitButton = findViewById(R.id.submitButton);
        cancelButton = findViewById(R.id.cancelButton);

        String title = getIntent().getStringExtra("WORKSHOP_TITLE");
        workshopId = getIntent().getStringExtra("WORKSHOP_ID");

        etWorkshopTitle.setText(title); // 自动填入标题

        cancelButton.setOnClickListener(v -> finish());

        submitButton.setOnClickListener(v -> submitRegistration());
    }

    private void submitRegistration() {
        String name = inputName.getText().toString();
        String email = inputEmail.getText().toString();
        String phone = inputPhone.getText().toString();
        String motivation = inputMotivation.getText().toString();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> reg = new HashMap<>();
        reg.put("userId", mAuth.getCurrentUser().getUid());
        reg.put("workshopId", workshopId);
        reg.put("workshopTitle", etWorkshopTitle.getText().toString());
        reg.put("fullName", name);
        reg.put("email", email);
        reg.put("phone", phone);
        reg.put("motivation", motivation);
        reg.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("workshop_registrations")
                .add(reg)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}