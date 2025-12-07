package com.example.mad_gp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditProfile extends AppCompatActivity {

    private ImageView ivEditAvatar, btnBack;
    private EditText etBio;
    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri imageUri; // 用于存储用户选中的图片 URI

    // 图片选择器
    private final ActivityResultLauncher<String> selectImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivEditAvatar.setImageURI(uri); // 预览选中的图片
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 初始化 Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // 如果这里报错 Cannot resolve symbol 'FirebaseStorage'，说明你必须去 build.gradle 加依赖
        // implementation("com.google.firebase:firebase-storage")
        storageRef = FirebaseStorage.getInstance().getReference();

        // 初始化控件
        ivEditAvatar = findViewById(R.id.ivEditAvatar);
        etBio = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // 1. 加载当前用户信息
        loadCurrentData();

        // 2. 点击头像 -> 选择图片
        ivEditAvatar.setOnClickListener(v -> selectImage.launch("image/*"));

        // 3. 点击返回
        btnBack.setOnClickListener(v -> finish());

        // 4. 点击保存
        btnSave.setOnClickListener(v -> {
            String bio = etBio.getText().toString().trim();
            uploadData(bio);
        });
    }

    private void loadCurrentData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 加载 Bio
                String currentBio = documentSnapshot.getString("bio");
                if (currentBio != null) etBio.setText(currentBio);

                // 加载头像
                String currentImg = documentSnapshot.getString("profileImageUrl");
                if (currentImg != null && !currentImg.isEmpty()) {
                    Glide.with(this).load(currentImg).into(ivEditAvatar);
                }
            }
        });
    }

    private void uploadData(String bio) {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String uid = mAuth.getCurrentUser().getUid();

        // 情况 A: 用户选了新图片 -> 先上传图片，拿到 URL，再更新 Firestore
        if (imageUri != null) {
            // 定义图片在云端的路径: profile_images/用户ID.jpg
            StorageReference fileRef = storageRef.child("profile_images/" + uid + ".jpg");

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // 上传成功，获取下载链接
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    updateFirestore(uid, bio, downloadUrl);
                });
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(EditProfile.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } else {
            // 情况 B: 用户没换图片 -> 只更新 Bio
            updateFirestore(uid, bio, null);
        }
    }

    private void updateFirestore(String uid, String bio, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("bio", bio);

        // 如果有新头像 URL，也更新进去
        if (imageUrl != null) {
            updates.put("profileImageUrl", imageUrl);
        }

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditProfile.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish(); // 关闭页面，回到 ProfilePage
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(EditProfile.this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}