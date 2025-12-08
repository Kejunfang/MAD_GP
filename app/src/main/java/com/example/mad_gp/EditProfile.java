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

                updateAvatarView(selectedAvatarTag);
            }
        });
    }

    private void showAvatarSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_dialog_select_avatar);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // ★★★ 修复点 1：必须用真实的文件名 "smile"，不能用 "avatar_original" ★★★
        // 因为 CommunityFeed 是通过文件名来找图片的
        setupDialogAvatar(dialog, R.id.imgAv1, "smile");

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

    public static int getAvatarResourceId(String tag) {
        if (tag == null) tag = "default";

        switch (tag) {
            // ★★★ 修复点 2：这里也要改成 "smile" ★★★
            // 这样在 EditProfile 页面里预览时也能正常显示
            case "smile": return R.drawable.smile;

            case "counsellor1": return R.drawable.counsellor1;
            case "counsellor2": return R.drawable.counsellor2;
            case "counsellor3": return R.drawable.counsellor3;
            case "counsellor4": return R.drawable.counsellor4;
            case "counsellor5": return R.drawable.counsellor5;

            // 处理旧数据：如果数据库里已经存了 "avatar_original"，为了兼容防止崩溃，也可以让它返回 smile
            case "avatar_original": return R.drawable.smile;

            default: return R.drawable.ic_default_avatar;
        }
    }
}