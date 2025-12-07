package com.example.mad_gp;

import java.io.Serializable;

// 实现 Serializable 是为了方便在 Intent 中传递整个对象
public class Counsellor implements Serializable {
    private String id; // 文档 ID
    private String name;
    private String title;
    private String location;
    private String imageName;

    public Counsellor() {
        // Firebase 需要空构造函数
    }

    public Counsellor(String id, String name, String title, String location, String imageName) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.location = location;
        this.imageName = imageName;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getImageName() { return imageName; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setTitle(String title) { this.title = title; }
    public void setLocation(String location) { this.location = location; }
    public void setImageName(String imageName) { this.imageName = imageName; }
}