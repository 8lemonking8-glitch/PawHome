package com.example.midtermproject.ui.admin;

import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.R;
import com.example.midtermproject.databinding.ActivityAdminMainBinding;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private AdminDashboardFragment dashboardFragment;
    private AdminPetsFragment petsFragment;
    private AdminRequestsFragment requestsFragment;
    private Fragment activeFragment;
    private int activeTabId = R.id.nav_dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            getWindow().setStatusBarContrastEnforced(false);
            getWindow().setNavigationBarContrastEnforced(false);
        }
        androidx.core.view.WindowInsetsControllerCompat insetsController = 
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightNavigationBars(true);
            insetsController.setAppearanceLightStatusBars(false);
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (binding.adminBottomNavCard != null) {
                android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) binding.adminBottomNavCard.getLayoutParams();
                int margin24dp = (int) (24 * getResources().getDisplayMetrics().density);
                params.bottomMargin = margin24dp + insets.bottom;
                binding.adminBottomNavCard.setLayoutParams(params);
            }
            for (int i = 0; i < binding.getRoot().getChildCount(); i++) {
                ViewCompat.dispatchApplyWindowInsets(binding.getRoot().getChildAt(i), windowInsets);
            }
            return windowInsets;
        });


        binding.navDashboard.setOnClickListener(v -> selectTab(R.id.nav_dashboard));
        binding.navPets.setOnClickListener(v -> selectTab(R.id.nav_pets));
        binding.navRequests.setOnClickListener(v -> selectTab(R.id.nav_requests));

        // Set default fragment
        if (savedInstanceState == null) {
            dashboardFragment = new AdminDashboardFragment();
            petsFragment = new AdminPetsFragment();
            requestsFragment = new AdminRequestsFragment();
            activeFragment = dashboardFragment;
            activeTabId = R.id.nav_dashboard;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.admin_nav_host_fragment, requestsFragment, "3").hide(requestsFragment)
                    .add(R.id.admin_nav_host_fragment, petsFragment, "2").hide(petsFragment)
                    .add(R.id.admin_nav_host_fragment, dashboardFragment, "1")
                    .commit();
            selectTab(R.id.nav_dashboard);
        } else {
            dashboardFragment = (AdminDashboardFragment) getSupportFragmentManager().findFragmentByTag("1");
            petsFragment = (AdminPetsFragment) getSupportFragmentManager().findFragmentByTag("2");
            requestsFragment = (AdminRequestsFragment) getSupportFragmentManager().findFragmentByTag("3");
            
            activeTabId = savedInstanceState.getInt("activeTabId", R.id.nav_dashboard);
            if (activeTabId == R.id.nav_dashboard) {
                activeFragment = dashboardFragment;
            } else if (activeTabId == R.id.nav_pets) {
                activeFragment = petsFragment;
            } else if (activeTabId == R.id.nav_requests) {
                activeFragment = requestsFragment;
            }
            
            binding.navDashboard.setSelected(activeTabId == R.id.nav_dashboard);
            binding.navPets.setSelected(activeTabId == R.id.nav_pets);
            binding.navRequests.setSelected(activeTabId == R.id.nav_requests);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("activeTabId", activeTabId);
    }

    public void switchToTab(int itemId) {
        selectTab(itemId);
    }

    private void selectTab(int itemId) {
        activeTabId = itemId;
        binding.navDashboard.setSelected(itemId == R.id.nav_dashboard);
        binding.navPets.setSelected(itemId == R.id.nav_pets);
        binding.navRequests.setSelected(itemId == R.id.nav_requests);

        Fragment selectedFragment = null;
        if (itemId == R.id.nav_dashboard) {
            selectedFragment = dashboardFragment;
        } else if (itemId == R.id.nav_pets) {
            selectedFragment = petsFragment;
        } else if (itemId == R.id.nav_requests) {
            selectedFragment = requestsFragment;
        }
        
        if (selectedFragment != null && selectedFragment != activeFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(selectedFragment)
                    .commit();
            activeFragment = selectedFragment;
        }
    }
}
