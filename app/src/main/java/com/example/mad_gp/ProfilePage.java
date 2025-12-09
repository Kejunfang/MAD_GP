package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

// Achive CommunityPostAdapter.OnPostActionListener api
public class ProfilePage extends AppCompatActivity implements CommunityPostAdapter.OnPostActionListener {

    private TextView tvUserName, tvUserBio, tvEventCount, tvPostCount;
    private TextView tvFollowingCount;

    private ImageView ivProfileImage, btnEditProfile;

    private LinearLayout btnMyPostsContainer;
    private LinearLayout btnFollowingContainer;

    private TextView tvLabelAppointments;
    private RecyclerView rvAppointments;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;
    private TextView tvLabelWorkshops;
    private RecyclerView rvParticipatedWorkshops;
    private ParticipatedWorkshopAdapter participatedAdapter;
    private List<Workshop> participatedWorkshopList;
    private TextView tvLabelLikedPosts;
    private RecyclerView rvLikedPosts;
    private CommunityPostAdapter likedPostAdapter;
    private List<CommunityPost> likedPostList;

    private TextView tvLabelMyPosts;
    private RecyclerView rvMyPosts;
    private CommunityPostAdapter myPostAdapter;
    private List<CommunityPost> myPostList;
    private boolean isMyPostsVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ListenerRegistration likedPostsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvUserName = findViewById(R.id.tvUserName);
        tvUserBio = findViewById(R.id.tvUserBio);
        tvEventCount = findViewById(R.id.tvEventCount);
        tvPostCount = findViewById(R.id.tvPostCount);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        btnFollowingContainer = findViewById(R.id.btnFollowingContainer);
        btnMyPostsContainer = findViewById(R.id.btnMyPostsContainer);

        tvLabelAppointments = findViewById(R.id.tvLabelAppointments);
        rvAppointments = findViewById(R.id.rvAppointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointmentList);
        rvAppointments.setAdapter(appointmentAdapter);

        tvLabelWorkshops = findViewById(R.id.tvLabelWorkshops);
        rvParticipatedWorkshops = findViewById(R.id.rvParticipatedWorkshops);
        rvParticipatedWorkshops.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        participatedWorkshopList = new ArrayList<>();
        participatedAdapter = new ParticipatedWorkshopAdapter(this, participatedWorkshopList);
        rvParticipatedWorkshops.setAdapter(participatedAdapter);

        tvLabelLikedPosts = findViewById(R.id.tvLabelLikedPosts);
        rvLikedPosts = findViewById(R.id.rvLikedPosts);
        rvLikedPosts.setLayoutManager(new LinearLayoutManager(this));
        likedPostList = new ArrayList<>();

        likedPostAdapter = new CommunityPostAdapter(this, likedPostList, this);
        rvLikedPosts.setAdapter(likedPostAdapter);

        tvLabelMyPosts = findViewById(R.id.tvLabelMyPosts);
        rvMyPosts = findViewById(R.id.rvMyPosts);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(this));
        myPostList = new ArrayList<>();

        myPostAdapter = new CommunityPostAdapter(this, myPostList, this);
        rvMyPosts.setAdapter(myPostAdapter);

        btnEditProfile.setOnClickListener(this::showPopupMenu);

        btnMyPostsContainer.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, MyPosts.class);
            startActivity(intent);
        });

        btnFollowingContainer.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, FollowList.class);
            startActivity(intent);
        });

        setupBottomNav();

        startListeningToLikedPosts();
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 1, 0, "Edit Profile");
        popup.getMenu().add(0, 2, 1, "Log Out");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    Intent intent = new Intent(ProfilePage.this, EditProfile.class);
                    startActivity(intent);
                    return true;
                case 2:
                    logout();
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(ProfilePage.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        loadMyAppointments();
        loadParticipatedWorkshops();
        loadMyPostCount();
        loadFollowingCount();

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

    private void loadFollowingCount() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .collection("following")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvFollowingCount.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> tvFollowingCount.setText("0"));
    }

    private void toggleMyPosts() {
        if (isMyPostsVisible) {
            tvLabelMyPosts.setVisibility(View.GONE);
            rvMyPosts.setVisibility(View.GONE);
            isMyPostsVisible = false;
        } else {
            tvLabelMyPosts.setVisibility(View.VISIBLE);
            rvMyPosts.setVisibility(View.VISIBLE);
            isMyPostsVisible = true;
            loadMyPosts();

            rvMyPosts.post(() -> {
                View scrollView = findViewById(R.id.profileScrollView);
                if(scrollView instanceof androidx.core.widget.NestedScrollView){
                    ((androidx.core.widget.NestedScrollView) scrollView).fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    private void loadMyPosts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("community_posts")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
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
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfilePage.this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

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
    //Navigation Bar
    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navEvent = findViewById(R.id.navEvent);
        LinearLayout navSocial = findViewById(R.id.navSocial);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(ProfilePage.this, HomePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navEvent != null) {
            navEvent.setOnClickListener(v -> {
                Intent intent = new Intent(ProfilePage.this, Event.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navSocial != null) {
            navSocial.setOnClickListener(v -> {
                Intent intent = new Intent(ProfilePage.this, CommunityFeed.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {

            });
        }
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