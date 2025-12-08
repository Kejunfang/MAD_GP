package com.example.mad_gp;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ChatList extends AppCompatActivity {

    private RecyclerView rvChatList;
    private ChatListAdapter adapter;
    private List<ChatRoom> chatRooms;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvChatList = findViewById(R.id.rvChatList);
        rvChatList.setLayoutManager(new LinearLayoutManager(this));

        chatRooms = new ArrayList<>();
        adapter = new ChatListAdapter(this, chatRooms);
        rvChatList.setAdapter(adapter);

        loadChatRooms();
    }

    private void loadChatRooms() {
        if (mAuth.getCurrentUser() == null) return;
        String myId = mAuth.getCurrentUser().getUid();

        // 查询 participants 数组中包含当前用户 ID 的聊天室
        db.collection("chat_rooms")
                .whereArrayContains("participants", myId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        chatRooms.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatRoom room = doc.toObject(ChatRoom.class);
                            if (room != null) {
                                room.setId(doc.getId());
                                chatRooms.add(room);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}