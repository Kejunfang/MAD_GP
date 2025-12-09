package com.example.mad_gp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreatePost extends AppCompatActivity {

    private ImageButton btnClose, btnRemoveImage, btnAddPhoto, btnAddCamera;
    private MaterialButton btnPost;
    private EditText etPostContent;
    private MaterialCardView imagePreviewContainer;
    private ImageView ivPreview, ivCurrentUserAvatar;
    private TextView tvCurrentUserName;

    private HorizontalScrollView imageSelectionContainer;
    private ImageView imgOption1, imgOption2, imgOption3, imgOption5;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String currentUserName = "Anonymous";
    private String currentUserAvatar = "counsellor1";

    private String selectedImageName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            currentUserId = user.getUid();
            fetchCurrentUserInfo();
        } else {
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void fetchCurrentUserInfo() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String avatar = documentSnapshot.getString("profileImageUrl");
                        if (name != null) {
                            currentUserName = name;
                            tvCurrentUserName.setText(name);
                        }
                        if (avatar != null) {
                            currentUserAvatar = avatar;
                            int resId = getResources().getIdentifier(avatar, "drawable", getPackageName());
                            if (resId != 0) ivCurrentUserAvatar.setImageResource(resId);
                        }
                    }
                });
    }

    private void initViews() {
        btnClose = findViewById(R.id.btnClose);
        btnPost = findViewById(R.id.btnPost);
        etPostContent = findViewById(R.id.etPostContent);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        ivPreview = findViewById(R.id.ivPreview);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnAddCamera = findViewById(R.id.btnAddCamera);
        ivCurrentUserAvatar = findViewById(R.id.ivHeaderAvatar);
        tvCurrentUserName = findViewById(R.id.tvHeaderName);

        imageSelectionContainer = findViewById(R.id.imageSelectionContainer);
        imgOption1 = findViewById(R.id.imgOption1);
        imgOption2 = findViewById(R.id.imgOption2);
        imgOption3 = findViewById(R.id.imgOption3);
        imgOption5 = findViewById(R.id.imgOption5);

        imagePreviewContainer.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());
        btnPost.setOnClickListener(v -> postToFirebase());

        btnAddPhoto.setOnClickListener(v -> {
            imageSelectionContainer.setVisibility(View.VISIBLE);
            imagePreviewContainer.setVisibility(View.GONE);
            Toast.makeText(this, "Select an image", Toast.LENGTH_SHORT).show();
        });

        btnAddCamera.setOnClickListener(v -> {
            imageSelectionContainer.setVisibility(View.VISIBLE);
        });

        View.OnClickListener selectImageListener = v -> {
            ImageView clickedImg = (ImageView) v;

            if (v.getId() == R.id.imgOption1) selectedImageName = "relaximage1";
            else if (v.getId() == R.id.imgOption2) selectedImageName = "relaximage2";
            else if (v.getId() == R.id.imgOption3) selectedImageName = "relaximage3";
            else if (v.getId() == R.id.imgOption5) selectedImageName = "relaximage5";

            int resId = getResources().getIdentifier(selectedImageName, "drawable", getPackageName());
            ivPreview.setImageResource(resId);
            imagePreviewContainer.setVisibility(View.VISIBLE);

            imageSelectionContainer.setVisibility(View.GONE);
        };

        imgOption1.setOnClickListener(selectImageListener);
        imgOption2.setOnClickListener(selectImageListener);
        imgOption3.setOnClickListener(selectImageListener);
        imgOption5.setOnClickListener(selectImageListener);

        btnRemoveImage.setOnClickListener(v -> {
            selectedImageName = "";
            imagePreviewContainer.setVisibility(View.GONE);
        });
    }

    private void postToFirebase() {
        String content = etPostContent.getText().toString().trim();

        if (content.isEmpty() && selectedImageName.isEmpty()) {
            Toast.makeText(CreatePost.this, "Please write something...", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setEnabled(false);

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("content", content);
        postMap.put("userId", currentUserId);
        postMap.put("userName", currentUserName);
        postMap.put("userAvatar", currentUserAvatar);
        postMap.put("timestamp", FieldValue.serverTimestamp());
        postMap.put("timeAgo", "Just now");
        postMap.put("likesCount", 0);
        postMap.put("commentCount", 0);
        postMap.put("likedBy", new ArrayList<String>());
        postMap.put("postImage", selectedImageName);

        db.collection("community_posts").add(postMap)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(CreatePost.this, "Posted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreatePost.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPost.setEnabled(true);
                });
    }
}