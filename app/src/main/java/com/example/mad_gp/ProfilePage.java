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
import com.google.firebase.firestore.ListenerRegistration; // 1. 引入监听器注册类

import java.util.ArrayList;
import java.util.List;

public class ProfilePage extends AppCompatActivity {

    // 1. 基本信息 UI
    private TextView tvUserName, tvUserBio, tvEventCount, tvPostCount;
    private ImageView ivProfileImage, btnEditProfile;

    // 2. 预约列表 (Appointments)
    private TextView tvLabelAppointments;
    private RecyclerView rvAppointments;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;

    // 3. 参与活动列表 (Workshops)
    private TextView tvLabelWorkshops;
    private RecyclerView rvParticipatedWorkshops;
    private ParticipatedWorkshopAdapter participatedAdapter;
    private List<Workshop> participatedWorkshopList;

    // 4. 喜欢的帖子列表 (Liked Posts)
    private TextView tvLabelLikedPosts;
    private RecyclerView rvLikedPosts;
    private CommunityPostAdapter likedPostAdapter;
    private List<CommunityPost> likedPostList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // ★★★ 新增：用于管理实时监听器，防止重复和内存泄漏
    private ListenerRegistration likedPostsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- 绑定基本控件 ---
        tvUserName = findViewById(R.id.tvUserName);
        tvUserBio = findViewById(R.id.tvUserBio);
        tvEventCount = findViewById(R.id.tvEventCount);
        tvPostCount = findViewById(R.id.tvPostCount);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // --- 初始化预约列表 (Vertical) ---
        tvLabelAppointments = findViewById(R.id.tvLabelAppointments);
        rvAppointments = findViewById(R.id.rvAppointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointmentList);
        rvAppointments.setAdapter(appointmentAdapter);

        // --- 初始化活动列表 (Horizontal) ---
        tvLabelWorkshops = findViewById(R.id.tvLabelWorkshops);
        rvParticipatedWorkshops = findViewById(R.id.rvParticipatedWorkshops);
        rvParticipatedWorkshops.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        participatedWorkshopList = new ArrayList<>();
        participatedAdapter = new ParticipatedWorkshopAdapter(this, participatedWorkshopList);
        rvParticipatedWorkshops.setAdapter(participatedAdapter);

        // --- 初始化喜欢帖子列表 (Vertical) ---
        tvLabelLikedPosts = findViewById(R.id.tvLabelLikedPosts);
        rvLikedPosts = findViewById(R.id.rvLikedPosts);
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

        // ★★★ 关键修改：我们在 onCreate 里开启“喜欢帖子”的实时监听
        startListeningToLikedPosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 这里只保留需要手动刷新数据的部分
        // Liked Posts 因为有实时监听，不需要在这里刷新了
        loadUserProfile();
        loadMyAppointments();
        loadParticipatedWorkshops();
        loadMyPostCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ★★★ 退出页面时移除监听，节省资源
        if (likedPostsListener != null) {
            likedPostsListener.remove();
        }
    }

    // --- 1. ★★★ 修改后：实时监听点赞过的帖子 ---
    // 改名为 startListening... 更贴切
    private void startListeningToLikedPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // 如果已经有监听器在运行，先移除（防止重复监听导致闪退或重复数据）
        if (likedPostsListener != null) {
            likedPostsListener.remove();
        }

        // 使用 addSnapshotListener 替代 get()
        likedPostsListener = db.collection("community_posts")
                .whereArrayContains("likedBy", currentUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // 出错处理，例如 Log.e(...)
                        return;
                    }

                    if (value != null) {
                        likedPostList.clear(); // 先清空列表

                        // 遍历新数据
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CommunityPost post = doc.toObject(CommunityPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                likedPostList.add(post);
                            }
                        }

                        // 刷新适配器
                        likedPostAdapter.notifyDataSetChanged();

                        // 根据是否有数据控制显示/隐藏
                        if (likedPostList.isEmpty()) {
                            tvLabelLikedPosts.setVisibility(View.GONE);
                            rvLikedPosts.setVisibility(View.GONE);
                        } else {
                            tvLabelLikedPosts.setVisibility(View.VISIBLE);
                            rvLikedPosts.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    // --- 2. 加载参与活动 (含计数) ---
    private void loadParticipatedWorkshops() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("workshop_registrations")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvEventCount.setText(String.valueOf(count));

                    if (!queryDocumentSnapshots.isEmpty()) {
                        tvLabelWorkshops.setVisibility(View.VISIBLE);
                        rvParticipatedWorkshops.setVisibility(View.VISIBLE);
                        participatedWorkshopList.clear();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String workshopId = doc.getString("workshopId");
                            if (workshopId != null) {
                                fetchWorkshopDetails(workshopId);
                            }
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
                            // 简单的去重检查（防止Workshop多次加载）
                            boolean exists = false;
                            for (Workshop w : participatedWorkshopList) {
                                if (w.getTitle().equals(workshop.getTitle())) { // 假设 Title 唯一，或者你可以比对 ID
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                participatedWorkshopList.add(workshop);
                                participatedAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    // --- 3. 加载预约 ---
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

    // --- 4. 统计发帖数量 ---
    private void loadMyPostCount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("community_posts")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvPostCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                });
    }

    // --- 5. 加载用户信息 ---
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
                                    Glide.with(ProfilePage.this).load(avatarTag).placeholder(R.drawable.ic_default_avatar).into(ivProfileImage);
                                } else {
                                    // 假设 EditProfile 类里有这个静态方法，如果没有请替换为你的逻辑
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