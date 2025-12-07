package com.example.mad_gp;

import java.util.ArrayList;
import java.util.List;

public class CommunityPost {
    private String postId; // 文档ID
    private String userName;
    private String userAvatar;
    private String timeAgo;
    private String content;
    private String postImage;
    private int likesCount;
    private int commentCount;
    private List<String> likedBy; // 谁点赞了

    public CommunityPost() {} // Firebase 需要

    // Getters
    public String getPostId() { return postId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getTimeAgo() { return timeAgo; }
    public String getContent() { return content; }
    public String getPostImage() { return postImage; }
    public int getLikesCount() { return likesCount; }
    public int getCommentCount() { return commentCount; }
    public List<String> getLikedBy() { return likedBy == null ? new ArrayList<>() : likedBy; }

    // Setters
    public void setPostId(String postId) { this.postId = postId; }
    // ... 其他 setter 也就是标准写法，Firebase 会自动处理
}