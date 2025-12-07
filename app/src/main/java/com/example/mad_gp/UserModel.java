package com.example.mad_gp;

public class UserModel {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String birthDate;
    private String profileImageUrl; // 预留给头像
    private String bio;             // 预留给简介

    // 空构造函数 (Firebase 需要)
    public UserModel() {
    }

    // 构造函数
    public UserModel(String userId, String name, String email, String phone, String birthDate) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.profileImageUrl = ""; // 默认为空
        this.bio = "Hello, I am using MAD GP!"; // 默认简介
    }

    // Getter 和 Setter 方法
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}