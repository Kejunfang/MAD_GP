package com.example.mad_gp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MyPosts extends AppCompatActivity {

    private RecyclerView rvMyPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        rvMyPosts = findViewById(R.id.rvMyPosts);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        rvMyPosts.setAdapter(postAdapter);

        loadUserPosts();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view your posts.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 核心查询：只获取当前用户发布的帖子
        db.collection("posts")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING) // 假设你有 timestamp 字段，最新的在前面
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        postList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            // 自动将 Firestore 文档转换为 Post 对象
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setId(doc.getId()); // 手动设置 ID
                                postList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "You haven't made any posts yet.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}