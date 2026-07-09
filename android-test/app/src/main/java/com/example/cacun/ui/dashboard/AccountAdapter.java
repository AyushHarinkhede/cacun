package com.example.cacun.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.models.SocialAccount;
import com.example.cacun.ui.detail.AccountDetailActivity;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private final List<SocialAccount> accounts;
    private final Context context;

    public AccountAdapter(Context context, List<SocialAccount> accounts) {
        this.context = context;
        this.accounts = accounts;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        SocialAccount account = accounts.get(position);
        holder.bind(account);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {

        private final View cardPlatformLogo;
        private final TextView txtPlatformLogo;
        private final TextView txtPlatformName;
        private final TextView txtUsername;
        private final TextView txtFollowersCount;
        private final TextView txtAccountStreak;
        private final View viewStatusDot;
        private final TextView txtStatus;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPlatformLogo = itemView.findViewById(R.id.card_platform_logo);
            txtPlatformLogo = itemView.findViewById(R.id.txt_platform_logo);
            txtPlatformName = itemView.findViewById(R.id.txt_platform_name);
            txtUsername = itemView.findViewById(R.id.txt_username);
            txtFollowersCount = itemView.findViewById(R.id.txt_followers_count);
            txtAccountStreak = itemView.findViewById(R.id.txt_account_streak);
            viewStatusDot = itemView.findViewById(R.id.view_status_dot);
            txtStatus = itemView.findViewById(R.id.txt_status);
        }

        public void bind(final SocialAccount account) {
            txtPlatformName.setText(account.getPlatform());
            txtUsername.setText("@" + account.getUsername());
            txtPlatformLogo.setText(account.getPlatformAbbreviation());

            // 1. Format audience stats based on platform
            String countSuffix = " followers";
            String platform = account.getPlatform().toLowerCase();
            if (platform.equals("youtube")) {
                countSuffix = " subscribers";
            } else if (platform.equals("duolingo")) {
                countSuffix = " followers";
            } else if (platform.equals("gmail")) {
                countSuffix = " total emails";
            } else if (platform.equals("signal")) {
                countSuffix = " contacts";
            }

            int followers = account.getFollowers();
            if (followers >= 1000000) {
                txtFollowersCount.setText(String.format("%.1fM%s", followers / 1000000.0, countSuffix));
            } else if (followers >= 1000) {
                txtFollowersCount.setText(String.format("%.1fk%s", followers / 1000.0, countSuffix));
            } else {
                txtFollowersCount.setText(followers + countSuffix);
            }

            // 2. Set Streak
            if (account.getStreak() > 0) {
                txtAccountStreak.setVisibility(View.VISIBLE);
                if (platform.equals("duolingo")) {
                    txtAccountStreak.setText("🔥 " + account.getStreak() + " Day Streak");
                } else if (platform.equals("snapchat")) {
                    txtAccountStreak.setText("🔥 " + account.getStreak() + " Snapstreak");
                } else {
                    txtAccountStreak.setText("🔥 " + account.getStreak() + " Days Active");
                }
            } else {
                txtAccountStreak.setVisibility(View.GONE);
            }

            // 3. Set Online status
            if (account.isOnline()) {
                viewStatusDot.setBackgroundResource(R.color.green_status);
                txtStatus.setText("Online");
            } else {
                viewStatusDot.setBackgroundResource(R.color.gray);
                txtStatus.setText("Offline");
            }

            // 4. Set platform logo brand color
            int brandColorResId = R.color.primary;
            switch (platform) {
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
            cardPlatformLogo.setBackgroundColor(ContextCompat.getColor(context, brandColorResId));

            // 5. Open single platform detail dashboard on card click
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AccountDetailActivity.class);
                    intent.putExtra("account_id", account.getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
