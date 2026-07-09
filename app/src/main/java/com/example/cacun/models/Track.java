package com.example.cacun.models;

import java.io.Serializable;

public class Track implements Serializable {
    private long id;
    private String title;
    private String artist;
    private String album;
    private int duration;
    private String path;
    private long albumId;

    public Track() {
    }

    public Track(long id, String title, String artist, String album, int duration, String path, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
        this.albumId = albumId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "Unknown Title";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist != null ? artist : "Unknown Artist";
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album != null ? album : "Unknown Album";
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }
}
