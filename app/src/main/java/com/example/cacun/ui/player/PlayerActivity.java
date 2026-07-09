package com.example.cacun.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cacun.R;
import com.example.cacun.database.DatabaseHelper;
import com.example.cacun.models.Track;
import com.example.cacun.service.MusicPlaybackService;
import com.example.cacun.ui.eq.EqualizerActivity;

import java.util.Locale;

public class PlayerActivity extends AppCompatActivity implements MusicPlaybackService.PlaybackEventListener {

    private ImageView ivAlbumArt;
    private TextView tvTitle;
    private TextView tvArtist;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    
    private ImageView btnShuffle;
    private ImageView btnRepeat;
    private ImageView ivPlayPauseIcon;
    private View btnPlayPause;
    
    // Lyrics Views
    private View lyricsHeader;
    private View scrollLyrics;
    private TextView tvLyrics;
    private ImageView ivLyricsArrow;
    private boolean isLyricsExpanded = false;

    private MusicPlaybackService playbackService;
    private boolean isBound = false;
    private DatabaseHelper dbHelper;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.LocalBinder binder = (MusicPlaybackService.LocalBinder) service;
            playbackService = binder.getService();
            isBound = true;
            
            // Register callback listener
            playbackService.registerListener(PlayerActivity.this);
            
            updatePlaybackUi();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        dbHelper = new DatabaseHelper(this);

        // Bind Views
        ivAlbumArt = findViewById(R.id.ivPlayerAlbumArt);
        tvTitle = findViewById(R.id.tvPlayerTitle);
        tvArtist = findViewById(R.id.tvPlayerArtist);
        seekBar = findViewById(R.id.seekBarPlayback);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        ivPlayPauseIcon = findViewById(R.id.ivPlayPauseIcon);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        
        lyricsHeader = findViewById(R.id.lyricsHeader);
        scrollLyrics = findViewById(R.id.scrollLyrics);
        tvLyrics = findViewById(R.id.tvLyrics);
        ivLyricsArrow = findViewById(R.id.ivLyricsArrow);

        // Bind Button Handlers
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnEqualizer).setOnClickListener(v -> {
            startActivity(new Intent(this, EqualizerActivity.class));
        });

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            if (isBound) playbackService.next();
        });

        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            if (isBound) playbackService.previous();
        });

        btnPlayPause.setOnClickListener(v -> {
            if (isBound) {
                if (playbackService.isPlaying()) {
                    playbackService.pause();
                } else {
                    playbackService.play();
                }
            }
        });

        btnShuffle.setOnClickListener(v -> {
            if (isBound) {
                boolean target = !playbackService.isShuffle();
                playbackService.setShuffle(target);
                updateShuffleRepeatToggles();
            }
        });

        btnRepeat.setOnClickListener(v -> {
            if (isBound) {
                boolean target = !playbackService.isRepeat();
                playbackService.setRepeat(target);
                updateShuffleRepeatToggles();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    playbackService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Setup expandable lyrics panel
        lyricsHeader.setOnClickListener(v -> toggleLyricsExpansion());

        // Bind Service
        Intent intent = new Intent(this, MusicPlaybackService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void updatePlaybackUi() {
        if (!isBound || playbackService == null) return;

        Track track = playbackService.getCurrentTrack();
        if (track != null) {
            onTrackChanged(track);
            onPlaybackStateChanged(playbackService.isPlaying());
            onPlaybackProgress(playbackService.getCurrentPosition(), playbackService.getDuration());
        }
        updateShuffleRepeatToggles();
    }

    private void updateShuffleRepeatToggles() {
        if (!isBound || playbackService == null) return;

        if (playbackService.isShuffle()) {
            btnShuffle.setColorFilter(Color.parseColor("#A78BFA")); // Active purple
        } else {
            btnShuffle.setColorFilter(Color.parseColor("#C4B5FD")); // Muted
        }

        if (playbackService.isRepeat()) {
            btnRepeat.setColorFilter(Color.parseColor("#A78BFA")); // Active purple
        } else {
            btnRepeat.setColorFilter(Color.parseColor("#C4B5FD")); // Muted
        }
    }

    private void toggleLyricsExpansion() {
        isLyricsExpanded = !isLyricsExpanded;
        if (isLyricsExpanded) {
            scrollLyrics.setVisibility(View.VISIBLE);
            ivLyricsArrow.setRotation(180f);
            loadLyricsForCurrentTrack();
        } else {
            scrollLyrics.setVisibility(View.GONE);
            ivLyricsArrow.setRotation(0f);
        }
    }

    private void loadLyricsForCurrentTrack() {
        if (!isBound || playbackService == null) return;
        Track current = playbackService.getCurrentTrack();
        if (current == null) return;

        String lyrics = dbHelper.getCachedLyrics(current.getTitle(), current.getArtist());
        if (lyrics != null && !lyrics.trim().isEmpty()) {
            tvLyrics.setText(lyrics);
        } else {
            tvLyrics.setText("No cached lyrics found for this song.\n\nLong-press this song in the Tracks list to add or edit lyrics!");
        }
    }

    // --- PlaybackEventListener Callbacks ---

    @Override
    public void onTrackChanged(Track track) {
        tvTitle.setText(track.getTitle());
        tvArtist.setText(track.getArtist());
        seekBar.setMax(track.getDuration());
        tvTotalTime.setText(formatDuration(track.getDuration()));
        ivAlbumArt.setImageResource(R.drawable.cacun);

        if (isLyricsExpanded) {
            loadLyricsForCurrentTrack();
        }
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        if (isPlaying) {
            ivPlayPauseIcon.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            ivPlayPauseIcon.setImageResource(android.R.drawable.ic_media_play);
        }
        updateAlbumArtAnimation(isPlaying);
    }

    @Override
    public void onPlaybackProgress(int currentMs, int durationMs) {
        seekBar.setProgress(currentMs);
        tvCurrentTime.setText(formatDuration(currentMs));
    }

    private String formatDuration(int durationMs) {
        int seconds = (durationMs / 1000) % 60;
        int minutes = (durationMs / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private boolean isAnimating = false;

    private void updateAlbumArtAnimation(boolean isPlaying) {
        if (isPlaying) {
            ivAlbumArt.animate().scaleX(1.05f).scaleY(1.05f).setDuration(400).start();
            if (!isAnimating) {
                isAnimating = true;
                startAlbumArtRotation();
            }
        } else {
            ivAlbumArt.animate().scaleX(1.0f).scaleY(1.0f).setDuration(400).start();
            isAnimating = false;
            ivAlbumArt.animate().cancel();
        }
    }

    private void startAlbumArtRotation() {
        if (!isAnimating) return;
        ivAlbumArt.animate()
                .rotationBy(360f)
                .setDuration(25000) // 25 seconds per full rotation
                .setInterpolator(new android.view.animation.LinearInterpolator())
                .withEndAction(this::startAlbumArtRotation)
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.view.WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.preferredRefreshRate = 120f;
            getWindow().setAttributes(layoutParams);
        }
        if (isBound && playbackService != null) {
            updateAlbumArtAnimation(playbackService.isPlaying());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.view.WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.preferredRefreshRate = 0f;
            getWindow().setAttributes(layoutParams);
        }
        isAnimating = false;
        ivAlbumArt.animate().cancel();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAnimating = false;
        ivAlbumArt.animate().cancel();
        if (isBound) {
            if (playbackService != null) {
                playbackService.unregisterListener(this);
            }
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
