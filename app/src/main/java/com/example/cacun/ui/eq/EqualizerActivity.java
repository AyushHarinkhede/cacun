package com.example.cacun.ui.eq;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.cacun.R;
import com.example.cacun.service.MusicPlaybackService;

import java.util.ArrayList;
import java.util.List;

public class EqualizerActivity extends AppCompatActivity {

    private SwitchCompat switchEq;
    private Spinner spinnerPresets;
    private SeekBar[] seekBars = new SeekBar[5];
    private SeekBar seekBarBass;
    private SeekBar seekBarVirtualizer;

    private MusicPlaybackService playbackService;
    private boolean isBound = false;
    private boolean isUpdatingUi = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.LocalBinder binder = (MusicPlaybackService.LocalBinder) service;
            playbackService = binder.getService();
            isBound = true;
            initializeEqualizerUi();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        // Bind Views
        switchEq = findViewById(R.id.switchEqEnable);
        spinnerPresets = findViewById(R.id.spinnerPresets);
        seekBars[0] = findViewById(R.id.seekBarBand0);
        seekBars[1] = findViewById(R.id.seekBarBand1);
        seekBars[2] = findViewById(R.id.seekBarBand2);
        seekBars[3] = findViewById(R.id.seekBarBand3);
        seekBars[4] = findViewById(R.id.seekBarBand4);
        seekBarBass = findViewById(R.id.seekBarBassBoost);
        seekBarVirtualizer = findViewById(R.id.seekBarVirtualizer);

        findViewById(R.id.btnEqBack).setOnClickListener(v -> finish());

        // Bind Service
        Intent intent = new Intent(this, MusicPlaybackService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initializeEqualizerUi() {
        if (!isBound || playbackService == null) return;

        // 1. Enable / Disable state
        boolean eqEnabled = playbackService.isEqEnabled();
        switchEq.setChecked(eqEnabled);
        setControlsEnabled(eqEnabled);

        switchEq.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playbackService.setEqEnabled(isChecked);
            setControlsEnabled(isChecked);
        });

        // Try setting up effects in service if not already active
        playbackService.setupAudioEffects();

        Equalizer eq = playbackService.getEqualizer();
        if (eq == null) {
            Toast.makeText(this, "Play some music first to enable Equalizer!", Toast.LENGTH_SHORT).show();
            // Fallback presets
            List<String> defaultPresets = new ArrayList<>();
            defaultPresets.add("Normal");
            defaultPresets.add("Pop");
            defaultPresets.add("Rock");
            defaultPresets.add("Jazz");
            defaultPresets.add("Classical");
            defaultPresets.add("Custom");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, defaultPresets);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPresets.setAdapter(adapter);
            spinnerPresets.setEnabled(false);
            setControlsEnabled(false);
            switchEq.setEnabled(false);
            return;
        }

        // 2. Presets Spinner
        List<String> presetNames = new ArrayList<>();
        short numPresets = eq.getNumberOfPresets();
        for (short i = 0; i < numPresets; i++) {
            presetNames.add(eq.getPresetName(i));
        }
        presetNames.add("Custom"); // Extra manual preset

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, presetNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPresets.setAdapter(adapter);

        short activePreset = playbackService.getActivePreset();
        if (activePreset != -1 && activePreset < numPresets) {
            spinnerPresets.setSelection(activePreset);
        } else {
            spinnerPresets.setSelection(numPresets); // Select Custom
        }

        spinnerPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingUi) return;
                if (position < numPresets) {
                    playbackService.setActivePreset((short) position);
                    updateBandSeekBarsFromService();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 3. Frequencies Band SeekBars
        for (int i = 0; i < 5; i++) {
            final int bandId = i;
            seekBars[i].setMax(30); // Represents -1500 to +1500 millibels (-15dB to +15dB)
            
            // Set initial progress
            short level = playbackService.getBandLevels()[i];
            int progress = (level / 100) + 15; // Map from [-1500, 1500] to [0, 30]
            seekBars[i].setProgress(progress);

            seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && isBound) {
                        short newLevel = (short) ((progress - 15) * 100);
                        playbackService.setBandLevel((short) bandId, newLevel);
                        
                        // Select Custom in Spinner
                        isUpdatingUi = true;
                        spinnerPresets.setSelection(numPresets);
                        isUpdatingUi = false;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        // 4. Bass Boost & Virtualizer
        seekBarBass.setMax(1000);
        seekBarBass.setProgress(playbackService.getBassStrength());
        seekBarBass.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    playbackService.setBassStrength((short) progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarVirtualizer.setMax(1000);
        seekBarVirtualizer.setProgress(playbackService.getVirtualizerStrength());
        seekBarVirtualizer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    playbackService.setVirtualizerStrength((short) progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateBandSeekBarsFromService() {
        if (!isBound || playbackService == null) return;
        isUpdatingUi = true;
        short[] levels = playbackService.getBandLevels();
        for (int i = 0; i < 5; i++) {
            int progress = (levels[i] / 100) + 15;
            seekBars[i].setProgress(progress);
        }
        isUpdatingUi = false;
    }

    private void setControlsEnabled(boolean enabled) {
        spinnerPresets.setEnabled(enabled);
        for (SeekBar sb : seekBars) {
            sb.setEnabled(enabled);
        }
        seekBarBass.setEnabled(enabled);
        seekBarVirtualizer.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
