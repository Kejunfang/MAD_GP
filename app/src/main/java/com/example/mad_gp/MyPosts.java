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

        // ★ 暂时去掉 orderBy,等索引创建完成后再加回来
        db.collection("community_posts")
                .whereEqualTo("userId", userId)
                // .orderBy("timestamp", Query.Direction.DESCENDING) // ← 先注释掉这行
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Query successful. Documents found: " + queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        postList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Log.d(TAG, "Processing document: " + doc.getId());

                            CommunityPost post = doc.toObject(CommunityPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                postList.add(post);
                                Log.d(TAG, "Added post with content: " + post.getContent());
                            }
                        }

                        Log.d(TAG, "Final list size: " + postList.size());
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "No posts found for this user");
                        Toast.makeText(this, "You haven't made any posts yet.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading posts", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}