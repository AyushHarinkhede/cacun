package com.example.cacun.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.models.Artist;

import java.util.List;
import java.util.Locale;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private final List<Artist> artists;
    private final OnArtistClickListener listener;

    public interface OnArtistClickListener {
        void onArtistClick(Artist artist);
    }

    public ArtistAdapter(List<Artist> artists, OnArtistClickListener listener) {
        this.artists = artists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artist artist = artists.get(position);
        holder.tvTitle.setText(artist.getName());
        holder.tvSubtitle.setText(String.format(Locale.getDefault(), "%d Songs", artist.getTrackCount()));
        holder.tvDuration.setVisibility(View.GONE); // Hide duration column for artists list
        holder.ivArt.setImageResource(R.drawable.cacun);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onArtistClick(artist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivArt;
        public TextView tvTitle;
        public TextView tvSubtitle;
        public TextView tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArt = itemView.findViewById(R.id.ivRowArt);
            tvTitle = itemView.findViewById(R.id.tvRowTitle);
            tvSubtitle = itemView.findViewById(R.id.tvRowSubtitle);
            tvDuration = itemView.findViewById(R.id.tvRowDuration);
        }
    }
}
