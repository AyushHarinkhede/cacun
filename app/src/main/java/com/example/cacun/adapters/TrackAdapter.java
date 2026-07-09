package com.example.cacun.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.models.Track;

import java.util.List;
import java.util.Locale;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {

    private final List<Track> tracks;
    private final OnTrackClickListener listener;

    public interface OnTrackClickListener {
        void onTrackClick(int position);
        void onTrackLongClick(int position);
    }

    public TrackAdapter(List<Track> tracks, OnTrackClickListener listener) {
        this.tracks = tracks;
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
        Track track = tracks.get(position);
        holder.tvTitle.setText(track.getTitle());
        holder.tvArtist.setText(track.getArtist());
        holder.tvDuration.setText(formatDuration(track.getDuration()));

        // Standard default art, or handle media metadata query in fragment
        holder.ivArt.setImageResource(R.drawable.cacun);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTrackClick(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTrackLongClick(position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    private String formatDuration(int durationMs) {
        int seconds = (durationMs / 1000) % 60;
        int minutes = (durationMs / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivArt;
        public TextView tvTitle;
        public TextView tvArtist;
        public TextView tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArt = itemView.findViewById(R.id.ivRowArt);
            tvTitle = itemView.findViewById(R.id.tvRowTitle);
            tvArtist = itemView.findViewById(R.id.tvRowSubtitle);
            tvDuration = itemView.findViewById(R.id.tvRowDuration);
        }
    }
}
