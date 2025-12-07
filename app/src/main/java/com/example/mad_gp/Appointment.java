package com.example.mad_gp;

public class Appointment {
    private String counsellorName;
    private String counsellorImage;
    private String date;
    private String time;
    private String location;

    public Appointment() {
        // Firebase 需要空构造函数
    }

    public Appointment(String counsellorName, String counsellorImage, String date, String time, String location) {
        this.counsellorName = counsellorName;
        this.counsellorImage = counsellorImage;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    public String getCounsellorName() { return counsellorName; }
    public String getCounsellorImage() { return counsellorImage; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
}