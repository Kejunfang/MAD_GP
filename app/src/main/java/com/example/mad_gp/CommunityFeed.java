package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CommunityFeed extends AppCompatActivity {

    private RecyclerView rvPosts;
    private CommunityPostAdapter postAdapter;
    private List<CommunityPost> postList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_feed);

        db = FirebaseFirestore.getInstance();

        // 1. 初始化 RecyclerView
        rvPosts = findViewById(R.id.rvCommunityPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new CommunityPostAdapter(this, postList);
        rvPosts.setAdapter(postAdapter);

        // 2. 加载数据 (实时监听)
        loadPosts();

        // 3. 按钮点击事件
        ImageButton btnChat = findViewById(R.id.btnChat);
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityFeed.this, ChatList.class);
            startActivity(intent);
        });

        FloatingActionButton fabPost = findViewById(R.id.fabPost);
        fabPost.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityFeed.this, CreatePost.class);
            startActivity(intent);
        });

        // 4. 底部导航栏
        setupBottomNavigation();
    }

    private void loadPosts() {
        // 使用 addSnapshotListener 可以让点赞数变化时自动更新界面
        db.collection("community_posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(CommunityFeed.this, "Error loading posts", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        postList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CommunityPost post = doc.toObject(CommunityPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId()); // 重要：设置ID以便点赞时使用
                                postList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityFeed.this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navEvent.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityFeed.this, Event.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityFeed.this, ProfilePage.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // navSocial 不需要点击事件，因为已经在当前页
    }
}