package com.example.cacun.ui.playlists;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cacun.MainActivity;
import com.example.cacun.R;
import com.example.cacun.adapters.PlaylistAdapter;
import com.example.cacun.database.DatabaseHelper;
import com.example.cacun.models.Track;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends Fragment implements PlaylistAdapter.OnPlaylistClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private FloatingActionButton fabAdd;
    private PlaylistAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<DatabaseHelper.PlaylistInfo> playlistsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshList);
        recyclerView = view.findViewById(R.id.recyclerViewMusic);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        fabAdd = view.findViewById(R.id.fabCreatePlaylist);

        dbHelper = new DatabaseHelper(requireContext());

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(v -> showCreatePlaylistDialog());

        swipeRefreshLayout.setOnRefreshListener(this::loadPlaylists);

        loadPlaylists();

        return view;
    }

    private void loadPlaylists() {
        playlistsList = dbHelper.getAllPlaylists();

        if (playlistsList.isEmpty()) {
            tvEmptyState.setText("No playlists created.\nTap + to create a playlist!");
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new PlaylistAdapter(playlistsList, this);
            recyclerView.setAdapter(adapter);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("New Playlist");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Playlist Name");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Playlist name cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            long res = dbHelper.createPlaylist(name);
            if (res == -1) {
                Toast.makeText(getContext(), "Playlist with this name already exists!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Created playlist: " + name, Toast.LENGTH_SHORT).show();
                loadPlaylists();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onPlaylistClick(DatabaseHelper.PlaylistInfo playlist) {
        List<Track> playlistTracks = dbHelper.getPlaylistTracks(playlist.id);

        if (playlistTracks.isEmpty()) {
            Toast.makeText(getContext(), "This playlist has no songs yet. Long-press songs in the Tracks list to add them!", Toast.LENGTH_LONG).show();
            return;
        }

        // Show tracks list in a dialog
        String[] titles = new String[playlistTracks.size()];
        for (int i = 0; i < playlistTracks.size(); i++) {
            titles[i] = (i + 1) + ". " + playlistTracks.get(i).getTitle();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(playlist.name);
        
        // Context actions on playlist items
        builder.setItems(titles, (dialog, which) -> {
            Track selectedTrack = playlistTracks.get(which);
            String[] trackOptions = {"Play Song", "Remove from Playlist", "Cancel"};
            
            AlertDialog.Builder itemActionBuilder = new AlertDialog.Builder(getContext());
            itemActionBuilder.setTitle(selectedTrack.getTitle());
            itemActionBuilder.setItems(trackOptions, (itemDialog, itemWhich) -> {
                if (itemWhich == 0) {
                    if (getActivity() instanceof MainActivity) {
                        MainActivity activity = (MainActivity) getActivity();
                        activity.playTrack(which, playlistTracks);
                    }
                } else if (itemWhich == 1) {
                    dbHelper.removeTrackFromPlaylist(playlist.id, selectedTrack.getId());
                    Toast.makeText(getContext(), "Removed from playlist!", Toast.LENGTH_SHORT).show();
                    onPlaylistClick(playlist); // Refresh dialog
                }
            });
            itemActionBuilder.show();
        });
        builder.show();
    }

    @Override
    public void onPlaylistLongClick(DatabaseHelper.PlaylistInfo playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Playlist");
        builder.setMessage("Are you sure you want to delete the playlist '" + playlist.name + "'?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            dbHelper.deletePlaylist(playlist.id);
            Toast.makeText(getContext(), "Playlist deleted", Toast.LENGTH_SHORT).show();
            loadPlaylists();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
