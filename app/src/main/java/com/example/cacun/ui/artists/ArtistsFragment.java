package com.example.cacun.ui.artists;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cacun.MainActivity;
import com.example.cacun.R;
import com.example.cacun.adapters.ArtistAdapter;
import com.example.cacun.models.Artist;
import com.example.cacun.models.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtistsFragment extends Fragment implements ArtistAdapter.OnArtistClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private ArtistAdapter adapter;
    private List<Track> allTracks = new ArrayList<>();
    private List<Artist> artistList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshList);
        recyclerView = view.findViewById(R.id.recyclerViewMusic);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        swipeRefreshLayout.setOnRefreshListener(this::loadArtists);

        loadArtists();

        return view;
    }

    private void loadArtists() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            allTracks = activity.getScannedTracks();
            
            groupTracksIntoArtists();

            if (artistList.isEmpty()) {
                tvEmptyState.setText("No artists found.\nPull to refresh scanning.");
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter = new ArtistAdapter(artistList, this);
                recyclerView.setAdapter(adapter);
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void groupTracksIntoArtists() {
        artistList.clear();
        Map<String, Artist> artistMap = new HashMap<>();

        for (Track track : allTracks) {
            String artistName = track.getArtist();
            if (artistMap.containsKey(artistName)) {
                artistMap.get(artistName).incrementTrackCount();
            } else {
                artistMap.put(artistName, new Artist(artistName, 1));
            }
        }

        artistList.addAll(artistMap.values());
    }

    @Override
    public void onArtistClick(Artist artist) {
        // Find all tracks by this artist
        List<Track> artistTracks = new ArrayList<>();
        for (Track t : allTracks) {
            if (t.getArtist().equalsIgnoreCase(artist.getName())) {
                artistTracks.add(t);
            }
        }

        // Show AlertDialog with tracks list
        String[] songTitles = new String[artistTracks.size()];
        for (int i = 0; i < artistTracks.size(); i++) {
            songTitles[i] = (i + 1) + ". " + artistTracks.get(i).getTitle();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Songs by " + artist.getName());
        builder.setItems(songTitles, (dialog, which) -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                // Play selected song, load full artist playlist into queue!
                activity.playTrack(which, artistTracks);
            }
        });
        builder.show();
    }
}
