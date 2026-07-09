package com.example.cacun.ui.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cacun.R;
import com.example.cacun.database.DatabaseHelper;
import com.example.cacun.models.SocialPost;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private FeedAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutEmptyFeed;
    private final List<SocialPost> postList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_feed);
        layoutEmptyFeed = view.findViewById(R.id.layout_empty_feed);

        recyclerView = view.findViewById(R.id.recycler_feed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FeedAdapter(requireContext(), postList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        // Set SwipeRefresh theme colors to match premium theme
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary_dark, R.color.accent);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.bg_dark_secondary);

        refreshData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        if (!isAdded()) return;

        List<SocialPost> freshPosts = dbHelper.getAllFeedPosts();
        postList.clear();
        postList.addAll(freshPosts);
        adapter.notifyDataSetChanged();

        swipeRefreshLayout.setRefreshing(false);

        if (freshPosts.isEmpty()) {
            layoutEmptyFeed.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyFeed.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
