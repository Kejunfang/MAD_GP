package com.example.mad_gp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. 获取传递过来的数据
        targetUserId = getIntent().getStringExtra("TARGET_USER_ID");
        targetUserName = getIntent().getStringExtra("USER_NAME");

        if (targetUserId == null) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. 生成 Chat Room ID (确保无论谁发起，ID都一样)
        chatRoomId = getChatRoomId(currentUserId, targetUserId);

        // 3. 初始化 UI
        initViews();

        // 4. 加载消息
        loadMessages();

        // 5. 发送按钮
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        tvChatUserName = findViewById(R.id.tvChatUserName); // 确保 XML 有这个 ID
        btnBack = findViewById(R.id.btnBack);
        rvChat = findViewById(R.id.rvChat); // 确保 XML 有这个 ID
        etMessageInput = findViewById(R.id.etMessageInput); // 确保 XML 有这个 ID
        btnSend = findViewById(R.id.btnSend); // 确保 XML 有这个 ID

        tvChatUserName.setText(targetUserName);
        btnBack.setOnClickListener(v -> finish());

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        rvChat.setAdapter(chatAdapter);
    }

    private void sendMessage() {
        String msgText = etMessageInput.getText().toString().trim();
        if (msgText.isEmpty()) return;

        etMessageInput.setText(""); // 清空输入框

        Timestamp now = new Timestamp(new Date());
        Message message = new Message(currentUserId, targetUserId, msgText, now);

        // 1. 保存消息到 sub-collection
        db.collection("chat_rooms").document(chatRoomId)
                .collection("messages")
                .add(message);

        // 2. 更新聊天室的最新消息 (用于列表显示)
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("participants", Arrays.asList(currentUserId, targetUserId));
        roomData.put("lastMessage", msgText);
        roomData.put("lastMessageTime", now);

        // set(..., SetOptions.merge()) 会创建文档(如果不存在)或更新字段
        db.collection("chat_rooms").document(chatRoomId).set(roomData);
    }

    private void loadMessages() {
        db.collection("chat_rooms").document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING) // 按时间正序
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        // 使用 DocumentChanges 只处理新增的消息，避免重复刷新
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message msg = dc.getDocument().toObject(Message.class);
                                messageList.add(msg);
                                // 通知插入了最后一行
                                chatAdapter.notifyItemInserted(messageList.size() - 1);
                                // 滚动到底部
                                rvChat.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                });
    }

    // 辅助方法：生成唯一的聊天室 ID (按字母顺序排序拼接)
    private String getChatRoomId(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }
}