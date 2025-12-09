package com.example.mad_gp;

import com.google.firebase.Timestamp;
import java.util.List;

public class ChatRoom {
    private String id;
    private List<String> participants;
    private String lastMessage;
    private Timestamp lastMessageTime;

    public ChatRoom() {}

    // Getters
    public String getId() { return id; }
    public List<String> getParticipants() { return participants; }
    public String getLastMessage() { return lastMessage; }
    public Timestamp getLastMessageTime() { return lastMessageTime; }

    public void setId(String id) { this.id = id; }
}