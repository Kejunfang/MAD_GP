package com.example.mad_gp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FollowList extends AppCompatActivity {

    private RecyclerView rvFollowList;
    private UserListAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        //Set Title
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Following");

        // Set btn back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 3. initial recycle view
        rvFollowList = findViewById(R.id.rvFollowList);
        rvFollowList.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserListAdapter(this, userList);
        rvFollowList.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFollowing();
    }

    private void loadFollowing() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .collection("following")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        userList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "You are not following anyone.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> followedIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        followedIds.add(doc.getId());
                    }

                    fetchUsersDetails(followedIds);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load following list.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUsersDetails(List<String> ids) {
        userList.clear();
        adapter.notifyDataSetChanged();

        for (String id : ids) {
            db.collection("users").document(id).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                user.setUserId(documentSnapshot.getId());
                                userList.add(user);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }
}