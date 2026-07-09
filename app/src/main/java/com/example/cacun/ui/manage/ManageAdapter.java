package com.example.cacun.ui.manage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.models.SocialAccount;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ManageAdapter extends RecyclerView.Adapter<ManageAdapter.ManageViewHolder> {

    private final List<SocialAccount> accounts;
    private final Context context;
    private final OnUnlinkClickListener unlinkClickListener;

    public interface OnUnlinkClickListener {
        void onUnlink(SocialAccount account);
    }

    public ManageAdapter(Context context, List<SocialAccount> accounts, OnUnlinkClickListener listener) {
        this.context = context;
        this.accounts = accounts;
        this.unlinkClickListener = listener;
    }

    @NonNull
    @Override
    public ManageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_account, parent, false);
        return new ManageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageViewHolder holder, int position) {
        SocialAccount account = accounts.get(position);
        holder.bind(account);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    class ManageViewHolder extends RecyclerView.ViewHolder {

        private final View cardManageLogo;
        private final TextView txtManageLogo;
        private final TextView txtManagePlatform;
        private final TextView txtManageUsername;
        private final MaterialButton btnUnlinkAccount;

        public ManageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardManageLogo = itemView.findViewById(R.id.card_manage_logo);
            txtManageLogo = itemView.findViewById(R.id.txt_manage_logo);
            txtManagePlatform = itemView.findViewById(R.id.txt_manage_platform);
            txtManageUsername = itemView.findViewById(R.id.txt_manage_username);
            btnUnlinkAccount = itemView.findViewById(R.id.btn_unlink_account);
        }

        public void bind(final SocialAccount account) {
            txtManagePlatform.setText(account.getPlatform());
            txtManageUsername.setText("@" + account.getUsername());
            txtManageLogo.setText(account.getPlatformAbbreviation());

            // 1. Set platform brand color
            String platform = account.getPlatform().toLowerCase();
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
            cardManageLogo.setBackgroundColor(ContextCompat.getColor(context, brandColorResId));

            // 2. Click listener for delete action
            btnUnlinkAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (unlinkClickListener != null) {
                        unlinkClickListener.onUnlink(account);
                    }
                }
            });
        }
    }
}
