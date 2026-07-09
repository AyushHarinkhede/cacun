package com.example.cacun.models;

import java.io.Serializable;

public class Artist implements Serializable {
    private String name;
    private int trackCount;

    public Artist(String name, int trackCount) {
        this.name = name;
        this.trackCount = trackCount;
    }

    public String getName() {
        return name != null ? name : "Unknown Artist";
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void incrementTrackCount() {
        this.trackCount++;
    }
}
