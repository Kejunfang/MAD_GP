package com.example.mad_gp;

import com.google.firebase.Timestamp;

public class Message {
    private String senderId;
    private String receiverId;
    private String message;
    private Timestamp timestamp;

    public Message() { }

    public Message(String senderId, String receiverId, String message, Timestamp timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getMessage() { return message; }
    public Timestamp getTimestamp() { return timestamp; }
}