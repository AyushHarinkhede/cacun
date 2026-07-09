package com.example.cacun.models;

import java.io.Serializable;

public class Album implements Serializable {
    private String name;
    private String artist;
    private long albumId;
    private int trackCount;

    public Album(String name, String artist, long albumId, int trackCount) {
        this.name = name;
        this.artist = artist;
        this.albumId = albumId;
        this.trackCount = trackCount;
    }

    public String getName() {
        return name != null ? name : "Unknown Album";
    }

    public String getArtist() {
        return artist != null ? artist : "Unknown Artist";
    }

    public long getAlbumId() {
        return albumId;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void incrementTrackCount() {
        this.trackCount++;
    }
}
