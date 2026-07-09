package com.example.cacun.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.database.DatabaseHelper;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final List<DatabaseHelper.PlaylistInfo> playlists;
    private final OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(DatabaseHelper.PlaylistInfo playlist);
        void onPlaylistLongClick(DatabaseHelper.PlaylistInfo playlist);
    }

    public PlaylistAdapter(List<DatabaseHelper.PlaylistInfo> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseHelper.PlaylistInfo playlist = playlists.get(position);
        holder.tvTitle.setText(playlist.name);
        holder.tvSubtitle.setText("Local Playlist");
        holder.ivArt.setImageResource(R.drawable.cacun);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaylistClick(playlist);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onPlaylistLongClick(playlist);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivArt;
        public TextView tvTitle;
        public TextView tvSubtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArt = itemView.findViewById(R.id.ivGridArt);
            tvTitle = itemView.findViewById(R.id.tvGridTitle);
            tvSubtitle = itemView.findViewById(R.id.tvGridSubtitle);
        }
    }
}
