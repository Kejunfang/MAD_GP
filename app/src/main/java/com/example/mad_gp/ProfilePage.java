package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout; // 引入 LinearLayout
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query; // 引入 Query

import java.util.ArrayList;
import java.util.List;

public class ProfilePage extends AppCompatActivity {

    // 1. 基本信息 UI
    private TextView tvUserName, tvUserBio, tvEventCount, tvPostCount;
    private ImageView ivProfileImage, btnEditProfile;
    // ★★★ 新增：Posts 按钮容器
    private LinearLayout btnMyPostsContainer;

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

    // 5. ★★★ 新增：我的帖子列表 (My Posts)
    private TextView tvLabelMyPosts;
    private RecyclerView rvMyPosts;
    private CommunityPostAdapter myPostAdapter; // 复用 Adapter
    private List<CommunityPost> myPostList;
    private boolean isMyPostsVisible = false; // 控制展开/折叠状态

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

        // ★ 绑定 Posts 容器
        btnMyPostsContainer = findViewById(R.id.btnMyPostsContainer);

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

        // --- ★★★ 初始化我的帖子列表 (Vertical, 默认隐藏) ---
        tvLabelMyPosts = findViewById(R.id.tvLabelMyPosts);
        rvMyPosts = findViewById(R.id.rvMyPosts);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        myPostList = new ArrayList<>();
        myPostAdapter = new CommunityPostAdapter(this, myPostList); // 复用 Adapter
        rvMyPosts.setAdapter(myPostAdapter);

        // 点击事件
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, EditProfile.class);
            startActivity(intent);
        });

        setupBottomNav();

        // 开启喜欢帖子的实时监听
        startListeningToLikedPosts();

        // ★★★ 给 Posts 区域添加点击事件
        btnMyPostsContainer.setOnClickListener(v -> toggleMyPosts());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        loadMyAppointments();
        loadParticipatedWorkshops();
        loadMyPostCount();

        // ★★★ 如果当前是展开状态，回来时刷新一下我的帖子数据
        if (isMyPostsVisible) {
            loadMyPosts();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (likedPostsListener != null) {
            likedPostsListener.remove();
        }
    }

    // --- ★★★ 新功能：切换显示/隐藏我的帖子 ---
    private void toggleMyPosts() {
        if (isMyPostsVisible) {
            // 如果本来是显示的，就隐藏
            tvLabelMyPosts.setVisibility(View.GONE);
            rvMyPosts.setVisibility(View.GONE);
            isMyPostsVisible = false;
        } else {
            // 如果本来是隐藏的，就显示并加载数据
            tvLabelMyPosts.setVisibility(View.VISIBLE);
            rvMyPosts.setVisibility(View.VISIBLE);
            isMyPostsVisible = true;
            loadMyPosts();

            // 自动滑到底部查看
            rvMyPosts.post(() -> {
                View scrollView = findViewById(R.id.profileScrollView);
                if(scrollView instanceof androidx.core.widget.NestedScrollView){
                    ((androidx.core.widget.NestedScrollView) scrollView).fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    // --- ★★★ 加载我的帖子数据 ---
    private void loadMyPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("community_posts")
                .whereEqualTo("userId", currentUser.getUid()) // 只查询 userId 等于当前用户的
                .orderBy("timestamp", Query.Direction.DESCENDING) // 按时间倒序
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myPostList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        CommunityPost post = doc.toObject(CommunityPost.class);
                        if (post != null) {
                            post.setPostId(doc.getId());
                            myPostList.add(post);
                        }
                    }
                    myPostAdapter.notifyDataSetChanged();

                    if (myPostList.isEmpty()) {
                        Toast.makeText(this, "You haven't posted anything yet.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfilePage.this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- 1. 实时监听点赞过的帖子 ---
    private void startListeningToLikedPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        if (likedPostsListener != null) {
            likedPostsListener.remove();
        }

        likedPostsListener = db.collection("community_posts")
                .whereArrayContains("likedBy", currentUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        likedPostList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CommunityPost post = doc.toObject(CommunityPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                likedPostList.add(post);
                            }
                        }
                        likedPostAdapter.notifyDataSetChanged();

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

    // --- 2. 加载参与活动 ---
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
                            boolean exists = false;
                            for (Workshop w : participatedWorkshopList) {
                                if (w.getTitle().equals(workshop.getTitle())) {
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