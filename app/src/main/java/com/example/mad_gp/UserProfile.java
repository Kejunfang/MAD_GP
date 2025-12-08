package com.example.mad_gp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1. 实现 OnPostActionListener 接口
public class UserProfile extends AppCompatActivity implements CommunityPostAdapter.OnPostActionListener {

    private ImageView ivUserAvatar;
    private TextView tvUserName, tvUserBio;
    private MaterialButton btnFollow, btnMessage;
    private RecyclerView rvUserPosts;

    private CommunityPostAdapter postAdapter;
    private List<CommunityPost> userPostList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String targetUserId;
    private String currentUserId;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        if (targetUserId == null || targetUserId.isEmpty()) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserInfo();
        loadUserPosts();
        setupButtons();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserBio = findViewById(R.id.tvUserBio);
        btnFollow = findViewById(R.id.btnFollow);
        btnMessage = findViewById(R.id.btnMessage);
        rvUserPosts = findViewById(R.id.rvUserPosts);

        btnBack.setOnClickListener(v -> finish());

        rvUserPosts.setLayoutManager(new LinearLayoutManager(this));
        userPostList = new ArrayList<>();

        // 2. 这里修改构造函数，传入 this 作为监听器
        postAdapter = new CommunityPostAdapter(this, userPostList, this);
        rvUserPosts.setAdapter(postAdapter);
    }

    private void loadUserInfo() {
        db.collection("users").document(targetUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvUserName.setText(name != null ? name : "Unknown");

                        String bio = documentSnapshot.getString("bio");
                        tvUserBio.setText(bio != null && !bio.isEmpty() ? bio : "This user hasn't written a bio yet.");

                        String avatarTag = documentSnapshot.getString("profileImageUrl");
                        if (avatarTag != null) {
                            if (avatarTag.startsWith("http")) {
                                Glide.with(this).load(avatarTag).into(ivUserAvatar);
                            } else {
                                int resId = getResources().getIdentifier(avatarTag, "drawable", getPackageName());
                                if (resId != 0) ivUserAvatar.setImageResource(resId);
                                else ivUserAvatar.setImageResource(R.drawable.ic_default_avatar);
                            }
                        }
                    }
                });
    }

    private void loadUserPosts() {
        Log.d("UserProfile", "Loading posts for user: " + targetUserId);

        db.collection("community_posts")
                .whereEqualTo("userId", targetUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        // 注意：如果出现 "The query requires an index" 错误，请查看 Logcat 中的链接去创建索引
                        Toast.makeText(this, "Load failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("UserProfile", "Error loading posts", error);
                        return;
                    }

                    userPostList.clear();
                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CommunityPost post = doc.toObject(CommunityPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                userPostList.add(post);
                            }
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                });
    }

    private void setupButtons() {
        if (currentUserId != null && currentUserId.equals(targetUserId)) {
            btnFollow.setVisibility(View.GONE);
            btnMessage.setVisibility(View.GONE);
            return;
        }

        checkFollowStatus();

        btnFollow.setOnClickListener(v -> {
            if (currentUserId == null) return;

            btnFollow.setEnabled(false);

            if (isFollowing) {
                db.collection("users").document(currentUserId)
                        .collection("following").document(targetUserId)
                        .delete();

                db.collection("users").document(targetUserId)
                        .collection("followers").document(currentUserId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            isFollowing = false;
                            updateFollowButtonUI();
                            Toast.makeText(UserProfile.this, "Unfollowed", Toast.LENGTH_SHORT).show();
                            btnFollow.setEnabled(true);
                        })
                        .addOnFailureListener(e -> btnFollow.setEnabled(true));

            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", com.google.firebase.Timestamp.now());

                db.collection("users").document(currentUserId)
                        .collection("following").document(targetUserId)
                        .set(data);

                db.collection("users").document(targetUserId)
                        .collection("followers").document(currentUserId)
                        .set(data)
                        .addOnSuccessListener(aVoid -> {
                            isFollowing = true;
                            updateFollowButtonUI();
                            Toast.makeText(UserProfile.this, "Followed!", Toast.LENGTH_SHORT).show();
                            btnFollow.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(UserProfile.this, "Failed to follow", Toast.LENGTH_SHORT).show();
                            btnFollow.setEnabled(true);
                        });
            }
        });

        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, Chat.class);
            intent.putExtra("USER_NAME", tvUserName.getText().toString());
            intent.putExtra("TARGET_USER_ID", targetUserId);
            startActivity(intent);
        });
    }

    private void checkFollowStatus() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .collection("following").document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFollowing = documentSnapshot.exists();
                    updateFollowButtonUI();
                });
    }

    private void updateFollowButtonUI() {
        if (isFollowing) {
            btnFollow.setText("Following");
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            btnFollow.setTextColor(getResources().getColor(R.color.text_main));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.sage_green)));
            btnFollow.setTextColor(getResources().getColor(R.color.white));
        }
    }

    // --- 3. 实现接口方法：评论点击 ---
    @Override
    public void onCommentClick(CommunityPost post) {
        showCommentDialog(post.getPostId());
    }

    // --- 4. 实现接口方法：分享点击 ---
    public void onShareClick(CommunityPost post) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareBody = post.getUserName() + " posted: " + post.getContent();
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this post from MAD GP");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    // --- 5. 添加显示评论弹窗的逻辑 ---
    private void showCommentDialog(String postId) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        // 确保你的布局文件中有 dialog_comment_sheet.xml
        View view = getLayoutInflater().inflate(R.layout.dialog_comment_sheet, null);
        dialog.setContentView(view);

        RecyclerView rvComments = view.findViewById(R.id.rvComments);
        EditText etCommentInput = view.findViewById(R.id.etCommentInput);
        ImageView btnSend = view.findViewById(R.id.btnSendComment);

        List<Comment> commentList = new ArrayList<>();
        CommentAdapter commentAdapter = new CommentAdapter(commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        // 加载评论
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

        // 发送评论
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

                                // 更新帖子评论数
                                db.collection("community_posts").document(postId)
                                        .update("commentCount", FieldValue.increment(1));
                            });
                });
            }
        });

        dialog.show();
    }
}