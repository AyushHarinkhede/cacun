package com.example.cacun.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cacun.models.SocialAccount;
import com.example.cacun.models.SocialNotification;
import com.example.cacun.models.SocialPost;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cacun_social.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_ACCOUNTS = "accounts";
    private static final String TABLE_POSTS = "posts";
    private static final String TABLE_NOTIFICATIONS = "notifications";

    // Common columns
    private static final String KEY_ID = "id";

    // ACCOUNTS Table - Columns
    private static final String KEY_PLATFORM = "platform";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_FOLLOWERS = "followers";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_SCREEN_TIME = "screen_time";
    private static final String KEY_IS_ONLINE = "is_online";
    private static final String KEY_LAST_UPDATED = "last_updated";

    // POSTS Table - Columns
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_LIKES = "likes";
    private static final String KEY_COMMENTS = "comments";
    private static final String KEY_SHARES = "shares";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_MEDIA_URL = "media_url";

    // NOTIFICATIONS Table - Columns
    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_IS_READ = "is_read";

    // Table Create Statements
    private static final String CREATE_TABLE_ACCOUNTS = "CREATE TABLE " + TABLE_ACCOUNTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PLATFORM + " TEXT,"
            + KEY_USERNAME + " TEXT,"
            + KEY_API_KEY + " TEXT,"
            + KEY_FOLLOWERS + " INTEGER DEFAULT 0,"
            + KEY_STREAK + " INTEGER DEFAULT 0,"
            + KEY_SCREEN_TIME + " INTEGER DEFAULT 0,"
            + KEY_IS_ONLINE + " INTEGER DEFAULT 0,"
            + KEY_LAST_UPDATED + " INTEGER" + ")";

    private static final String CREATE_TABLE_POSTS = "CREATE TABLE " + TABLE_POSTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ACCOUNT_ID + " INTEGER,"
            + KEY_CONTENT + " TEXT,"
            + KEY_LIKES + " INTEGER DEFAULT 0,"
            + KEY_COMMENTS + " INTEGER DEFAULT 0,"
            + KEY_SHARES + " INTEGER DEFAULT 0,"
            + KEY_TIMESTAMP + " INTEGER,"
            + KEY_MEDIA_URL + " TEXT,"
            + "FOREIGN KEY(" + KEY_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + "(" + KEY_ID + ") ON DELETE CASCADE" + ")";

    private static final String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ACCOUNT_ID + " INTEGER,"
            + KEY_TITLE + " TEXT,"
            + KEY_MESSAGE + " TEXT,"
            + KEY_TIMESTAMP + " INTEGER,"
            + KEY_IS_READ + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + KEY_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + "(" + KEY_ID + ") ON DELETE CASCADE" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ACCOUNTS);
        db.execSQL(CREATE_TABLE_POSTS);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // --- ACCOUNTS Methods ---

    public long addAccount(SocialAccount account) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PLATFORM, account.getPlatform());
        values.put(KEY_USERNAME, account.getUsername());
        values.put(KEY_API_KEY, account.getApiKey());
        values.put(KEY_FOLLOWERS, account.getFollowers());
        values.put(KEY_STREAK, account.getStreak());
        values.put(KEY_SCREEN_TIME, account.getScreenTime());
        values.put(KEY_IS_ONLINE, account.isOnline() ? 1 : 0);
        values.put(KEY_LAST_UPDATED, System.currentTimeMillis());

        return db.insert(TABLE_ACCOUNTS, null, values);
    }

    public List<SocialAccount> getAllAccounts() {
        List<SocialAccount> accounts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ACCOUNTS + " ORDER BY " + KEY_PLATFORM + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                SocialAccount acct = new SocialAccount();
                acct.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                acct.setPlatform(c.getString(c.getColumnIndexOrThrow(KEY_PLATFORM)));
                acct.setUsername(c.getString(c.getColumnIndexOrThrow(KEY_USERNAME)));
                acct.setApiKey(c.getString(c.getColumnIndexOrThrow(KEY_API_KEY)));
                acct.setFollowers(c.getInt(c.getColumnIndexOrThrow(KEY_FOLLOWERS)));
                acct.setStreak(c.getInt(c.getColumnIndexOrThrow(KEY_STREAK)));
                acct.setScreenTime(c.getInt(c.getColumnIndexOrThrow(KEY_SCREEN_TIME)));
                acct.setOnline(c.getInt(c.getColumnIndexOrThrow(KEY_IS_ONLINE)) == 1);
                acct.setLastUpdated(c.getLong(c.getColumnIndexOrThrow(KEY_LAST_UPDATED)));
                accounts.add(acct);
            } while (c.moveToNext());
        }
        c.close();
        return accounts;
    }

    public SocialAccount getAccount(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + KEY_ID + " = " + id;
        Cursor c = db.rawQuery(selectQuery, null);
        SocialAccount acct = null;

        if (c.moveToFirst()) {
            acct = new SocialAccount();
            acct.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
            acct.setPlatform(c.getString(c.getColumnIndexOrThrow(KEY_PLATFORM)));
            acct.setUsername(c.getString(c.getColumnIndexOrThrow(KEY_USERNAME)));
            acct.setApiKey(c.getString(c.getColumnIndexOrThrow(KEY_API_KEY)));
            acct.setFollowers(c.getInt(c.getColumnIndexOrThrow(KEY_FOLLOWERS)));
            acct.setStreak(c.getInt(c.getColumnIndexOrThrow(KEY_STREAK)));
            acct.setScreenTime(c.getInt(c.getColumnIndexOrThrow(KEY_SCREEN_TIME)));
            acct.setOnline(c.getInt(c.getColumnIndexOrThrow(KEY_IS_ONLINE)) == 1);
            acct.setLastUpdated(c.getLong(c.getColumnIndexOrThrow(KEY_LAST_UPDATED)));
        }
        c.close();
        return acct;
    }

    public void updateAccountMetrics(int id, int followers, int streak, int screenTime, boolean isOnline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FOLLOWERS, followers);
        values.put(KEY_STREAK, streak);
        values.put(KEY_SCREEN_TIME, screenTime);
        values.put(KEY_IS_ONLINE, isOnline ? 1 : 0);
        values.put(KEY_LAST_UPDATED, System.currentTimeMillis());

        db.update(TABLE_ACCOUNTS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteAccount(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACCOUNTS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- POSTS Methods ---

    public long addPost(SocialPost post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ACCOUNT_ID, post.getAccountId());
        values.put(KEY_CONTENT, post.getContent());
        values.put(KEY_LIKES, post.getLikes());
        values.put(KEY_COMMENTS, post.getComments());
        values.put(KEY_SHARES, post.getShares());
        values.put(KEY_TIMESTAMP, post.getTimestamp());
        values.put(KEY_MEDIA_URL, post.getMediaUrl());

        return db.insert(TABLE_POSTS, null, values);
    }

    public void updatePostMetrics(int id, int likes, int comments, int shares) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LIKES, likes);
        values.put(KEY_COMMENTS, comments);
        values.put(KEY_SHARES, shares);

        db.update(TABLE_POSTS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<SocialPost> getAllFeedPosts() {
        List<SocialPost> posts = new ArrayList<>();
        String selectQuery = "SELECT p.*, a." + KEY_PLATFORM + ", a." + KEY_USERNAME + " FROM " + TABLE_POSTS + " p "
                + "JOIN " + TABLE_ACCOUNTS + " a ON p." + KEY_ACCOUNT_ID + " = a." + KEY_ID + " "
                + "ORDER BY p." + KEY_TIMESTAMP + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                SocialPost post = new SocialPost();
                post.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                post.setAccountId(c.getInt(c.getColumnIndexOrThrow(KEY_ACCOUNT_ID)));
                post.setPlatform(c.getString(c.getColumnIndexOrThrow(KEY_PLATFORM)));
                post.setUsername(c.getString(c.getColumnIndexOrThrow(KEY_USERNAME)));
                post.setContent(c.getString(c.getColumnIndexOrThrow(KEY_CONTENT)));
                post.setLikes(c.getInt(c.getColumnIndexOrThrow(KEY_LIKES)));
                post.setComments(c.getInt(c.getColumnIndexOrThrow(KEY_COMMENTS)));
                post.setShares(c.getInt(c.getColumnIndexOrThrow(KEY_SHARES)));
                post.setTimestamp(c.getLong(c.getColumnIndexOrThrow(KEY_TIMESTAMP)));
                post.setMediaUrl(c.getString(c.getColumnIndexOrThrow(KEY_MEDIA_URL)));
                posts.add(post);
            } while (c.moveToNext());
        }
        c.close();
        return posts;
    }

    public List<SocialPost> getAccountPosts(int accountId) {
        List<SocialPost> posts = new ArrayList<>();
        String selectQuery = "SELECT p.*, a." + KEY_PLATFORM + ", a." + KEY_USERNAME + " FROM " + TABLE_POSTS + " p "
                + "JOIN " + TABLE_ACCOUNTS + " a ON p." + KEY_ACCOUNT_ID + " = a." + KEY_ID + " "
                + "WHERE p." + KEY_ACCOUNT_ID + " = " + accountId + " "
                + "ORDER BY p." + KEY_TIMESTAMP + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                SocialPost post = new SocialPost();
                post.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                post.setAccountId(c.getInt(c.getColumnIndexOrThrow(KEY_ACCOUNT_ID)));
                post.setPlatform(c.getString(c.getColumnIndexOrThrow(KEY_PLATFORM)));
                post.setUsername(c.getString(c.getColumnIndexOrThrow(KEY_USERNAME)));
                post.setContent(c.getString(c.getColumnIndexOrThrow(KEY_CONTENT)));
                post.setLikes(c.getInt(c.getColumnIndexOrThrow(KEY_LIKES)));
                post.setComments(c.getInt(c.getColumnIndexOrThrow(KEY_COMMENTS)));
                post.setShares(c.getInt(c.getColumnIndexOrThrow(KEY_SHARES)));
                post.setTimestamp(c.getLong(c.getColumnIndexOrThrow(KEY_TIMESTAMP)));
                post.setMediaUrl(c.getString(c.getColumnIndexOrThrow(KEY_MEDIA_URL)));
                posts.add(post);
            } while (c.moveToNext());
        }
        c.close();
        return posts;
    }

    // --- NOTIFICATIONS Methods ---

    public long addNotification(SocialNotification notif) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ACCOUNT_ID, notif.getAccountId());
        values.put(KEY_TITLE, notif.getTitle());
        values.put(KEY_MESSAGE, notif.getMessage());
        values.put(KEY_TIMESTAMP, notif.getTimestamp());
        values.put(KEY_IS_READ, notif.isRead() ? 1 : 0);

        return db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    public List<SocialNotification> getAllNotifications() {
        List<SocialNotification> notifs = new ArrayList<>();
        String selectQuery = "SELECT n.*, a." + KEY_PLATFORM + ", a." + KEY_USERNAME + " FROM " + TABLE_NOTIFICATIONS + " n "
                + "JOIN " + TABLE_ACCOUNTS + " a ON n." + KEY_ACCOUNT_ID + " = a." + KEY_ID + " "
                + "ORDER BY n." + KEY_TIMESTAMP + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                SocialNotification notif = new SocialNotification();
                notif.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                notif.setAccountId(c.getInt(c.getColumnIndexOrThrow(KEY_ACCOUNT_ID)));
                notif.setPlatform(c.getString(c.getColumnIndexOrThrow(KEY_PLATFORM)));
                notif.setUsername(c.getString(c.getColumnIndexOrThrow(KEY_USERNAME)));
                notif.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
                notif.setMessage(c.getString(c.getColumnIndexOrThrow(KEY_MESSAGE)));
                notif.setTimestamp(c.getLong(c.getColumnIndexOrThrow(KEY_TIMESTAMP)));
                notif.setRead(c.getInt(c.getColumnIndexOrThrow(KEY_IS_READ)) == 1);
                notifs.add(notif);
            } while (c.moveToNext());
        }
        c.close();
        return notifs;
    }

    public int getUnreadNotificationsCount() {
        String countQuery = "SELECT * FROM " + TABLE_NOTIFICATIONS + " WHERE " + KEY_IS_READ + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void markAllNotificationsAsRead() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_READ, 1);
        db.update(TABLE_NOTIFICATIONS, values, null, null);
    }
}
