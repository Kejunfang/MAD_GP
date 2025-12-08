package com.example.mad_gp;

import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "MyPosts";
    private RecyclerView rvMyPosts;
    private CommunityPostAdapter postAdapter;
    private List<CommunityPost> postList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        Log.d(TAG, "onCreate called");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        rvMyPosts = findViewById(R.id.rvMyPosts);

        if (rvMyPosts == null) {
            Log.e(TAG, "RecyclerView is NULL!");
            return;
        }

        postList = new ArrayList<>();
        postAdapter = new CommunityPostAdapter(this, postList);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        rvMyPosts.setAdapter(postAdapter);

        Log.d(TAG, "RecyclerView setup complete");

        loadUserPosts();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User is not logged in!");
            Toast.makeText(this, "Please log in to view your posts.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Loading posts for user: " + userId);

        // ★★★ 修改重点：使用 addSnapshotListener 替换 get() ★★★
        // 这样当你点赞时，数据库更新，这里会自动收到通知并刷新列表，红心就会变色
        db.collection("community_posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING) // 建议加上排序，如果报错请看 Logcat 创建索引
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading posts", error);
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (value != null) {
                        postList.clear();
                        if (!value.isEmpty()) {
                            Log.d(TAG, "Documents found: " + value.size());
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                CommunityPost post = doc.toObject(CommunityPost.class);
                                if (post != null) {
                                    post.setPostId(doc.getId()); // 关键：设置ID用于点赞
                                    postList.add(post);
                                    Log.d(TAG, "Added post: " + post.getContent());
                                }
                            }
                        } else {
                            Log.d(TAG, "No posts found for this user");
                            // 列表为空时，这里可以选择显示一个 Empty View 或者 Toast
                            // Toast.makeText(this, "You haven't made any posts yet.", Toast.LENGTH_SHORT).show();
                        }

                        // 刷新列表
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }
}