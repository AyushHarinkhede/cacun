package com.example.cacun.api;

import android.content.Context;
import android.util.Log;

import com.example.cacun.database.DatabaseHelper;
import com.example.cacun.models.SocialAccount;
import com.example.cacun.models.SocialNotification;
import com.example.cacun.models.SocialPost;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class SocialApiManager {

    private static final String TAG = "SocialApiManager";
    private final DatabaseHelper dbHelper;
    private final Random random = new Random();

    public SocialApiManager(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // Callback for async API calls
    public interface ApiCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Connects an account: generates initial data (deterministic simulation + attempts real API sync if supported)
     */
    public void connectAccount(final SocialAccount account, final ApiCallback callback) {
        // 1. Generate deterministic mock data first as baseline/fallback
        generateInitialData(account);

        // 2. Save it initially
        long accountId = dbHelper.addAccount(account);
        account.setId((int) accountId);

        // Add initial mock posts and notification based on platform
        createInitialContent(account);

        // 3. For real APIs, trigger an immediate async fetch to overwrite with live data
        String platform = account.getPlatform().toLowerCase();
        if (platform.equals("github") || platform.equals("reddit")) {
            syncAccountLive(account, new ApiCallback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess();
                }

                @Override
                public void onFailure(Exception e) {
                    // Fail gracefully, keep mock baseline
                    Log.w(TAG, "Live sync failed, using simulation: " + e.getMessage());
                    callback.onSuccess();
                }
            });
        } else {
            // Simulated platforms complete immediately
            callback.onSuccess();
        }
    }

    /**
     * Deterministically generates starting stats based on username hash
     */
    private void generateInitialData(SocialAccount account) {
        int hash = Math.abs(account.getUsername().hashCode());
        
        // Random-like but consistent for same username
        int baseFollowers = (hash % 15000) + 150;
        int baseStreak = (hash % 30) + 1;
        int screenTime = hash % 60;
        boolean isOnline = (hash % 2 == 0);

        // Specific platforms adjust base stats
        String platform = account.getPlatform().toLowerCase();
        if (platform.equals("youtube")) {
            baseFollowers = (hash % 200000) + 1200; // subscriber count
        } else if (platform.equals("duolingo")) {
            baseFollowers = (hash % 50) + 5; // Duolingo followers are small
            baseStreak = (hash % 120) + 3; // Streaks can be very long
        } else if (platform.equals("signal")) {
            baseFollowers = (hash % 20) + 2; // Contacts
            baseStreak = 0; // Signal doesn't have streaks
        } else if (platform.equals("gmail")) {
            baseFollowers = (hash % 300) + 50; // Total emails
            baseStreak = 0;
        }

        account.setFollowers(baseFollowers);
        account.setStreak(baseStreak);
        account.setScreenTime(screenTime);
        account.setOnline(isOnline);
    }

    /**
     * Generates initial feed posts and a welcome alert in the database
     */
    private void createInitialContent(SocialAccount account) {
        String platform = account.getPlatform().toLowerCase();
        String username = account.getUsername();
        long now = System.currentTimeMillis();

        // 1. Create Welcome Alert
        SocialNotification welcome = new SocialNotification();
        welcome.setAccountId(account.getId());
        welcome.setTitle("Account Linked");
        welcome.setMessage("Successfully connected " + account.getPlatform() + " profile @" + username + " to Cacun.");
        welcome.setTimestamp(now);
        welcome.setRead(false);
        dbHelper.addNotification(welcome);

        // 2. Create Platform-specific Initial Posts
        String[] templates;
        switch (platform) {
            case "instagram":
                templates = new String[]{
                    "Chasing sunsets and good vibes 🌅✨ #aesthetic #lifestyle",
                    "A Sunday well spent brings a week of content. ☕📖 #weekend",
                    "Throwback to last summer. Missing the ocean breeze! 🌊🏖️"
                };
                break;
            case "facebook":
                templates = new String[]{
                    "Had an amazing dinner tonight with the family. Grateful for these moments! ❤️🍲",
                    "Excited to start a new position today! Thanks for all the support."
                };
                break;
            case "x":
                templates = new String[]{
                    "Why does Gradle sync always take exactly long enough for me to make coffee? ☕ #androiddev",
                    "AI is moving so fast. What a time to be building! 🚀"
                };
                break;
            case "snapchat":
                templates = new String[]{
                    "Live from the streets! 🍕📸",
                    "Streak maintained! Double snap back later. 🙌"
                };
                break;
            case "reddit":
                templates = new String[]{
                    "TIL that sea otters hold hands when they sleep so they don't drift apart. 🦦",
                    "My app compiles on the first try and I don't know whether to celebrate or be suspicious."
                };
                break;
            case "linkedin":
                templates = new String[]{
                    "I am thrilled to announce that I have successfully completed the Mobile System Architecture certification! 📜 #learning",
                    "Networking is not about connecting people. It's about connecting people with people, ideas, and opportunities."
                };
                break;
            case "github":
                templates = new String[]{
                    "Initial commit: Refactored mobile codebase to clean Java architecture. 🛠️",
                    "Merged PR #12: Added live Simulation Engine for social dashboards. ✅"
                };
                break;
            case "youtube":
                templates = new String[]{
                    "New Video: How I Built a Social Aggregator Android App in Java from Scratch! 🎥💻",
                    "Hitting 100k subscribers soon! Thank you guys so much! 🎉"
                };
                break;
            case "duolingo":
                templates = new String[]{
                    "Completed a 15-minute French lesson today! Perfect score 💯. #languages",
                    "Duo the owl will find me if I don't practice my Spanish today. 🦉💀"
                };
                break;
            case "gmail":
                templates = new String[]{
                    "Inbox Zero achieved. It lasted for exactly 4 seconds. 📧",
                    "Welcome to Gmail! Here is a guide on how to filter spam."
                };
                break;
            default:
                templates = new String[]{
                    "Hello world from my new connected social account! #cacun",
                    "Staying connected via the unified Cacun dashboard."
                };
                break;
        }

        // Add them to database with random metrics
        for (int i = 0; i < templates.length; i++) {
            SocialPost post = new SocialPost();
            post.setAccountId(account.getId());
            post.setContent(templates[i]);
            
            int seed = Math.abs((username + templates[i]).hashCode());
            post.setLikes((seed % 400) + 12);
            post.setComments((seed % 80) + 2);
            post.setShares((seed % 20) + 0);
            
            // Stagger timestamps
            post.setTimestamp(now - (i * 3 * 3600 * 1000L) - (seed % 3600000L));
            post.setMediaUrl(""); // No media file, uses gradient placeholder
            dbHelper.addPost(post);
        }
    }

    /**
     * Triggers live API requests for GitHub / Reddit
     */
    public void syncAccountLive(final SocialAccount account, final ApiCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String platform = account.getPlatform().toLowerCase();
                    if (platform.equals("github")) {
                        fetchGitHubLive(account);
                    } else if (platform.equals("reddit")) {
                        fetchRedditLive(account);
                    }
                    callback.onSuccess();
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        }).start();
    }

    /**
     * Connects to api.github.com to retrieve user profile data
     */
    private void fetchGitHubLive(SocialAccount account) throws Exception {
        String urlString = "https://api.github.com/users/" + account.getUsername();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Cacun-Android-App");
        conn.setConnectTimeout(6000);
        conn.setReadTimeout(6000);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            int followers = json.getInt("followers");
            int repos = json.getInt("public_repos");

            // Update in DB
            account.setFollowers(followers);
            dbHelper.updateAccountMetrics(account.getId(), followers, repos, account.getScreenTime(), account.isOnline());
            
            Log.d(TAG, "GitHub live sync completed for @" + account.getUsername() + " (Followers: " + followers + ")");
        } else {
            throw new Exception("GitHub API returned HTTP " + responseCode);
        }
    }

    /**
     * Connects to reddit.com to retrieve user about profile data
     */
    private void fetchRedditLive(SocialAccount account) throws Exception {
        String urlString = "https://www.reddit.com/user/" + account.getUsername() + "/about.json";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Cacun-Android-App");
        conn.setConnectTimeout(6000);
        conn.setReadTimeout(6000);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject root = new JSONObject(response.toString());
            JSONObject data = root.getJSONObject("data");
            int karma = data.getInt("total_karma");

            // Update in DB
            account.setFollowers(karma);
            dbHelper.updateAccountMetrics(account.getId(), karma, account.getStreak(), account.getScreenTime(), account.isOnline());
            
            Log.d(TAG, "Reddit live sync completed for @" + account.getUsername() + " (Karma: " + karma + ")");
        } else {
            throw new Exception("Reddit API returned HTTP " + responseCode);
        }
    }

    /**
     * Periodically called to simulate live updates (drifts follower counts, active screens, posts likes, new alerts)
     */
    public boolean simulateLiveUpdates() {
        List<SocialAccount> accounts = dbHelper.getAllAccounts();
        if (accounts.isEmpty()) return false;

        boolean dataChanged = false;
        long now = System.currentTimeMillis();

        // 1. Drift follower count and accumulate screen time for connected accounts
        for (SocialAccount acct : accounts) {
            boolean shouldUpdate = random.nextInt(3) == 0; // 33% chance per tick
            if (shouldUpdate) {
                // Drift followers (+5 or -2)
                int deltaFollowers = random.nextInt(8) - 2; 
                int newFollowers = Math.max(0, acct.getFollowers() + deltaFollowers);
                
                // Screen time accumulates if online
                int newScreenTime = acct.getScreenTime();
                if (acct.isOnline()) {
                    newScreenTime += random.nextInt(3) + 1; // +1 to 3 mins
                }

                // Randomly toggle online status
                boolean newOnlineStatus = acct.isOnline();
                if (random.nextInt(10) == 0) { // 10% chance to flip online status
                    newOnlineStatus = !newOnlineStatus;
                }

                acct.setFollowers(newFollowers);
                acct.setScreenTime(newScreenTime);
                acct.setOnline(newOnlineStatus);
                dbHelper.updateAccountMetrics(acct.getId(), newFollowers, acct.getStreak(), newScreenTime, newOnlineStatus);
                dataChanged = true;
            }
        }

        // 2. Drift likes/comments on existing posts
        List<SocialPost> posts = dbHelper.getAllFeedPosts();
        for (SocialPost post : posts) {
            if (random.nextInt(5) == 0) { // 20% chance per post
                int likesDelta = random.nextInt(5) + 1;
                int commentsDelta = random.nextBoolean() ? random.nextInt(2) : 0;
                int sharesDelta = random.nextInt(10) == 0 ? 1 : 0;

                dbHelper.updatePostMetrics(post.getId(), 
                        post.getLikes() + likesDelta, 
                        post.getComments() + commentsDelta, 
                        post.getShares() + sharesDelta);
                dataChanged = true;
            }
        }

        // 3. Occasionally generate a new Notification/Alert
        if (random.nextInt(15) == 0) { // ~6% chance per sync loop
            SocialAccount luckyAcct = accounts.get(random.nextInt(accounts.size()));
            SocialNotification notif = new SocialNotification();
            notif.setAccountId(luckyAcct.getId());
            notif.setTimestamp(now);
            notif.setRead(false);

            String platform = luckyAcct.getPlatform().toLowerCase();
            String user = luckyAcct.getUsername();

            switch (platform) {
                case "instagram":
                    notif.setTitle("New Like");
                    notif.setMessage("@some_user liked your recent Instagram photo.");
                    break;
                case "x":
                    notif.setTitle("Mentioned in tweet");
                    notif.setMessage("@tech_guru replied to your tweet: 'Great project!'");
                    break;
                case "duolingo":
                    notif.setTitle("Streak Warning! 🦉");
                    notif.setMessage("Duo says: Complete your daily French lesson to save your " + luckyAcct.getStreak() + "-day streak!");
                    notif.setTimestamp(now);
                    // Might decrement streak in db if ignored (for realism, but here we just alert)
                    break;
                case "github":
                    notif.setTitle("Starred repository");
                    notif.setMessage("@dev_alice starred your repository 'cacun-mobile'.");
                    break;
                case "gmail":
                    notif.setTitle("New Email");
                    notif.setMessage("From: security@google.com - New login detected in Windows.");
                    break;
                case "snapchat":
                    notif.setTitle("New Snap");
                    notif.setMessage("Snap from @best_friend (Double Tap to View).");
                    break;
                default:
                    notif.setTitle("New Notification");
                    notif.setMessage("You have an unread update on your " + luckyAcct.getPlatform() + " account.");
                    break;
            }

            dbHelper.addNotification(notif);
            dataChanged = true;
        }

        return dataChanged;
    }
}
