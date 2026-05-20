package com.example.midtermproject.ui.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.R;
import com.example.midtermproject.databinding.ActivityAdminMainBinding;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.adminBottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_dashboard) {
                selectedFragment = new AdminDashboardFragment();
            } else if (itemId == R.id.navigation_pets) {
                selectedFragment = new AdminPetsFragment();
            } else if (itemId == R.id.navigation_requests) {
                selectedFragment = new AdminRequestsFragment();
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_nav_host_fragment, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            binding.adminBottomNavigation.setSelectedItemId(R.id.navigation_dashboard);
        }
    }
}
