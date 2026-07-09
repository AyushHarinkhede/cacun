package com.example.cacun.ui.alerts;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.models.SocialNotification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<SocialNotification> notifications;
    private final Context context;

    public NotificationAdapter(Context context, List<SocialNotification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        SocialNotification notif = notifications.get(position);
        holder.bind(notif);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final View cardNotifLogo;
        private final TextView txtNotifLogo;
        private final TextView txtNotifTitle;
        private final TextView txtNotifMessage;
        private final View viewNotifUnreadDot;
        private final TextView txtNotifTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotifLogo = itemView.findViewById(R.id.card_notif_logo);
            txtNotifLogo = itemView.findViewById(R.id.txt_notif_logo);
            txtNotifTitle = itemView.findViewById(R.id.txt_notif_title);
            txtNotifMessage = itemView.findViewById(R.id.txt_notif_message);
            viewNotifUnreadDot = itemView.findViewById(R.id.view_notif_unread_dot);
            txtNotifTime = itemView.findViewById(R.id.txt_notif_time);
        }

        public void bind(SocialNotification notif) {
            txtNotifTitle.setText(notif.getTitle());
            txtNotifMessage.setText(notif.getMessage());

            // 1. Get Platform Abbreviation
            String platform = notif.getPlatform() != null ? notif.getPlatform() : "Cacun";
            String abbr = "?";
            if (platform.length() > 0) {
                switch (platform.toLowerCase()) {
                    case "instagram": abbr = "IG"; break;
                    case "facebook": abbr = "FB"; break;
                    case "x": abbr = "X"; break;
                    case "snapchat": abbr = "SC"; break;
                    case "reddit": abbr = "RD"; break;
                    case "linkedin": abbr = "LI"; break;
                    case "github": abbr = "GH"; break;
                    case "youtube": abbr = "YT"; break;
                    case "gmail": abbr = "GM"; break;
                    case "pinterest": abbr = "PI"; break;
                    case "signal": abbr = "SG"; break;
                    case "duolingo": abbr = "DL"; break;
                    default: abbr = platform.substring(0, Math.min(platform.length(), 2)).toUpperCase(); break;
                }
            }
            txtNotifLogo.setText(abbr);

            // 2. Tint platform brand color
            int brandColorResId = R.color.primary;
            switch (platform.toLowerCase()) {
                case "instagram": brandColorResId = R.color.color_instagram; break;
                case "facebook": brandColorResId = R.color.color_facebook; break;
                case "x": brandColorResId = R.color.color_x; break;
                case "snapchat": brandColorResId = R.color.color_snapchat; break;
                case "reddit": brandColorResId = R.color.color_reddit; break;
                case "linkedin": brandColorResId = R.color.color_linkedin; break;
                case "github": brandColorResId = R.color.color_github; break;
                case "youtube": brandColorResId = R.color.color_youtube; break;
                case "gmail": brandColorResId = R.color.color_gmail; break;
                case "pinterest": brandColorResId = R.color.color_pinterest; break;
                case "signal": brandColorResId = R.color.color_signal; break;
                case "duolingo": brandColorResId = R.color.color_duolingo; break;
            }
            cardNotifLogo.setBackgroundColor(ContextCompat.getColor(context, brandColorResId));

            // 3. Relative timestamp
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    notif.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            txtNotifTime.setText(relativeTime);

            // 4. Toggle unread blue dot
            if (notif.isRead()) {
                viewNotifUnreadDot.setVisibility(View.INVISIBLE);
            } else {
                viewNotifUnreadDot.setVisibility(View.VISIBLE);
            }
        }
    }
}
