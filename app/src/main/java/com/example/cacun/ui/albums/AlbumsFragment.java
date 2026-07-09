package com.example.cacun.ui.albums;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cacun.MainActivity;
import com.example.cacun.R;
import com.example.cacun.adapters.AlbumAdapter;
import com.example.cacun.models.Album;
import com.example.cacun.models.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumsFragment extends Fragment implements AlbumAdapter.OnAlbumClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private AlbumAdapter adapter;
    private List<Track> allTracks = new ArrayList<>();
    private List<Album> albumList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshList);
        recyclerView = view.findViewById(R.id.recyclerViewMusic);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        
        swipeRefreshLayout.setOnRefreshListener(this::loadAlbums);

        loadAlbums();

        return view;
    }

    private void loadAlbums() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            allTracks = activity.getScannedTracks();
            
            groupTracksIntoAlbums();

            if (albumList.isEmpty()) {
                tvEmptyState.setText("No albums found.\nPull to refresh scanning.");
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter = new AlbumAdapter(albumList, this);
                recyclerView.setAdapter(adapter);
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void groupTracksIntoAlbums() {
        albumList.clear();
        Map<String, Album> albumMap = new HashMap<>();

        for (Track track : allTracks) {
            String albumName = track.getAlbum();
            if (albumMap.containsKey(albumName)) {
                albumMap.get(albumName).incrementTrackCount();
            } else {
                albumMap.put(albumName, new Album(
                        albumName,
                        track.getArtist(),
                        track.getAlbumId(),
                        1
                ));
            }
        }

        albumList.addAll(albumMap.values());
    }

    @Override
    public void onAlbumClick(Album album) {
        // Find all tracks in this album
        List<Track> albumTracks = new ArrayList<>();
        for (Track t : allTracks) {
            if (t.getAlbum().equalsIgnoreCase(album.getName())) {
                albumTracks.add(t);
            }
        }

        // Show AlertDialog with tracks list
        String[] songTitles = new String[albumTracks.size()];
        for (int i = 0; i < albumTracks.size(); i++) {
            songTitles[i] = (i + 1) + ". " + albumTracks.get(i).getTitle();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(album.getName() + " - " + album.getArtist());
        builder.setItems(songTitles, (dialog, which) -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                // Play selected song, load full album playlist into queue!
                activity.playTrack(which, albumTracks);
            }
        });
        builder.show();
    }
}
