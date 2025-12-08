package com.example.mad_gp;

import java.io.Serializable;

public class Post implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private String content;
    private String timestamp; // 简化为 String
    private int likeCount;
    private int commentCount;

    public Post() {}

    // 假设这是从 Firebase 自动转换的对象
    public String getId() { return id; }
    public String getContent() { return content; }
    public String getUserName() { return userName; }
    public String getTimestamp() { return timestamp; }
    public int getLikeCount() { return likeCount; }
    public int getCommentCount() { return commentCount; }
    public String getUserId() { return userId; }

    // Setters (如果需要)
    public void setId(String id) { this.id = id; }
    public void setContent(String content) { this.content = content; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public void setUserId(String userId) { this.userId = userId; }
}