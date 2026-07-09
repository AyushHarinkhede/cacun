package com.example.cacun.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.database.DatabaseHelper;
import com.example.cacun.models.SocialAccount;
import com.example.cacun.models.SocialPost;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private final List<SocialAccount> accountList = new ArrayList<>();

    private TextView txtTotalChannels;
    private TextView txtTotalEngagement;
    private TextView txtTotalStreaks;
    private LinearLayout layoutEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Initialize UI Elements
        txtTotalChannels = view.findViewById(R.id.txt_total_channels);
        txtTotalEngagement = view.findViewById(R.id.txt_total_engagement);
        txtTotalStreaks = view.findViewById(R.id.txt_total_streaks);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        recyclerView = view.findViewById(R.id.recycler_dashboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AccountAdapter(requireContext(), accountList);
        recyclerView.setAdapter(adapter);

        refreshData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    /**
     * Reads database, computes aggregations, and refreshes the RecyclerView
     */
    public void refreshData() {
        if (!isAdded()) return;

        List<SocialAccount> freshAccounts = dbHelper.getAllAccounts();
        accountList.clear();
        accountList.addAll(freshAccounts);
        adapter.notifyDataSetChanged();

        if (freshAccounts.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            txtTotalChannels.setText("0");
            txtTotalEngagement.setText("0");
            txtTotalStreaks.setText("0 🔥");
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Compute summary statistics
            txtTotalChannels.setText(String.valueOf(freshAccounts.size()));

            // Aggregate streaks
            int totalStreaks = 0;
            for (SocialAccount acct : freshAccounts) {
                totalStreaks += acct.getStreak();
            }
            txtTotalStreaks.setText(totalStreaks + " 🔥");

            // Aggregate engagement (likes + comments + shares across all posts)
            int totalEngagement = 0;
            List<SocialPost> posts = dbHelper.getAllFeedPosts();
            for (SocialPost post : posts) {
                totalEngagement += (post.getLikes() + post.getComments() + post.getShares());
            }

            if (totalEngagement >= 1000) {
                txtTotalEngagement.setText(String.format("%.1fk", totalEngagement / 1000.0));
            } else {
                txtTotalEngagement.setText(String.valueOf(totalEngagement));
            }
        }
    }
}
