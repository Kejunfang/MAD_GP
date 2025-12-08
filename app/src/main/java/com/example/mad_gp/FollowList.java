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

        // 1. 设置标题
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Following");

        // 2. 设置返回键逻辑
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 3. 初始化 RecyclerView
        rvFollowList = findViewById(R.id.rvFollowList);
        rvFollowList.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserListAdapter(this, userList);
        rvFollowList.setAdapter(adapter);

        // 注意：这里去掉了 loadFollowing()，因为它移动到了 onResume()
    }

    // ★★★ 重点修改：使用 onResume 确保每次回到这个页面都会刷新数据 ★★★
    @Override
    protected void onResume() {
        super.onResume();
        loadFollowing();
    }

    private void loadFollowing() {
        if (currentUserId == null) return;

        // 1. 获取 "following" 集合中的所有 ID
        db.collection("users").document(currentUserId)
                .collection("following")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // ★★★ 修改：如果发现没有关注任何人，要清空列表并刷新，否则可能还显示着旧数据
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

                    // 2. 根据 ID 获取用户详细信息
                    fetchUsersDetails(followedIds);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load following list.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUsersDetails(List<String> ids) {
        // 清空列表，防止数据重复叠加
        userList.clear();
        // 先通知一下适配器清空了，防止视觉上没反应
        adapter.notifyDataSetChanged();

        for (String id : ids) {
            db.collection("users").document(id).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                user.setUserId(documentSnapshot.getId());
                                userList.add(user);
                                adapter.notifyDataSetChanged(); // 每加载到一个就刷新一次
                            }
                        }
                    });
        }
    }
}