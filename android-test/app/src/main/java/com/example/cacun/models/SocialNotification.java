package com.example.cacun.models;

public class SocialNotification {
    private int id;
    private int accountId;
    private String platform;     // helper field for queries
    private String username;     // helper field for queries
    private String title;
    private String message;
    private long timestamp;
    private boolean isRead;

    public SocialNotification() {}

    public SocialNotification(int id, int accountId, String platform, String username, String title, String message, long timestamp, boolean isRead) {
        this.id = id;
        this.accountId = accountId;
        this.platform = platform;
        this.username = username;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
