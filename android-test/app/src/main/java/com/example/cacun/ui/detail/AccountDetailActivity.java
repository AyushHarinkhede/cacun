package com.example.cacun.ui.detail;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.database.DatabaseHelper;
import com.example.cacun.models.SocialAccount;
import com.example.cacun.models.SocialPost;
import com.example.cacun.ui.feed.FeedAdapter;

import java.util.List;

public class AccountDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SocialAccount account;

    private View cardDetailLogo;
    private TextView txtDetailLogo;
    private TextView txtDetailUsername;
    private TextView txtDetailPlatform;
    private View viewDetailStatusDot;
    private TextView txtDetailStatus;
    private TextView txtDetailFollowers;
    private TextView txtDetailStreak;
    private TextView txtDetailScreenTime;
    private ProgressBar progressScreenTime;
    private TextView txtProgressDetails;
    private RecyclerView recyclerDetailFeed;
    private TextView txtEmptyDetailFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        dbHelper = new DatabaseHelper(this);

        // Get account ID from intent
        int accountId = getIntent().getIntExtra("account_id", -1);
        account = dbHelper.getAccount(accountId);

        if (account == null) {
            Toast.makeText(this, "Account details not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        cardDetailLogo = findViewById(R.id.card_detail_logo);
        txtDetailLogo = findViewById(R.id.txt_detail_logo);
        txtDetailUsername = findViewById(R.id.txt_detail_username);
        txtDetailPlatform = findViewById(R.id.txt_detail_platform);
        viewDetailStatusDot = findViewById(R.id.view_detail_status_dot);
        txtDetailStatus = findViewById(R.id.txt_detail_status);
        txtDetailFollowers = findViewById(R.id.txt_detail_followers);
        txtDetailStreak = findViewById(R.id.txt_detail_streak);
        txtDetailScreenTime = findViewById(R.id.txt_detail_screen_time);
        progressScreenTime = findViewById(R.id.progress_screen_time);
        txtProgressDetails = findViewById(R.id.txt_progress_details);
        recyclerDetailFeed = findViewById(R.id.recycler_detail_feed);
        txtEmptyDetailFeed = findViewById(R.id.txt_empty_detail_feed);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(account.getPlatform() + " Details");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        populateData();
        setupFeed();
    }

    private void populateData() {
        String platform = account.getPlatform();
        String username = account.getUsername();

        txtDetailUsername.setText("@" + username);
        txtDetailPlatform.setText(platform + " Connected Account");
        txtDetailLogo.setText(account.getPlatformAbbreviation());

        // 1. Followers
        int followers = account.getFollowers();
        if (followers >= 1000000) {
            txtDetailFollowers.setText(String.format("%.2fM", followers / 1000000.0));
        } else if (followers >= 1000) {
            txtDetailFollowers.setText(String.format("%.1fk", followers / 1000.0));
        } else {
            txtDetailFollowers.setText(String.valueOf(followers));
        }

        // 2. Streak
        if (account.getStreak() > 0) {
            txtDetailStreak.setText(account.getStreak() + " 🔥");
        } else {
            txtDetailStreak.setText("None");
        }

        // 3. Screen Time
        txtDetailScreenTime.setText(account.getScreenTime() + "m");

        // 4. Progress Monitor (Daily limit set to 120 minutes)
        int screenTimeMinutes = account.getScreenTime();
        progressScreenTime.setProgress(Math.min(screenTimeMinutes, 120));
        txtProgressDetails.setText(screenTimeMinutes + "m spent / 120m daily limit");

        // Color code progress bar if limit is exceeded
        if (screenTimeMinutes > 100) {
            progressScreenTime.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, R.color.red_status), PorterDuff.Mode.SRC_IN);
        } else {
            progressScreenTime.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_IN);
        }

        // 5. Status
        if (account.isOnline()) {
            viewDetailStatusDot.setBackgroundResource(R.color.green_status);
            txtDetailStatus.setText("ACTIVE NOW");
            txtDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.green_status));
        } else {
            viewDetailStatusDot.setBackgroundResource(R.color.gray);
            txtDetailStatus.setText("OFFLINE");
            txtDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }

        // 6. Color theme of platform logo card
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
        cardDetailLogo.setBackgroundColor(ContextCompat.getColor(this, brandColorResId));
    }

    private void setupFeed() {
        List<SocialPost> posts = dbHelper.getAccountPosts(account.getId());
        
        if (posts.isEmpty()) {
            txtEmptyDetailFeed.setVisibility(View.VISIBLE);
            recyclerDetailFeed.setVisibility(View.GONE);
        } else {
            txtEmptyDetailFeed.setVisibility(View.GONE);
            recyclerDetailFeed.setVisibility(View.VISIBLE);

            recyclerDetailFeed.setLayoutManager(new LinearLayoutManager(this));
            FeedAdapter adapter = new FeedAdapter(this, posts);
            recyclerDetailFeed.setAdapter(adapter);
        }
    }
}
