package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.firestore.Query; // 暂时不需要这个引用了

import java.util.ArrayList;
import java.util.List;

public class ProfilePage extends AppCompatActivity {

    // 1. 声明原有 UI 变量
    private TextView tvUserName, tvUserBio, tvEventCount, tvPostCount;
    private ImageView ivProfileImage, btnEditProfile;

    // 2. 声明预约列表相关变量
    private TextView tvLabelAppointments;
    private RecyclerView rvAppointments;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;

    // 3. 声明 Firebase 变量
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 绑定原有控件
        tvUserName = findViewById(R.id.tvUserName);
        tvUserBio = findViewById(R.id.tvUserBio);
        tvEventCount = findViewById(R.id.tvEventCount);
        tvPostCount = findViewById(R.id.tvPostCount);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // --- 绑定预约列表控件 ---
        tvLabelAppointments = findViewById(R.id.tvLabelAppointments);
        rvAppointments = findViewById(R.id.rvAppointments);

        // 初始化 RecyclerView
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointmentList);
        rvAppointments.setAdapter(appointmentAdapter);

        // 编辑按钮事件
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, EditProfile.class);
            startActivity(intent);
        });

        // 底部导航栏逻辑
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        navEvent.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, Event.class)));
        navSocial.setOnClickListener(v -> startActivity(new Intent(ProfilePage.this, CommunityFeed.class)));
        navProfile.setOnClickListener(v -> Toast.makeText(ProfilePage.this, "You are already here!", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        loadMyAppointments(); // 每次回来都刷新
    }

    // --- 加载所有预约 (不排序版，防止报错) ---
    private void loadMyAppointments() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // 查询: userId 是自己
        // 注意：我把 orderBy 注释掉了，这样就不会报错 "Requires an index"
        db.collection("appointments")
                .whereEqualTo("userId", currentUser.getUid())
                // .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // 有预约：显示标题和列表
                        tvLabelAppointments.setVisibility(View.VISIBLE);
                        rvAppointments.setVisibility(View.VISIBLE);

                        appointmentList.clear(); // 清空旧数据
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String name = doc.getString("counsellorName");
                            String date = doc.getString("date");
                            String time = doc.getString("time");
                            String location = doc.getString("location");
                            String imgName = doc.getString("counsellorImage");

                            appointmentList.add(new Appointment(name, imgName, date, time, location));
                        }
                        appointmentAdapter.notifyDataSetChanged();
                    } else {
                        // 没预约：隐藏标题和列表
                        tvLabelAppointments.setVisibility(View.GONE);
                        rvAppointments.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    // 如果出错，打印错误信息并隐藏列表
                    Toast.makeText(ProfilePage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvLabelAppointments.setVisibility(View.GONE);
                    rvAppointments.setVisibility(View.GONE);
                });
    }

    // --- 加载用户信息 ---
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            tvUserName.setText(name != null ? name : "No Name");

                            String bio = documentSnapshot.getString("bio");
                            if (bio != null && !bio.isEmpty()) {
                                tvUserBio.setText(bio);
                            } else {
                                tvUserBio.setText("This user hasn't written a bio yet.");
                            }

                            String avatarTag = documentSnapshot.getString("profileImageUrl");
                            if (avatarTag != null && !avatarTag.isEmpty()) {
                                if (avatarTag.startsWith("http")) {
                                    Glide.with(ProfilePage.this)
                                            .load(avatarTag)
                                            .placeholder(R.drawable.ic_default_avatar)
                                            .centerCrop()
                                            .into(ivProfileImage);
                                } else {
                                    int resId = EditProfile.getAvatarResourceId(avatarTag);
                                    ivProfileImage.setImageResource(resId);
                                }
                            } else {
                                ivProfileImage.setImageResource(R.drawable.ic_default_avatar);
                            }
                        }
                    });
        } else {
            startActivity(new Intent(ProfilePage.this, Login.class));
            finish();
        }
    }
}