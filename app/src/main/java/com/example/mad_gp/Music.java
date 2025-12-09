package com.example.mad_gp;

import android.os.Parcel;
import android.os.Parcelable;

public class Music implements Parcelable {
    private String id;
    private String title;
    private String artist;
    private String category;
    private int duration;
    private String coverUrl;
    private String audioUrl;

    public Music() {}

    // 完整构造函数
    public Music(String id, String title, String artist, String category,
                 int duration, String coverUrl, String audioUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.category = category;
        this.duration = duration;
        this.coverUrl = coverUrl;
        this.audioUrl = audioUrl;
    }

    protected Music(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        category = in.readString();
        duration = in.readInt();
        coverUrl = in.readString();
        audioUrl = in.readString();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(category);
        dest.writeInt(duration);
        dest.writeString(coverUrl);
        dest.writeString(audioUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Music music = (Music) o;
        return id != null && id.equals(music.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}