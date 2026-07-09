package com.example.cacun.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cacun.models.Track;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cacun_music.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_PLAYLISTS = "playlists";
    private static final String TABLE_PLAYLIST_TRACKS = "playlist_tracks";
    private static final String TABLE_LYRICS = "lyrics_cache";

    // Common columns
    private static final String KEY_ID = "id";

    // PLAYLISTS Columns
    private static final String KEY_PLAYLIST_NAME = "name";
    private static final String KEY_CREATED_AT = "created_at";

    // PLAYLIST_TRACKS Columns
    private static final String KEY_PLAYLIST_ID = "playlist_id";
    private static final String KEY_MEDIA_STORE_ID = "media_store_id";
    private static final String KEY_TRACK_TITLE = "title";
    private static final String KEY_TRACK_ARTIST = "artist";
    private static final String KEY_TRACK_PATH = "path";
    private static final String KEY_TRACK_DURATION = "duration";

    // LYRICS Columns
    private static final String KEY_LYRICS_TITLE = "title";
    private static final String KEY_LYRICS_ARTIST = "artist";
    private static final String KEY_LYRICS_TEXT = "lyrics_text";

    // Create statements
    private static final String CREATE_TABLE_PLAYLISTS = "CREATE TABLE " + TABLE_PLAYLISTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PLAYLIST_NAME + " TEXT UNIQUE,"
            + KEY_CREATED_AT + " INTEGER" + ")";

    private static final String CREATE_TABLE_PLAYLIST_TRACKS = "CREATE TABLE " + TABLE_PLAYLIST_TRACKS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PLAYLIST_ID + " INTEGER,"
            + KEY_MEDIA_STORE_ID + " INTEGER,"
            + KEY_TRACK_TITLE + " TEXT,"
            + KEY_TRACK_ARTIST + " TEXT,"
            + KEY_TRACK_PATH + " TEXT,"
            + KEY_TRACK_DURATION + " INTEGER,"
            + "FOREIGN KEY(" + KEY_PLAYLIST_ID + ") REFERENCES " + TABLE_PLAYLISTS + "(" + KEY_ID + ") ON DELETE CASCADE" + ")";

    private static final String CREATE_TABLE_LYRICS = "CREATE TABLE " + TABLE_LYRICS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_LYRICS_TITLE + " TEXT,"
            + KEY_LYRICS_ARTIST + " TEXT,"
            + KEY_LYRICS_TEXT + " TEXT,"
            + "UNIQUE(" + KEY_LYRICS_TITLE + ", " + KEY_LYRICS_ARTIST + ")" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PLAYLISTS);
        db.execSQL(CREATE_TABLE_PLAYLIST_TRACKS);
        db.execSQL(CREATE_TABLE_LYRICS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_TRACKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LYRICS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // --- PLAYLISTS Methods ---

    public long createPlaylist(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PLAYLIST_NAME, name);
        values.put(KEY_CREATED_AT, System.currentTimeMillis());
        return db.insert(TABLE_PLAYLISTS, null, values);
    }

    public List<PlaylistInfo> getAllPlaylists() {
        List<PlaylistInfo> playlists = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PLAYLISTS + " ORDER BY " + KEY_PLAYLIST_NAME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                PlaylistInfo p = new PlaylistInfo();
                p.id = c.getInt(c.getColumnIndexOrThrow(KEY_ID));
                p.name = c.getString(c.getColumnIndexOrThrow(KEY_PLAYLIST_NAME));
                p.createdAt = c.getLong(c.getColumnIndexOrThrow(KEY_CREATED_AT));
                playlists.add(p);
            } while (c.moveToNext());
        }
        c.close();
        return playlists;
    }

    public void deletePlaylist(int playlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLISTS, KEY_ID + " = ?", new String[]{String.valueOf(playlistId)});
    }

    // --- PLAYLIST_TRACKS Methods ---

    public long addTrackToPlaylist(int playlistId, Track track) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Check for duplicates
        String checkQuery = "SELECT * FROM " + TABLE_PLAYLIST_TRACKS + " WHERE " 
                + KEY_PLAYLIST_ID + " = " + playlistId + " AND " + KEY_MEDIA_STORE_ID + " = " + track.getId();
        Cursor cursor = db.rawQuery(checkQuery, null);
        int count = cursor.getCount();
        cursor.close();
        if (count > 0) return -1; // Already in playlist

        ContentValues values = new ContentValues();
        values.put(KEY_PLAYLIST_ID, playlistId);
        values.put(KEY_MEDIA_STORE_ID, track.getId());
        values.put(KEY_TRACK_TITLE, track.getTitle());
        values.put(KEY_TRACK_ARTIST, track.getArtist());
        values.put(KEY_TRACK_PATH, track.getPath());
        values.put(KEY_TRACK_DURATION, track.getDuration());

        return db.insert(TABLE_PLAYLIST_TRACKS, null, values);
    }

    public void removeTrackFromPlaylist(int playlistId, long mediaStoreId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLIST_TRACKS, 
                KEY_PLAYLIST_ID + " = ? AND " + KEY_MEDIA_STORE_ID + " = ?", 
                new String[]{String.valueOf(playlistId), String.valueOf(mediaStoreId)});
    }

    public List<Track> getPlaylistTracks(int playlistId) {
        List<Track> tracks = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PLAYLIST_TRACKS + " WHERE " + KEY_PLAYLIST_ID + " = " + playlistId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Track t = new Track();
                t.setId(c.getLong(c.getColumnIndexOrThrow(KEY_MEDIA_STORE_ID)));
                t.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_TRACK_TITLE)));
                t.setArtist(c.getString(c.getColumnIndexOrThrow(KEY_TRACK_ARTIST)));
                t.setPath(c.getString(c.getColumnIndexOrThrow(KEY_TRACK_PATH)));
                t.setDuration(c.getInt(c.getColumnIndexOrThrow(KEY_TRACK_DURATION)));
                t.setAlbum("Playlist Track");
                tracks.add(t);
            } while (c.moveToNext());
        }
        c.close();
        return tracks;
    }

    // --- LYRICS Methods ---

    public void cacheLyrics(String title, String artist, String lyricsText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LYRICS_TITLE, title.toLowerCase().trim());
        values.put(KEY_LYRICS_ARTIST, artist.toLowerCase().trim());
        values.put(KEY_LYRICS_TEXT, lyricsText);

        db.insertWithOnConflict(TABLE_LYRICS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getCachedLyrics(String title, String artist) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + KEY_LYRICS_TEXT + " FROM " + TABLE_LYRICS + " WHERE "
                + KEY_LYRICS_TITLE + " = ? AND " + KEY_LYRICS_ARTIST + " = ?";
        Cursor c = db.rawQuery(selectQuery, new String[]{title.toLowerCase().trim(), artist.toLowerCase().trim()});
        String lyrics = null;

        if (c.moveToFirst()) {
            lyrics = c.getString(c.getColumnIndexOrThrow(KEY_LYRICS_TEXT));
        }
        c.close();
        return lyrics;
    }

    // Simple Helper class for playlist metadata
    public static class PlaylistInfo {
        public int id;
        public String name;
        public long createdAt;
    }
}
