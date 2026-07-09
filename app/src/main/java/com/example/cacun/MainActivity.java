package com.example.cacun;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cacun.models.Track;
import com.example.cacun.service.MusicPlaybackService;
import com.example.cacun.ui.albums.AlbumsFragment;
import com.example.cacun.ui.artists.ArtistsFragment;
import com.example.cacun.ui.player.PlayerActivity;
import com.example.cacun.ui.playlists.PlaylistsFragment;
import com.example.cacun.ui.tracks.TracksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicPlaybackService.PlaybackEventListener {

    private static final int PERMISSION_REQUEST_CODE = 1002;

    private FragmentManager fragmentManager;
    private Fragment activeFragment;

    private TracksFragment tracksFragment;
    private AlbumsFragment albumsFragment;
    private ArtistsFragment artistsFragment;
    private PlaylistsFragment playlistsFragment;

    // Media scan storage
    private final List<Track> scannedTracks = new ArrayList<>();

    // Playback Service binding
    private MusicPlaybackService playbackService;
    private boolean isBound = false;

    // Mini Player Views
    private View miniPlayerBar;
    private ImageView ivMiniArt;
    private TextView tvMiniTitle;
    private TextView tvMiniArtist;
    private ImageView btnMiniPlayPause;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.LocalBinder binder = (MusicPlaybackService.LocalBinder) service;
            playbackService = binder.getService();
            isBound = true;
            
            // Register callback to update mini player bar
            playbackService.registerListener(MainActivity.this);
            
            updateMiniPlayerState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        // Bind Mini Player Views
        miniPlayerBar = findViewById(R.id.miniPlayerBar);
        ivMiniArt = findViewById(R.id.ivMiniArt);
        tvMiniTitle = findViewById(R.id.tvMiniTitle);
        tvMiniArtist = findViewById(R.id.tvMiniArtist);
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause);
        ImageView btnMiniNext = findViewById(R.id.btnMiniNext);

        // Header bar customizations
        TextView statusText = findViewById(R.id.status_text);
        statusText.setText("HI-RES AUDIO");

        // Set up bottom navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tracks) {
                switchToFragment(getTracksFragment());
                return true;
            } else if (itemId == R.id.nav_albums) {
                switchToFragment(getAlbumsFragment());
                return true;
            } else if (itemId == R.id.nav_artists) {
                switchToFragment(getArtistsFragment());
                return true;
            } else if (itemId == R.id.nav_playlists) {
                switchToFragment(getPlaylistsFragment());
                return true;
            }
            return false;
        });

        // Mini player click actions
        miniPlayerBar.setOnClickListener(v -> {
            startActivity(new Intent(this, PlayerActivity.class));
        });

        btnMiniPlayPause.setOnClickListener(v -> {
            if (isBound && playbackService != null) {
                if (playbackService.isPlaying()) {
                    playbackService.pause();
                } else {
                    playbackService.play();
                }
            }
        });

        btnMiniNext.setOnClickListener(v -> {
            if (isBound && playbackService != null) {
                playbackService.next();
            }
        });

        // Check storage permissions
        if (checkStoragePermissions()) {
            scanLocalAudio();
        } else {
            requestStoragePermissions();
        }

        // Bind playback service
        Intent intent = new Intent(this, MusicPlaybackService.class);
        startService(intent); // Keep service alive independently
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void switchToFragment(Fragment targetFragment) {
        if (targetFragment == null) return;
        activeFragment = targetFragment;
        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, targetFragment)
                .commitAllowingStateLoss();
    }

    // Lazy load UI fragments
    private TracksFragment getTracksFragment() {
        if (tracksFragment == null) {
            tracksFragment = new TracksFragment();
        }
        return tracksFragment;
    }

    private AlbumsFragment getAlbumsFragment() {
        if (albumsFragment == null) {
            albumsFragment = new AlbumsFragment();
        }
        return albumsFragment;
    }

    private ArtistsFragment getArtistsFragment() {
        if (artistsFragment == null) {
            artistsFragment = new ArtistsFragment();
        }
        return artistsFragment;
    }

    private PlaylistsFragment getPlaylistsFragment() {
        if (playlistsFragment == null) {
            playlistsFragment = new PlaylistsFragment();
        }
        return playlistsFragment;
    }

    // --- Permissions checks ---

    private boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                    PERMISSION_REQUEST_CODE
            );
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanLocalAudio();
            } else {
                Toast.makeText(this, "Storage permission is required to list offline songs. Showing sample lists.", Toast.LENGTH_LONG).show();
                loadFallbackSampleData();
            }
        }
    }

    // --- Media Scanner ---

    private void scanLocalAudio() {
        scannedTracks.clear();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        try (Cursor cursor = getContentResolver().query(uri, projection, selection, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                    // Filter out short notification sounds (less than 5 seconds)
                    if (duration >= 5000) {
                        scannedTracks.add(new Track(id, title, artist, album, duration, path, albumId));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If no music exists, fill with sample tracks for testing/demo
        if (scannedTracks.isEmpty()) {
            loadFallbackSampleData();
        }

        // Launch default fragment
        switchToFragment(getTracksFragment());
    }

    private void loadFallbackSampleData() {
        scannedTracks.add(new Track(1, "Dynamic Harmony", "Aether Pixel", "System Soundscape", 212000, "sample_path_1", 101));
        scannedTracks.add(new Track(2, "Glass Waves", "Retro Fluid", "Translucent Dreams", 185000, "sample_path_2", 102));
        scannedTracks.add(new Track(3, "Subtle Ambient", "Material Flow", "System Soundscape", 340000, "sample_path_3", 101));
        scannedTracks.add(new Track(4, "High Res Beats", "Flac Core", "Lossless Beats", 154000, "sample_path_4", 103));
        scannedTracks.add(new Track(5, "Violet Drift", "Aether Pixel", "Translucent Dreams", 225000, "sample_path_5", 102));
    }

    public List<Track> getScannedTracks() {
        return scannedTracks;
    }

    // Public controller for child fragments to trigger play queue
    public void playTrack(int index, List<Track> playlist) {
        if (isBound && playbackService != null) {
            playbackService.setPlaylist(playlist);
            playbackService.playTrack(index);
            
            // Launch full Now Playing Activity
            Intent playerIntent = new Intent(this, PlayerActivity.class);
            startActivity(playerIntent);
        }
    }

    private void updateMiniPlayerState() {
        if (!isBound || playbackService == null) return;
        Track current = playbackService.getCurrentTrack();
        if (current != null) {
            miniPlayerBar.setVisibility(View.VISIBLE);
            tvMiniTitle.setText(current.getTitle());
            tvMiniArtist.setText(current.getArtist());
            ivMiniArt.setImageResource(R.drawable.cacun);
            onPlaybackStateChanged(playbackService.isPlaying());
        } else {
            miniPlayerBar.setVisibility(View.GONE);
        }
    }

    // --- PlaybackEventListener Callbacks ---

    @Override
    public void onTrackChanged(Track track) {
        updateMiniPlayerState();
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        if (isPlaying) {
            btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    @Override
    public void onPlaybackProgress(int currentMs, int durationMs) {
        // Mini player doesn't draw progress bar, ignore progress updates
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.view.WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.preferredRefreshRate = 120f;
            getWindow().setAttributes(layoutParams);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.view.WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.preferredRefreshRate = 0f;
            getWindow().setAttributes(layoutParams);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            if (playbackService != null) {
                playbackService.unregisterListener(this);
            }
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
