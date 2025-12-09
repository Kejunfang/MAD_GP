package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CommunityFeed extends AppCompatActivity implements CommunityPostAdapter.OnPostActionListener {

    private RecyclerView recyclerView;
    private CommunityPostAdapter adapter;
    private List<CommunityPost> postList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_feed);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        adapter = new CommunityPostAdapter(this, postList, this);
        recyclerView.setAdapter(adapter);

        loadPosts();

        ImageButton btnChat = findViewById(R.id.btnChat);
        if (btnChat != null) {
            btnChat.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityFeed.this, ChatList.class);
                startActivity(intent);
            });
        }

        FloatingActionButton fabPost = findViewById(R.id.fabCreatePost);
        if (fabPost != null) {
            fabPost.setOnClickListener(v ->
                    startActivity(new Intent(CommunityFeed.this, CreatePost.class))
            );
        }

        setupBottomNavigation();
    }

    // navigation Bar
    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityFeed.this, HomePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navEvent != null) {
            navEvent.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityFeed.this, Event.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navSocial != null) {
            navSocial.setOnClickListener(v -> {

            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityFeed.this, ProfilePage.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    private void loadPosts() {
        db.collection("community_posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        postList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CommunityPost post = doc.toObject(CommunityPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                postList.add(post);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    public void onShareClick(CommunityPost post) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareBody = post.getUserName() + " posted: " + post.getContent();
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this post from MAD GP");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    @Override
    public void onCommentClick(CommunityPost post) {
        showCommentDialog(post.getPostId());
    }

    private void showCommentDialog(String postId) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_comment_sheet, null);
        dialog.setContentView(view);

        RecyclerView rvComments = view.findViewById(R.id.rvComments);
        EditText etCommentInput = view.findViewById(R.id.etCommentInput);
        ImageView btnSend = view.findViewById(R.id.btnSendComment);

        List<Comment> commentList = new ArrayList<>();
        CommentAdapter commentAdapter = new CommentAdapter(commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        db.collection("community_posts").document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        commentList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            commentList.add(doc.toObject(Comment.class));
                        }
                        commentAdapter.notifyDataSetChanged();
                        if (commentList.size() > 0) {
                            rvComments.smoothScrollToPosition(commentList.size() - 1);
                        }
                    }
                });

        btnSend.setOnClickListener(v -> {
            String content = etCommentInput.getText().toString().trim();
            if (TextUtils.isEmpty(content)) return;

            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();

                db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                    String name = userDoc.getString("name");
                    if (name == null) name = "User";

                    Comment newComment = new Comment(uid, name, content, Timestamp.now());

                    db.collection("community_posts").document(postId).collection("comments")
                            .add(newComment)
                            .addOnSuccessListener(docRef -> {
                                etCommentInput.setText("");
                                Toast.makeText(this, "Comment sent", Toast.LENGTH_SHORT).show();

                                db.collection("community_posts").document(postId)
                                        .update("commentCount", FieldValue.increment(1));
                            });
                });
            }
        });

        dialog.show();
    }
}