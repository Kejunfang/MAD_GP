package com.example.mad_gp;

import java.io.Serializable;

public class Counsellor implements Serializable {
    private String id;
    private String name;
    private String title;
    private String location;
    private String imageName;

    public Counsellor() {
    }

    public Counsellor(String id, String name, String title, String location, String imageName) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.location = location;
        this.imageName = imageName;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
}