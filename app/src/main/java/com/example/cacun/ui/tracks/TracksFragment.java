package com.example.cacun.ui.tracks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cacun.MainActivity;
import com.example.cacun.R;
import com.example.cacun.adapters.TrackAdapter;
import com.example.cacun.database.DatabaseHelper;
import com.example.cacun.models.Track;

import java.util.ArrayList;
import java.util.List;

public class TracksFragment extends Fragment implements TrackAdapter.OnTrackClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private TrackAdapter adapter;
    private List<Track> tracksList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshList);
        recyclerView = view.findViewById(R.id.recyclerViewMusic);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        dbHelper = new DatabaseHelper(requireContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        swipeRefreshLayout.setOnRefreshListener(this::loadTracks);

        loadTracks();

        return view;
    }

    private void loadTracks() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            tracksList = activity.getScannedTracks();
            
            if (tracksList.isEmpty()) {
                tvEmptyState.setText("No local music found.\nPull to refresh scanning.");
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter = new TrackAdapter(tracksList, this);
                recyclerView.setAdapter(adapter);
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onTrackClick(int position) {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.playTrack(position, tracksList);
        }
    }

    @Override
    public void onTrackLongClick(int position) {
        Track track = tracksList.get(position);
        String[] options = {"Add to Playlist", "Add / Edit Lyrics", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(track.getTitle());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showAddToPlaylistDialog(track);
            } else if (which == 1) {
                showLyricsEditDialog(track);
            }
        });
        builder.show();
    }

    private void showAddToPlaylistDialog(Track track) {
        List<DatabaseHelper.PlaylistInfo> playlists = dbHelper.getAllPlaylists();
        if (playlists.isEmpty()) {
            Toast.makeText(getContext(), "Please create a playlist first in the Playlists tab!", Toast.LENGTH_LONG).show();
            return;
        }

        String[] names = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            names[i] = playlists.get(i).name;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add to Playlist");
        builder.setItems(names, (dialog, which) -> {
            DatabaseHelper.PlaylistInfo selected = playlists.get(which);
            long res = dbHelper.addTrackToPlaylist(selected.id, track);
            if (res == -1) {
                Toast.makeText(getContext(), "Song already exists in this playlist!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Added to " + selected.name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void showLyricsEditDialog(Track track) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Lyrics for " + track.getTitle());

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        
        // Load existing
        String existing = dbHelper.getCachedLyrics(track.getTitle(), track.getArtist());
        if (existing != null) {
            input.setText(existing);
        } else {
            input.setHint("Type or paste lyrics here...");
        }
        
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String lyricsText = input.getText().toString();
            dbHelper.cacheLyrics(track.getTitle(), track.getArtist(), lyricsText);
            Toast.makeText(getContext(), "Lyrics saved!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
