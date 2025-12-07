package com.example.mad_gp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPassword, signUpText;

    // 1. 声明 Firebase Auth (解决 cannot resolve symbol 'mAuth')
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 2. 初始化 Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 初始化控件
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpText = findViewById(R.id.signUpText);

        // --- 登录按钮点击事件 ---
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Password is required");
                    return;
                }

                // 3. 使用 Firebase 进行真正的登录
                // (替换掉了之前的假登录逻辑)
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                // 登录成功，跳转主页
                                Intent intent = new Intent(Login.this, HomePage.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 登录失败 (密码错误或账号不存在)
                                Toast.makeText(Login.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // --- 忘记密码点击事件 (修复了 mAuth 报错) ---
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. 创建一个容器，为了给输入框加边距
                android.widget.FrameLayout container = new android.widget.FrameLayout(Login.this);
                android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                );
                // 设置左右边距，防止输入框贴边
                params.leftMargin = 50;
                params.rightMargin = 50;

                // 2. 创建输入框
                final EditText resetMail = new EditText(Login.this);
                resetMail.setLayoutParams(params);
                resetMail.setHint("Type your email here..."); // 加上提示文字
                resetMail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); // 设置为邮箱输入模式

                // 把输入框放进容器
                container.addView(resetMail);

                // 3. 创建对话框
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(Login.this);
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter your email to receive reset link:");
                passwordResetDialog.setView(container); // 这里放入容器，而不是直接放输入框

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString().trim();

                        // 调试提示：看看程序到底读到了什么
                        if (TextUtils.isEmpty(mail)) {
                            Toast.makeText(Login.this, "Error: You didn't enter an email!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mAuth.sendPasswordResetEmail(mail)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Login.this, "Success! Reset Link Sent to Your Email.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Login.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                passwordResetDialog.create().show();
            }
        });

        // --- 注册文字点击事件 ---
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
}