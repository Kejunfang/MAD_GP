package com.example.mad_gp;

import com.google.firebase.Timestamp;

public class Comment {
    private String commentId;
    private String userId;
    private String userName;
    private String content;
    private Timestamp timestamp;

    public Comment() { } // Firebase 需要空构造函数

    public Comment(String userId, String userName, String content, Timestamp timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public Timestamp getTimestamp() { return timestamp; }
}