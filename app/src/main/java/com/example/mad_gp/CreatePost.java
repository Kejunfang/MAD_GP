package com.example.mad_gp; // 记得检查包名

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class CreatePost extends AppCompatActivity {

    // 声明控件
    private ImageButton btnClose;
    private MaterialButton btnPost;
    private EditText etPostContent;
    private MaterialCardView imagePreviewContainer;
    private ImageButton btnRemoveImage;
    private ImageButton btnAddPhoto;
    private ImageButton btnAddCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // 1. 初始化控件
        initViews();

        // 2. 设置点击事件
        setupListeners();
    }

    private void initViews() {
        btnClose = findViewById(R.id.btnClose);
        btnPost = findViewById(R.id.btnPost);
        etPostContent = findViewById(R.id.etPostContent);

        // 图片预览区域
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        btnRemoveImage = findViewById(R.id.btnRemoveImage); // 刚才加的 ID

        // 底部工具栏按钮
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnAddCamera = findViewById(R.id.btnAddCamera);

        // 默认状态：隐藏图片预览 (模拟刚进来没有图片)
        imagePreviewContainer.setVisibility(View.GONE);
    }

    private void setupListeners() {
        // --- 关闭页面 ---
        btnClose.setOnClickListener(v -> finish());

        // --- 发布按钮 ---
        btnPost.setOnClickListener(v -> {
            String content = etPostContent.getText().toString().trim();
            boolean hasImage = imagePreviewContainer.getVisibility() == View.VISIBLE;

            // 校验：既没字也没图，不让发
            if (content.isEmpty() && !hasImage) {
                Toast.makeText(CreatePost.this, "Please write something...", Toast.LENGTH_SHORT).show();
            } else {
                // 模拟发布成功
                Toast.makeText(CreatePost.this, "Post Published Successfully!", Toast.LENGTH_SHORT).show();
                finish(); // 关闭页面，返回上一页
            }
        });

        // --- 模拟添加图片 (相册) ---
        btnAddPhoto.setOnClickListener(v -> {
            imagePreviewContainer.setVisibility(View.VISIBLE); // 显示预览图
            Toast.makeText(CreatePost.this, "Image Selected", Toast.LENGTH_SHORT).show();
        });

        // --- 模拟拍照 ---
        btnAddCamera.setOnClickListener(v -> {
            imagePreviewContainer.setVisibility(View.VISIBLE); // 显示预览图
            Toast.makeText(CreatePost.this, "Photo Taken", Toast.LENGTH_SHORT).show();
        });

        // --- 删除图片 ---
        if (btnRemoveImage != null) {
            btnRemoveImage.setOnClickListener(v -> {
                imagePreviewContainer.setVisibility(View.GONE); // 隐藏预览图
                Toast.makeText(CreatePost.this, "Image Removed", Toast.LENGTH_SHORT).show();
            });
        }
    }
}