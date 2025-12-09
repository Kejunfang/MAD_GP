package com.example.mad_gp;

public class DailyTip {
    private String id;
    private String title;
    private String subtitle;
    private String imageUrl;

    public DailyTip() {

    }

    public DailyTip(String id, String title, String subtitle, String imageUrl) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getImageUrl() { return imageUrl; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}