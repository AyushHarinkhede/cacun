package com.example.cacun.ui.feed;

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
import com.example.cacun.models.SocialPost;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private final List<SocialPost> posts;
    private final Context context;

    public FeedAdapter(Context context, List<SocialPost> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        SocialPost post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {

        private final View cardPostPlatformLogo;
        private final TextView txtPostPlatformLogo;
        private final TextView txtPostAuthor;
        private final TextView txtPostHandle;
        private final TextView txtPostTime;
        private final TextView txtPostContent;
        private final TextView txtPostLikes;
        private final TextView txtPostComments;
        private final TextView txtPostShares;
        private final View cardPostMedia;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPostPlatformLogo = itemView.findViewById(R.id.card_post_platform_logo);
            txtPostPlatformLogo = itemView.findViewById(R.id.txt_post_platform_logo);
            txtPostAuthor = itemView.findViewById(R.id.txt_post_author);
            txtPostHandle = itemView.findViewById(R.id.txt_post_handle);
            txtPostTime = itemView.findViewById(R.id.txt_post_time);
            txtPostContent = itemView.findViewById(R.id.txt_post_content);
            txtPostLikes = itemView.findViewById(R.id.txt_post_likes);
            txtPostComments = itemView.findViewById(R.id.txt_post_comments);
            txtPostShares = itemView.findViewById(R.id.txt_post_shares);
            cardPostMedia = itemView.findViewById(R.id.card_post_media);
        }

        public void bind(SocialPost post) {
            String platform = post.getPlatform() != null ? post.getPlatform() : "Cacun";
            String username = post.getUsername() != null ? post.getUsername() : "user";
            
            txtPostAuthor.setText(platform + " • @" + username);
            txtPostHandle.setText(platform + " Feed");
            txtPostContent.setText(post.getContent());

            // 1. Abbreviation Logo
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
            txtPostPlatformLogo.setText(abbr);

            // 2. Relative time string
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    post.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            txtPostTime.setText(relativeTime);

            // 3. Format Metrics Counts
            txtPostLikes.setText(formatCount(post.getLikes()));
            txtPostComments.setText(formatCount(post.getComments()));
            txtPostShares.setText(formatCount(post.getShares()));

            // 4. platform-specific logo background color
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
            cardPostPlatformLogo.setBackgroundColor(ContextCompat.getColor(context, brandColorResId));

            // 5. Hide media card for Signal or Gmail where media is rare, show it for visual apps
            String p = platform.toLowerCase();
            if (p.equals("signal") || p.equals("gmail") || p.equals("duolingo")) {
                cardPostMedia.setVisibility(View.GONE);
            } else {
                cardPostMedia.setVisibility(View.VISIBLE);
            }
        }

        private String formatCount(int count) {
            if (count >= 1000000) {
                return String.format("%.1fM", count / 1000000.0);
            } else if (count >= 1000) {
                return String.format("%.1fk", count / 1000.0);
            }
            return String.valueOf(count);
        }
    }
}
