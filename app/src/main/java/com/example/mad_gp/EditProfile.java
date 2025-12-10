package com.example.mad_gp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private FirebaseStorage storage;

    private String selectedAvatarTag = "default";
    private Uri imageUri;
    private boolean isCustomImageSelected = false;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        isCustomImageSelected = true;
                        Glide.with(this).load(imageUri).into(ivEditAvatar);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        ivEditAvatar = findViewById(R.id.ivEditAvatar);
        etBio = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        loadCurrentData();

        ivEditAvatar.setOnClickListener(v -> showAvatarSelectionDialog());

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String bio = etBio.getText().toString().trim();
            if (isCustomImageSelected && imageUri != null) {
                uploadImageAndSave(bio);
            } else {
                saveToFirestore(bio, selectedAvatarTag);
            }
        });
    }

    private void loadCurrentData() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentBio = documentSnapshot.getString("bio");
                if (currentBio != null) etBio.setText(currentBio);

                String currentAvatar = documentSnapshot.getString("profileImageUrl");
                if (currentAvatar != null && !currentAvatar.isEmpty()) {
                    selectedAvatarTag = currentAvatar;
                }
                updateAvatarView(selectedAvatarTag);
            }
        });
    }

    private void showAvatarSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_dialog_select_avatar);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setupDialogAvatar(dialog, R.id.imgAv1, "smile");
        setupDialogAvatar(dialog, R.id.imgAv2, "counsellor1");
        setupDialogAvatar(dialog, R.id.imgAv3, "counsellor2");
        setupDialogAvatar(dialog, R.id.imgAv4, "counsellor3");
        setupDialogAvatar(dialog, R.id.imgAv5, "counsellor4");

        ImageView imgUpload = dialog.findViewById(R.id.imgAv6);
        if (imgUpload != null) {
            imgUpload.setImageResource(R.drawable.ic_image);
            imgUpload.setOnClickListener(v -> {
                dialog.dismiss();
                openGallery();
            });
        }

        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void setupDialogAvatar(Dialog dialog, int viewId, String tag) {
        ImageView img = dialog.findViewById(viewId);
        if (img != null) {
            img.setOnClickListener(v -> {
                selectedAvatarTag = tag;
                isCustomImageSelected = false;
                imageUri = null;
                updateAvatarView(tag);
                dialog.dismiss();
            });
        }
    }

    private void updateAvatarView(String tag) {
        if (tag != null && tag.startsWith("http")) {
            Glide.with(this).load(tag).placeholder(R.drawable.ic_default_avatar).into(ivEditAvatar);
        } else {
            int resId = getAvatarResourceId(tag);
            ivEditAvatar.setImageResource(resId);
        }
    }

    private void uploadImageAndSave(String bio) {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            String uid = mAuth.getCurrentUser().getUid();
            String filename = "avatars/" + uid + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference ref = storage.getReference().child(filename);

            ref.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        saveToFirestore(bio, downloadUrl);
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(EditProfile.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            Toast.makeText(EditProfile.this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToFirestore(String bio, String avatarUrlOrTag) {
        if (progressBar.getVisibility() != View.VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        }

        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("bio", bio);
        updates.put("profileImageUrl", avatarUrlOrTag);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditProfile.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(EditProfile.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static int getAvatarResourceId(String tag) {
        if (tag == null) tag = "default";
        switch (tag) {
            case "smile": return R.drawable.smile;
            case "counsellor1": return R.drawable.counsellor1;
            case "counsellor2": return R.drawable.counsellor2;
            case "counsellor3": return R.drawable.counsellor3;
            case "counsellor4": return R.drawable.counsellor4;
            case "counsellor5": return R.drawable.counsellor5;
            default: return R.drawable.ic_default_avatar;
        }
    }
}