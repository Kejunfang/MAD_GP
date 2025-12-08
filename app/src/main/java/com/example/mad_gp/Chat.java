package com.example.mad_gp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // ★ 引入 Glide
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat extends AppCompatActivity {

    private String chatRoomId;
    private String targetUserId;
    private String targetUserName;
    private String currentUserId;

    private TextView tvChatUserName;
    private ImageButton btnBack;
    private RecyclerView rvChat;
    private EditText etMessageInput;
    private ImageView btnSend;
    private ImageView ivChatAvatar; // ★ 新增：头像控件

    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");
        targetUserName = getIntent().getStringExtra("USER_NAME");

        if (targetUserId == null) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatRoomId = getChatRoomId(currentUserId, targetUserId);

        initViews();
        loadTargetUserInfo(); // ★ 新增：加载对方头像
        loadMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        tvChatUserName = findViewById(R.id.tvChatUserName);
        btnBack = findViewById(R.id.btnBack);
        rvChat = findViewById(R.id.rvChat);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSend);

        // ★ 请确保你的 activity_chat.xml 里面有一个 ImageView 的 ID 是 ivChatAvatar
        // 如果你的 XML 只有名字没有头像，你需要加一个 ImageView 在名字旁边
        ivChatAvatar = findViewById(R.id.ivChatAvatar);

        tvChatUserName.setText(targetUserName);
        btnBack.setOnClickListener(v -> finish());

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        rvChat.setAdapter(chatAdapter);
    }

    // ★★★ 新增方法：加载对方的头像 ★★★
    private void loadTargetUserInfo() {
        db.collection("users").document(targetUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String avatarUrl = documentSnapshot.getString("profileImageUrl");

                        // 使用 Glide 加载图片
                        if (ivChatAvatar != null) {
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                if (avatarUrl.startsWith("http")) {
                                    Glide.with(this).load(avatarUrl).into(ivChatAvatar);
                                } else {
                                    // 处理本地资源名 (例如 "counsellor1")
                                    int resId = getResources().getIdentifier(avatarUrl, "drawable", getPackageName());
                                    if (resId != 0) ivChatAvatar.setImageResource(resId);
                                    else ivChatAvatar.setImageResource(R.drawable.ic_default_avatar);
                                }
                            } else {
                                ivChatAvatar.setImageResource(R.drawable.ic_default_avatar);
                            }
                        }
                    }
                });
    }

    private void sendMessage() {
        String msgText = etMessageInput.getText().toString().trim();
        if (msgText.isEmpty()) return;

        etMessageInput.setText("");

        Timestamp now = new Timestamp(new Date());
        Message message = new Message(currentUserId, targetUserId, msgText, now);

        db.collection("chat_rooms").document(chatRoomId)
                .collection("messages")
                .add(message);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("participants", Arrays.asList(currentUserId, targetUserId));
        roomData.put("lastMessage", msgText);
        roomData.put("lastMessageTime", now);

        // ★ 同时也存入 participantsIds 以便查询更精准 (可选，但推荐)
        db.collection("chat_rooms").document(chatRoomId).set(roomData);
    }

    private void loadMessages() {
        db.collection("chat_rooms").document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message msg = dc.getDocument().toObject(Message.class);
                                messageList.add(msg);
                                chatAdapter.notifyItemInserted(messageList.size() - 1);
                                rvChat.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                });
    }

    private String getChatRoomId(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }
}