package com.example.cacun.models;

public class SocialPost {
    private int id;
    private int accountId;
    private String platform;     // helper field for queries
    private String username;     // helper field for queries
    private String content;
    private int likes;
    private int comments;
    private int shares;
    private long timestamp;
    private String mediaUrl;

    public SocialPost() {}

    public SocialPost(int id, int accountId, String platform, String username, String content, int likes, int comments, int shares, long timestamp, String mediaUrl) {
        this.id = id;
        this.accountId = accountId;
        this.platform = platform;
        this.username = username;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
        this.shares = shares;
        this.timestamp = timestamp;
        this.mediaUrl = mediaUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }

    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
}
