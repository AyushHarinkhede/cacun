package com.example.cacun.models;

public class SocialAccount {
    private int id;
    private String platform;
    private String username;
    private String apiKey;
    private int followers;
    private int streak;
    private int screenTime; // spent screen time in minutes
    private boolean isOnline;
    private long lastUpdated;

    public SocialAccount() {}

    public SocialAccount(int id, String platform, String username, String apiKey, int followers, int streak, int screenTime, boolean isOnline, long lastUpdated) {
        this.id = id;
        this.platform = platform;
        this.username = username;
        this.apiKey = apiKey;
        this.followers = followers;
        this.streak = streak;
        this.screenTime = screenTime;
        this.isOnline = isOnline;
        this.lastUpdated = lastUpdated;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public int getFollowers() { return followers; }
    public void setFollowers(int followers) { this.followers = followers; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public int getScreenTime() { return screenTime; }
    public void setScreenTime(int screenTime) { this.screenTime = screenTime; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    // Helper to get initials for platform abbreviation
    public String getPlatformAbbreviation() {
        if (platform == null || platform.isEmpty()) return "?";
        switch (platform.toLowerCase()) {
            case "instagram": return "IG";
            case "facebook": return "FB";
            case "x": return "X";
            case "snapchat": return "SC";
            case "reddit": return "RD";
            case "linkedin": return "LI";
            case "github": return "GH";
            case "youtube": return "YT";
            case "gmail": return "GM";
            case "pinterest": return "PI";
            case "signal": return "SG";
            case "duolingo": return "DL";
            default: return platform.substring(0, Math.min(platform.length(), 2)).toUpperCase();
        }
    }
}
