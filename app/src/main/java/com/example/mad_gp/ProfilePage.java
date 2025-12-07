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
// import com.google.firebase.firestore.Query; // 暂时注释掉，避免需要索引

import java.util.ArrayList;
import java.util.List;

public class ProfilePage extends AppCompatActivity {

    // 基本 UI
    private TextView tvUserName, tvUserBio, tvEventCount, tvPostCount;
    private ImageView ivProfileImage, btnEditProfile;

    // 列表 1: 预约 (竖向)
    private TextView tvLabelAppointments;
    private RecyclerView rvAppointments;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;

    // 列表 2: 参与的活动 (横向)
    private TextView tvLabelWorkshops;
    private RecyclerView rvParticipatedWorkshops;
    private ParticipatedAdapter participatedAdapter; // 这里用上了我们新建的 Adapter
    private List<Workshop> participatedWorkshopList;

    // 列表 3: 点赞的帖子 (竖向)
    private TextView tvLabelLikedPosts;
    private RecyclerView rvLikedPosts;
    private CommunityPostAdapter likedPostAdapter;
    private List<CommunityPost> likedPostList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- 绑定控件 ---
        tvUserName = findViewById(R.id.tvUserName);
        tvUserBio = findViewById(R.id.tvUserBio);
        tvEventCount = findViewById(R.id.tvEventCount);
        tvPostCount = findViewById(R.id.tvPostCount);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        tvLabelAppointments = findViewById(R.id.tvLabelAppointments);
        rvAppointments = findViewById(R.id.rvAppointments);

        tvLabelWorkshops = findViewById(R.id.tvLabelWorkshops);
        rvParticipatedWorkshops = findViewById(R.id.rvParticipatedWorkshops);

        tvLabelLikedPosts = findViewById(R.id.tvLabelLikedPosts);
        rvLikedPosts = findViewById(R.id.rvLikedPosts);

        // --- 初始化 Adapter ---

        // 1. 预约列表
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointmentList);
        rvAppointments.setAdapter(appointmentAdapter);

        // 2. 参与活动列表 (横向)
        rvParticipatedWorkshops.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        participatedWorkshopList = new ArrayList<>();
        participatedAdapter = new ParticipatedAdapter(this, participatedWorkshopList);
        rvParticipatedWorkshops.setAdapter(participatedAdapter);

        // 3. 点赞帖子列表
        rvLikedPosts.setLayoutManager(new LinearLayoutManager(this));
        likedPostList = new ArrayList<>();
        likedPostAdapter = new CommunityPostAdapter(this, likedPostList);
        rvLikedPosts.setAdapter(likedPostAdapter);

        // 点击事件
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, EditProfile.class);
            startActivity(intent);
        });

        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        loadMyAppointments();
        loadParticipatedWorkshops();
        loadLikedPosts();
        loadMyPostCount(); // 加载发帖数量
    }

    // --- 加载数据方法 ---

    // 1. 预约
    private void loadMyAppointments() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("appointments")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        tvLabelAppointments.setVisibility(View.VISIBLE);
                        rvAppointments.setVisibility(View.VISIBLE);
                        appointmentList.clear();
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
                        tvLabelAppointments.setVisibility(View.GONE);
                        rvAppointments.setVisibility(View.GONE);
                    }
                });
    }

    // 2. 参与活动 (含计数)
    private void loadParticipatedWorkshops() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("workshop_registrations")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvEventCount.setText(String.valueOf(count)); // 更新 Events 数量

                    if (!queryDocumentSnapshots.isEmpty()) {
                        tvLabelWorkshops.setVisibility(View.VISIBLE);
                        rvParticipatedWorkshops.setVisibility(View.VISIBLE);
                        participatedWorkshopList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String workshopId = doc.getString("workshopId");
                            if (workshopId != null) fetchWorkshopDetails(workshopId);
                        }
                    } else {
                        tvLabelWorkshops.setVisibility(View.GONE);
                        rvParticipatedWorkshops.setVisibility(View.GONE);
                    }
                });
    }

    private void fetchWorkshopDetails(String workshopId) {
        db.collection("workshops").document(workshopId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Workshop workshop = documentSnapshot.toObject(Workshop.class);
                        if (workshop != null) {
                            participatedWorkshopList.add(workshop);
                            participatedAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    // 3. 点赞的帖子
    private void loadLikedPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("community_posts")
                .whereArrayContains("likedBy", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        tvLabelLikedPosts.setVisibility(View.VISIBLE);
                        rvLikedPosts.setVisibility(View.VISIBLE);
                        likedPostList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            CommunityPost post = doc.toObject(CommunityPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                likedPostList.add(post);
                            }
                        }
                        likedPostAdapter.notifyDataSetChanged();
                    } else {
                        tvLabelLikedPosts.setVisibility(View.GONE);
                        rvLikedPosts.setVisibility(View.GONE);
                    }
                });
    }

    // 4. 统计我发的帖子数量
    private void loadMyPostCount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // 假设 community_posts 里有 userId 字段
        db.collection("community_posts")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvPostCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                });
    }

    // 5. 用户基本信息
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            tvUserName.setText(documentSnapshot.getString("name"));
                            String bio = documentSnapshot.getString("bio");
                            tvUserBio.setText((bio != null && !bio.isEmpty()) ? bio : "This user hasn't written a bio yet.");

                            String avatarTag = documentSnapshot.getString("profileImageUrl");
                            if (avatarTag != null && !avatarTag.isEmpty()) {
                                if (avatarTag.startsWith("http")) {
                                    Glide.with(ProfilePage.this).load(avatarTag).placeholder(R.drawable.ic_default_avatar).centerCrop().into(ivProfileImage);
                                } else {
                                    ivProfileImage.setImageResource(EditProfile.getAvatarResourceId(avatarTag));
                                }
                            } else {
                                ivProfileImage.setImageResource(R.drawable.ic_default_avatar);
                            }
                        }
                    });
        }
    }

    private void setupBottomNav() {
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
}