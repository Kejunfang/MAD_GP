package com.example.mad_gp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private ImageView ivEditAvatar, btnBack;
    private EditText etBio;
    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // 默认依然是 "default" (对应灰色占位图)
    private String selectedAvatarTag = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
            saveToFirestore(bio);
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

                // 更新视图
                updateAvatarView(selectedAvatarTag);
            }
        });
    }

    private void showAvatarSelectionDialog() {
        Dialog dialog = new Dialog(this);
        // 确保这里的 layout 文件名正确
        dialog.setContentView(R.layout.activity_dialog_select_avatar);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // --- 修改点 1: 给第一个头像换个名字，叫 "avatar_original"，不要叫 "default" ---
        setupDialogAvatar(dialog, R.id.imgAv1, "avatar_original");

        setupDialogAvatar(dialog, R.id.imgAv2, "counsellor1");
        setupDialogAvatar(dialog, R.id.imgAv3, "counsellor2");
        setupDialogAvatar(dialog, R.id.imgAv4, "counsellor3");
        setupDialogAvatar(dialog, R.id.imgAv5, "counsellor4");
        setupDialogAvatar(dialog, R.id.imgAv6, "counsellor5");

        dialog.show();
    }

    private void setupDialogAvatar(Dialog dialog, int viewId, String tag) {
        ImageView img = dialog.findViewById(viewId);
        if (img != null) {
            img.setOnClickListener(v -> {
                selectedAvatarTag = tag;
                updateAvatarView(tag);
                dialog.dismiss();
            });
        }
    }

    private void updateAvatarView(String tag) {
        int resId = getAvatarResourceId(tag);
        ivEditAvatar.setImageResource(resId);
    }

    private void saveToFirestore(String bio) {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("bio", bio);
        updates.put("profileImageUrl", selectedAvatarTag);

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

    // --- 修改点 2: 在这里处理 "avatar_original" ---
    public static int getAvatarResourceId(String tag) {
        if (tag == null) tag = "default";

        switch (tag) {
            // 如果用户选了第一个头像，返回那个彩色的 R.drawable.avatar
            case "avatar_original": return R.drawable.smile;

            case "counsellor1": return R.drawable.counsellor1;
            case "counsellor2": return R.drawable.counsellor2;
            case "counsellor3": return R.drawable.counsellor3;
            case "counsellor4": return R.drawable.counsellor4;
            case "counsellor5": return R.drawable.counsellor5;

            // 只有 tag 是 "default" (或者其他未知的) 时，才返回灰色的 ic_default_avatar
            default: return R.drawable.ic_default_avatar;
        }
    }
}