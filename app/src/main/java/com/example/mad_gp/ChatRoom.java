package com.example.mad_gp;

import com.google.firebase.Timestamp;
import java.util.List;

public class ChatRoom {
    private String id; // 聊天室ID
    private List<String> participants; // 参与者 ID 列表
    private String lastMessage;
    private Timestamp lastMessageTime;

    public ChatRoom() {} // Firebase 需要

    // Getters
    public String getId() { return id; }
    public List<String> getParticipants() { return participants; }
    public String getLastMessage() { return lastMessage; }
    public Timestamp getLastMessageTime() { return lastMessageTime; }

    public void setId(String id) { this.id = id; }
}