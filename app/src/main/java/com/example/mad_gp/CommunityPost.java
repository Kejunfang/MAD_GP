package com.example.mad_gp;

import java.util.ArrayList;
import java.util.List;

public class CommunityPost {
    private String postId;
    private String userName;
    private String userId;
    private String userAvatar;
    private String timeAgo;
    private String content;
    private String postImage;
    private int likesCount;
    private int commentCount;
    private List<String> likedBy;

    public CommunityPost() {}

    // Getters
    public String getPostId() { return postId; }
    public String getUserName() { return userName; }
    public String getUserId() { return userId; }
    public String getUserAvatar() { return userAvatar; }
    public String getTimeAgo() { return timeAgo; }
    public String getContent() { return content; }
    public String getPostImage() { return postImage; }
    public int getLikesCount() { return likesCount; }
    public int getCommentCount() { return commentCount; }
    public List<String> getLikedBy() { return likedBy == null ? new ArrayList<>() : likedBy; }

    // Setters
    public void setPostId(String postId) { this.postId = postId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    public void setContent(String content) { this.content = content; }
    public void setPostImage(String postImage) { this.postImage = postImage; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }
}