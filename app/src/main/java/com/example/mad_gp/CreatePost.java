package com.example.mad_gp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private FirebaseStorage storage;

    private String currentUserId;
    private String currentUserName = "Anonymous";
    private String currentUserAvatar = "counsellor1";

    private String selectedPredefinedImage = "";
    private Uri selectedImageUri = null;
    private Bitmap capturedImageBitmap = null;
    private boolean isCustomImage = false;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    capturedImageBitmap = null;
                    selectedPredefinedImage = "";
                    isCustomImage = true;

                    imagePreviewContainer.setVisibility(View.VISIBLE);
                    imageSelectionContainer.setVisibility(View.GONE);
                    Glide.with(this).load(uri).into(ivPreview);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            capturedImageBitmap = imageBitmap;
                            selectedImageUri = null;
                            selectedPredefinedImage = "";
                            isCustomImage = true;

                            imagePreviewContainer.setVisibility(View.VISIBLE);
                            imageSelectionContainer.setVisibility(View.GONE);
                            ivPreview.setImageBitmap(imageBitmap);
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
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
                            if (avatar.startsWith("http")) {
                                Glide.with(this).load(avatar).circleCrop().into(ivCurrentUserAvatar);
                            } else {
                                int resId = getResources().getIdentifier(avatar, "drawable", getPackageName());
                                if (resId != 0) ivCurrentUserAvatar.setImageResource(resId);
                            }
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

        btnPost.setOnClickListener(v -> {
            String content = etPostContent.getText().toString().trim();
            if (content.isEmpty() && !isCustomImage && selectedPredefinedImage.isEmpty()) {
                Toast.makeText(CreatePost.this, "Please write something...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isCustomImage) {
                uploadImageAndPost(content);
            } else {
                savePostToFirestore(content, selectedPredefinedImage);
            }
        });

        btnAddPhoto.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        btnAddCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        View.OnClickListener selectPredefinedListener = v -> {
            ImageView clickedImg = (ImageView) v;

            if (v.getId() == R.id.imgOption1) selectedPredefinedImage = "relaximage1";
            else if (v.getId() == R.id.imgOption2) selectedPredefinedImage = "relaximage2";
            else if (v.getId() == R.id.imgOption3) selectedPredefinedImage = "relaximage3";
            else if (v.getId() == R.id.imgOption5) selectedPredefinedImage = "relaximage5";

            selectedImageUri = null;
            capturedImageBitmap = null;
            isCustomImage = false;

            int resId = getResources().getIdentifier(selectedPredefinedImage, "drawable", getPackageName());
            ivPreview.setImageResource(resId);
            imagePreviewContainer.setVisibility(View.VISIBLE);
            imageSelectionContainer.setVisibility(View.GONE);
        };

        imgOption1.setOnClickListener(selectPredefinedListener);
        imgOption2.setOnClickListener(selectPredefinedListener);
        imgOption3.setOnClickListener(selectPredefinedListener);
        imgOption5.setOnClickListener(selectPredefinedListener);

        btnRemoveImage.setOnClickListener(v -> {
            selectedPredefinedImage = "";
            selectedImageUri = null;
            capturedImageBitmap = null;
            isCustomImage = false;
            imagePreviewContainer.setVisibility(View.GONE);
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndPost(String content) {
        btnPost.setEnabled(false);
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        String filename = "posts/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(filename);

        if (selectedImageUri != null) {
            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        savePostToFirestore(content, uri.toString());
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreatePost.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnPost.setEnabled(true);
                    });
        } else if (capturedImageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            ref.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        savePostToFirestore(content, uri.toString());
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreatePost.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnPost.setEnabled(true);
                    });
        }
    }

    private void savePostToFirestore(String content, String imageUrl) {
        if (btnPost.isEnabled()) btnPost.setEnabled(false);

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
        postMap.put("postImage", imageUrl);

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