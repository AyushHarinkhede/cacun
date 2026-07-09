package com.example.cacun.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.example.cacun.MainActivity;
import com.example.cacun.R;
import com.example.cacun.models.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicPlaybackService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "com.example.cacun.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.cacun.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.cacun.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.cacun.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.cacun.ACTION_STOP";

    private static final String CHANNEL_ID = "cacun_music_channel";
    private static final int NOTIFICATION_ID = 101;

    private final IBinder binder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    private List<Track> playlist = new ArrayList<>();
    private int currentTrackIndex = -1;
    private boolean isPrepared = false;
    private boolean playOnPrepare = false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    // Track original indices for shuffle support
    private List<Track> originalPlaylist = new ArrayList<>();

    // Listener callbacks for bound UI updates
    private final List<PlaybackEventListener> listeners = new ArrayList<>();
    private final Handler progressHandler = new Handler(Looper.getMainLooper());

    private MediaSessionCompat mediaSession;

    // Audio FX
    private android.media.audiofx.Equalizer equalizer;
    private android.media.audiofx.BassBoost bassBoost;
    private android.media.audiofx.Virtualizer virtualizer;
    private boolean eqEnabled = false;
    private short[] bandLevels = new short[5];
    private short bassStrength = 0;
    private short virtualizerStrength = 0;
    private short activePreset = -1;

    // BroadcastReceiver for headphone unplug events
    private final BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pause();
            }
        }
    };

    public interface PlaybackEventListener {
        void onTrackChanged(Track track);
        void onPlaybackStateChanged(boolean isPlaying);
        void onPlaybackProgress(int currentMs, int durationMs);
    }

    public class LocalBinder extends Binder {
        public MusicPlaybackService getService() {
            return MusicPlaybackService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initMediaPlayer();
        createNotificationChannel();

        // Setup MediaSession for lock screen metadata routing
        mediaSession = new MediaSessionCompat(this, "CacunMusic");
        mediaSession.setActive(true);

        // Register noisy receiver (headphones unplugged)
        registerReceiver(noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        // Start progress reporting loop
        startProgressUpdater();
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }

        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_PLAY:
                    if (isPlaying()) {
                        pause();
                    } else {
                        play();
                    }
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_PREVIOUS:
                    previous();
                    break;
                case ACTION_NEXT:
                    next();
                    break;
                case ACTION_STOP:
                    stopSelf();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopProgressUpdater();
        
        try {
            unregisterReceiver(noisyReceiver);
        } catch (IllegalArgumentException e) {
            // Ignore
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (mediaSession != null) {
            mediaSession.release();
        }

        abandonAudioFocus();
    }

    // --- Media Control Interfaces ---

    public void setPlaylist(List<Track> tracks) {
        this.originalPlaylist = new ArrayList<>(tracks);
        this.playlist = new ArrayList<>(tracks);
        if (isShuffle) {
            shufflePlaylistKeepCurrent();
        }
    }

    public List<Track> getPlaylist() {
        return playlist;
    }

    public void setShuffle(boolean shuffle) {
        this.isShuffle = shuffle;
        if (shuffle) {
            shufflePlaylistKeepCurrent();
        } else {
            // Restore original playlist order
            Track currentTrack = getCurrentTrack();
            this.playlist = new ArrayList<>(originalPlaylist);
            if (currentTrack != null) {
                this.currentTrackIndex = playlist.indexOf(currentTrack);
            }
        }
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setRepeat(boolean repeat) {
        this.isRepeat = repeat;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    private void shufflePlaylistKeepCurrent() {
        Track currentTrack = getCurrentTrack();
        if (currentTrack != null) {
            playlist.remove(currentTrack);
            Collections.shuffle(playlist);
            playlist.add(0, currentTrack);
            currentTrackIndex = 0;
        } else {
            Collections.shuffle(playlist);
        }
    }

    public void playTrack(int index) {
        if (index < 0 || index >= playlist.size()) return;
        currentTrackIndex = index;
        Track track = playlist.get(index);
        isPrepared = false;
        playOnPrepare = true;

        initMediaPlayer();
        try {
            mediaPlayer.setDataSource(track.getPath());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            notifyError();
        }
    }

    public void play() {
        if (requestAudioFocus()) {
            if (isPrepared && mediaPlayer != null) {
                mediaPlayer.start();
                notifyPlaybackState(true);
                startForegroundNotification();
            } else if (currentTrackIndex != -1) {
                playTrack(currentTrackIndex);
            } else if (!playlist.isEmpty()) {
                playTrack(0);
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notifyPlaybackState(false);
            startForegroundNotification(); // Update notification with Play action
        }
    }

    public void next() {
        if (playlist.isEmpty()) return;
        int nextIndex = currentTrackIndex + 1;
        if (nextIndex >= playlist.size()) {
            nextIndex = 0; // Loop back
        }
        playTrack(nextIndex);
    }

    public void previous() {
        if (playlist.isEmpty()) return;
        int prevIndex = currentTrackIndex - 1;
        if (prevIndex < 0) {
            prevIndex = playlist.size() - 1; // Loop back
        }
        playTrack(prevIndex);
    }

    public void seekTo(int positionMs) {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(positionMs);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && isPrepared && mediaPlayer.isPlaying();
    }

    public int getDuration() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public Track getCurrentTrack() {
        if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
            return playlist.get(currentTrackIndex);
        }
        return null;
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public int getAudioSessionId() {
        if (mediaPlayer != null) {
            return mediaPlayer.getAudioSessionId();
        }
        return -1;
    }

    // --- Listeners Management ---

    public void registerListener(PlaybackEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            // Notify immediately of current state
            Track current = getCurrentTrack();
            if (current != null) {
                listener.onTrackChanged(current);
                listener.onPlaybackStateChanged(isPlaying());
            }
        }
    }

    public void unregisterListener(PlaybackEventListener listener) {
        listeners.remove(listener);
    }

    private void notifyTrackChanged(Track track) {
        new Handler(Looper.getMainLooper()).post(() -> {
            for (PlaybackEventListener l : listeners) {
                l.onTrackChanged(track);
            }
        });
    }

    private void notifyPlaybackState(boolean isPlaying) {
        new Handler(Looper.getMainLooper()).post(() -> {
            for (PlaybackEventListener l : listeners) {
                l.onPlaybackStateChanged(isPlaying);
            }
        });
    }

    private void notifyError() {
        new Handler(Looper.getMainLooper()).post(() -> {
            for (PlaybackEventListener l : listeners) {
                l.onPlaybackStateChanged(false);
            }
        });
    }

    // --- Progress Updater ---

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying()) {
                int pos = getCurrentPosition();
                int dur = getDuration();
                for (PlaybackEventListener l : listeners) {
                    l.onPlaybackProgress(pos, dur);
                }
            }
            progressHandler.postDelayed(this, 1000);
        }
    };

    private void startProgressUpdater() {
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdater() {
        progressHandler.removeCallbacks(progressRunnable);
    }

    // --- Audio Focus Management ---

    private boolean requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();
            return audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                    == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer != null && !mediaPlayer.isPlaying() && playOnPrepare) {
                    mediaPlayer.start();
                    notifyPlaybackState(true);
                }
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(1.0f, 1.0f); // Restore full volume
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.2f, 0.2f); // Lower volume (ducking)
                }
                break;
        }
    }

    // --- MediaPlayer Listeners ---

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        setupAudioEffects(); // Setup and apply FX to new session
        notifyTrackChanged(getCurrentTrack());
        if (playOnPrepare) {
            play();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        isPrepared = false;
        if (isRepeat) {
            playTrack(currentTrackIndex); // Replay current
        } else {
            next(); // Play next
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        isPrepared = false;
        notifyError();
        initMediaPlayer();
        return false;
    }

    // --- Notification Foreground System ---

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cacun Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows controls for the playing offline song.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void startForegroundNotification() {
        Track track = getCurrentTrack();
        if (track == null) return;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        // Define Actions
        PendingIntent prevIntent = getServiceActionIntent(ACTION_PREVIOUS);
        PendingIntent playIntent = getServiceActionIntent(ACTION_PLAY);
        PendingIntent nextIntent = getServiceActionIntent(ACTION_NEXT);
        PendingIntent stopIntent = getServiceActionIntent(ACTION_STOP);

        int playPauseIcon = isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(track.getTitle())
                .setContentText(track.getArtist())
                .setSubText(track.getAlbum())
                .setContentIntent(pendingIntent)
                .setOngoing(isPlaying())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevIntent)
                .addAction(playPauseIcon, isPlaying() ? "Pause" : "Play", playIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
                .setStyle(new MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.getSessionToken()));

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private PendingIntent getServiceActionIntent(String action) {
        Intent intent = new Intent(this, MusicPlaybackService.class);
        intent.setAction(action);
        return PendingIntent.getService(
                this, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );
    }

    // --- Audio FX Interfaces for EqualizerActivity ---

    public void setupAudioEffects() {
        int sessionId = getAudioSessionId();
        if (sessionId == -1) return;

        try {
            releaseAudioEffects();

            equalizer = new android.media.audiofx.Equalizer(0, sessionId);
            bassBoost = new android.media.audiofx.BassBoost(0, sessionId);
            virtualizer = new android.media.audiofx.Virtualizer(0, sessionId);

            applyEffectsSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseAudioEffects() {
        if (equalizer != null) {
            equalizer.release();
            equalizer = null;
        }
        if (bassBoost != null) {
            bassBoost.release();
            bassBoost = null;
        }
        if (virtualizer != null) {
            virtualizer.release();
            virtualizer = null;
        }
    }

    private void applyEffectsSettings() {
        try {
            if (equalizer != null) {
                equalizer.setEnabled(eqEnabled);
                if (activePreset != -1 && activePreset < equalizer.getNumberOfPresets()) {
                    equalizer.usePreset(activePreset);
                } else {
                    for (short i = 0; i < 5; i++) {
                        if (i < equalizer.getNumberOfBands()) {
                            equalizer.setBandLevel(i, bandLevels[i]);
                        }
                    }
                }
            }
            if (bassBoost != null) {
                bassBoost.setEnabled(eqEnabled);
                if (bassBoost.getStrengthSupported()) {
                    bassBoost.setStrength(bassStrength);
                }
            }
            if (virtualizer != null) {
                virtualizer.setEnabled(eqEnabled);
                virtualizer.setStrength(virtualizerStrength);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isEqEnabled() { return eqEnabled; }
    public void setEqEnabled(boolean enabled) {
        this.eqEnabled = enabled;
        applyEffectsSettings();
    }

    public android.media.audiofx.Equalizer getEqualizer() { return equalizer; }
    public android.media.audiofx.BassBoost getBassBoost() { return bassBoost; }
    public android.media.audiofx.Virtualizer getVirtualizer() { return virtualizer; }

    public short getBassStrength() { return bassStrength; }
    public void setBassStrength(short strength) {
        this.bassStrength = strength;
        if (bassBoost != null && bassBoost.getStrengthSupported()) {
            bassBoost.setStrength(strength);
        }
    }

    public short getVirtualizerStrength() { return virtualizerStrength; }
    public void setVirtualizerStrength(short strength) {
        this.virtualizerStrength = strength;
        if (virtualizer != null) {
            virtualizer.setStrength(strength);
        }
    }

    public short getActivePreset() { return activePreset; }
    public void setActivePreset(short preset) {
        this.activePreset = preset;
        if (equalizer != null && preset >= 0 && preset < equalizer.getNumberOfPresets()) {
            equalizer.usePreset(preset);
            for (short i = 0; i < 5; i++) {
                if (i < equalizer.getNumberOfBands()) {
                    bandLevels[i] = equalizer.getBandLevel(i);
                }
            }
        }
    }

    public short[] getBandLevels() { return bandLevels; }
    public void setBandLevel(short band, short level) {
        if (band >= 0 && band < 5) {
            bandLevels[band] = level;
            activePreset = -1; // Custom
            if (equalizer != null && band < equalizer.getNumberOfBands()) {
                equalizer.setBandLevel(band, level);
            }
        }
    }
}
