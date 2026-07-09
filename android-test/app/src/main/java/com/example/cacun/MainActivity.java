package com.example.cacun;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cacun.api.SocialApiManager;
import com.example.cacun.ui.dashboard.DashboardFragment;
import com.example.cacun.ui.feed.FeedFragment;
import com.example.cacun.ui.manage.ManageFragment;
import com.example.cacun.ui.alerts.AlertsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Fragment activeFragment;

    private DashboardFragment dashboardFragment;
    private FeedFragment feedFragment;
    private AlertsFragment alertsFragment;
    private ManageFragment manageFragment;

    private SocialApiManager apiManager;
    private final Handler syncHandler = new Handler(Looper.getMainLooper());
    private Runnable syncRunnable;

    private View statusLight;
    private TextView statusText;

    // Simulation sync interval (10 seconds)
    private static final int SYNC_INTERVAL_MS = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiManager = new SocialApiManager(this);
        fragmentManager = getSupportFragmentManager();

        // Header bar status elements
        statusLight = findViewById(R.id.status_light);
        statusText = findViewById(R.id.status_text);

        // Setup bottom navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_dashboard) {
                    switchToFragment(getDashboardFragment());
                    return true;
                } else if (itemId == R.id.navigation_feed) {
                    switchToFragment(getFeedFragment());
                    return true;
                } else if (itemId == R.id.navigation_alerts) {
                    switchToFragment(getAlertsFragment());
                    return true;
                } else if (itemId == R.id.navigation_accounts) {
                    switchToFragment(getManageFragment());
                    return true;
                }
                return false;
            }
        });

        // Load default fragment
        switchToFragment(getDashboardFragment());

        // Start background simulation engine
        startSyncEngine();
    }

    private void switchToFragment(Fragment targetFragment) {
        if (targetFragment == null) return;
        activeFragment = targetFragment;

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .commit();
    }

    // Lazy load fragments
    private DashboardFragment getDashboardFragment() {
        if (dashboardFragment == null) {
            dashboardFragment = new DashboardFragment();
        }
        return dashboardFragment;
    }

    private FeedFragment getFeedFragment() {
        if (feedFragment == null) {
            feedFragment = new FeedFragment();
        }
        return feedFragment;
    }

    private AlertsFragment getAlertsFragment() {
        if (alertsFragment == null) {
            alertsFragment = new AlertsFragment();
        }
        return alertsFragment;
    }

    private ManageFragment getManageFragment() {
        if (manageFragment == null) {
            manageFragment = new ManageFragment();
        }
        return manageFragment;
    }

    /**
     * Initializes and starts the background simulation updates loop
     */
    private void startSyncEngine() {
        syncRunnable = new Runnable() {
            @Override
            public void run() {
                // Run SQLite simulation in a background thread to prevent UI lag
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean changesMade = apiManager.simulateLiveUpdates();
                        
                        // Update UI on main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (changesMade) {
                                    // Flash status light to indicate active background update
                                    flashStatusSync();
                                    
                                    // Refresh the currently visible fragment
                                    if (activeFragment instanceof DashboardFragment) {
                                        ((DashboardFragment) activeFragment).refreshData();
                                    } else if (activeFragment instanceof FeedFragment) {
                                        ((FeedFragment) activeFragment).refreshData();
                                    } else if (activeFragment instanceof AlertsFragment) {
                                        ((AlertsFragment) activeFragment).refreshData();
                                    } else if (activeFragment instanceof ManageFragment) {
                                        ((ManageFragment) activeFragment).refreshData();
                                    }
                                }
                            }
                        });
                    }
                }).start();

                // Repeat every 10 seconds
                syncHandler.postDelayed(this, SYNC_INTERVAL_MS);
            }
        };
        syncHandler.postDelayed(syncRunnable, SYNC_INTERVAL_MS);
    }

    private void flashStatusSync() {
        statusText.setText("SYNCING");
        statusLight.setBackgroundResource(R.color.accent); // Tint amber
        
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                statusText.setText("LIVE");
                statusLight.setBackgroundResource(R.color.green_status); // Tint back green
            }
        }, 1200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent memory leaks by removing callbacks
        if (syncRunnable != null) {
            syncHandler.removeCallbacks(syncRunnable);
        }
    }
}
