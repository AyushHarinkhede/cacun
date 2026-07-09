package com.example.cacun.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.models.Album;

import java.util.List;
import java.util.Locale;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private final List<Album> albums;
    private final OnAlbumClickListener listener;

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    public AlbumAdapter(List<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums;
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
        Album album = albums.get(position);
        holder.tvTitle.setText(album.getName());
        holder.tvSubtitle.setText(String.format(Locale.getDefault(), "%d Songs", album.getTrackCount()));
        
        holder.ivArt.setImageResource(R.drawable.cacun);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlbumClick(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
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
