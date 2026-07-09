package com.example.cacun.ui.manage;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cacun.R;
import com.example.cacun.api.SocialApiManager;
import com.example.cacun.database.DatabaseHelper;
import com.example.cacun.models.SocialAccount;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ManageFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private SocialApiManager apiManager;
    private RecyclerView recyclerView;
    private ManageAdapter adapter;
    private final List<SocialAccount> accountList = new ArrayList<>();
    private LinearLayout layoutEmptyManage;

    private static final String[] SOCIAL_PLATFORMS = {
        "Instagram", "Facebook", "X", "Snapchat", "Reddit", "LinkedIn", 
        "GitHub", "YouTube", "Gmail", "Pinterest", "Signal", "Duolingo"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        apiManager = new SocialApiManager(requireContext());

        layoutEmptyManage = view.findViewById(R.id.layout_empty_manage);
        MaterialButton btnLinkAccount = view.findViewById(R.id.btn_link_account);

        recyclerView = view.findViewById(R.id.recycler_manage_accounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new ManageAdapter(requireContext(), accountList, new ManageAdapter.OnUnlinkClickListener() {
            @Override
            public void onUnlink(SocialAccount account) {
                dbHelper.deleteAccount(account.getId());
                Toast.makeText(getContext(), account.getPlatform() + " account unlinked.", Toast.LENGTH_SHORT).show();
                refreshData();
            }
        });
        recyclerView.setAdapter(adapter);

        btnLinkAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAccountDialog();
            }
        });

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

        List<SocialAccount> freshAccounts = dbHelper.getAllAccounts();
        accountList.clear();
        accountList.addAll(freshAccounts);
        adapter.notifyDataSetChanged();

        if (freshAccounts.isEmpty()) {
            layoutEmptyManage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyManage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Inflates dialog_add_account.xml and shows a customized popup to connect a new profile
     */
    private void showAddAccountDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_account, null);
        final Spinner spinner = dialogView.findViewById(R.id.spinner_platforms);
        final EditText editUsername = dialogView.findViewById(R.id.edit_username);
        final EditText editApiKey = dialogView.findViewById(R.id.edit_api_key);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_add);
        MaterialButton btnConnect = dialogView.findViewById(R.id.btn_confirm_add);

        // Populate spinner with brand list
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, SOCIAL_PLATFORMS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String platform = spinner.getSelectedItem().toString();
                String username = editUsername.getText().toString().trim();
                String apiKey = editApiKey.getText().toString().trim();

                if (username.isEmpty()) {
                    editUsername.setError("Username cannot be empty");
                    return;
                }

                // Check for duplicates
                for (SocialAccount acct : accountList) {
                    if (acct.getPlatform().equalsIgnoreCase(platform) && acct.getUsername().equalsIgnoreCase(username)) {
                        Toast.makeText(getContext(), "Account already linked!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                SocialAccount newAccount = new SocialAccount();
                newAccount.setPlatform(platform);
                newAccount.setUsername(username);
                newAccount.setApiKey(apiKey);

                // Use ApiManager to build mock or real stats and save
                apiManager.connectAccount(newAccount, new SocialApiManager.ApiCallback() {
                    @Override
                    public void onSuccess() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "Connected successfully!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    refreshData();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(final Exception e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "Sync Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    refreshData();
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
