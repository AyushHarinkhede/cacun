package com.example.cacun.ui.alerts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cacun.R;
import com.example.cacun.database.DatabaseHelper;
import com.example.cacun.models.SocialNotification;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutEmptyAlerts;
    private final List<SocialNotification> notificationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_alerts);
        layoutEmptyAlerts = view.findViewById(R.id.layout_empty_alerts);
        MaterialButton btnClearAlerts = view.findViewById(R.id.btn_clear_alerts);

        recyclerView = view.findViewById(R.id.recycler_alerts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(requireContext(), notificationList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        btnClearAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notificationList.isEmpty()) {
                    Toast.makeText(getContext(), "No notifications to read.", Toast.LENGTH_SHORT).show();
                    return;
                }
                dbHelper.markAllNotificationsAsRead();
                Toast.makeText(getContext(), "All notifications marked as read.", Toast.LENGTH_SHORT).show();
                refreshData();
            }
        });

        // Styling swipeRefreshLayout
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

        List<SocialNotification> freshNotifs = dbHelper.getAllNotifications();
        notificationList.clear();
        notificationList.addAll(freshNotifs);
        adapter.notifyDataSetChanged();

        swipeRefreshLayout.setRefreshing(false);

        if (freshNotifs.isEmpty()) {
            layoutEmptyAlerts.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyAlerts.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
