package com.example.mad_gp;

import java.io.Serializable;

public class Workshop implements Serializable {
    private String id;
    private String title;
    private String description;
    private String fullDescription;
    private String location;
    private String price;
    private String imageName;
    private String agenda;

    public Workshop() {}

    public Workshop(String id, String title, String description, String fullDescription, String location, String price, String imageName, String agenda) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fullDescription = fullDescription;
        this.location = location;
        this.price = price;
        this.imageName = imageName;
        this.agenda = agenda;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFullDescription() { return fullDescription; }
    public String getLocation() { return location; }
    public String getPrice() { return price; }
    public String getImageName() { return imageName; }
    public String getAgenda() { return agenda; }
}