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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class SignUp extends AppCompatActivity {

    private EditText nameEditText, emailEditText, birthDateEditText, phoneEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton, googleSignUpButton;
    private TextView loginText;

    // Firebase 声明
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 初始化 Firebase Auth 和 Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
                // Google Sign-In 逻辑通常比较复杂，需要单独配置 GoogleSignInClient
            }
        });

        // Click listener for Log In text
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, Login.class); // 修正：跳转到 Login 而不是 HomePage
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

        // --- 验证逻辑 ---
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

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // --- 1. 创建 Firebase Authentication 用户 ---
        signUpButton.setEnabled(false); // 防止重复点击
        signUpButton.setText("Signing Up...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 注册成功，获取当前用户 ID
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                saveUserToFirestore(userId, name, email, phone, birthDate);
                            }
                        } else {
                            // 注册失败
                            signUpButton.setEnabled(true);
                            signUpButton.setText("Sign Up");
                            Toast.makeText(SignUp.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // --- 2. 将用户信息保存到 Firestore ---
    private void saveUserToFirestore(String userId, String name, String email, String phone, String birthDate) {
        // 创建 UserModel 对象
        UserModel user = new UserModel(userId, name, email, phone, birthDate);

        // 保存到 "users" 集合，文档 ID 为 userId
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SignUp.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();

                        // 跳转到主页
                        Intent intent = new Intent(SignUp.this, HomePage.class);
                        // 清除之前的 Activity 栈，防止用户按返回键回到注册页
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signUpButton.setEnabled(true);
                        signUpButton.setText("Sign Up");
                        Toast.makeText(SignUp.this, "Failed to save user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                        // 格式化日期
                        String selectedDate = String.format("%02d/%02d/%04d", d, m + 1, y);
                        birthDateEditText.setText(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}