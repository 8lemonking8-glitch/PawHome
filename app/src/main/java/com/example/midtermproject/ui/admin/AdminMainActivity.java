package com.example.midtermproject.ui.admin;

import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.R;
import com.example.midtermproject.databinding.ActivityAdminMainBinding;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private final AdminDashboardFragment dashboardFragment = new AdminDashboardFragment();
    private final AdminPetsFragment petsFragment = new AdminPetsFragment();
    private final AdminRequestsFragment requestsFragment = new AdminRequestsFragment();
    private Fragment activeFragment = dashboardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (binding.adminBottomNavCard != null) {
                android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) binding.adminBottomNavCard.getLayoutParams();
                int margin24dp = (int) (24 * getResources().getDisplayMetrics().density);
                params.bottomMargin = margin24dp + insets.bottom;
                binding.adminBottomNavCard.setLayoutParams(params);
            }
            return windowInsets;
        });


        binding.navDashboard.setOnClickListener(v -> selectTab(R.id.nav_dashboard));
        binding.navPets.setOnClickListener(v -> selectTab(R.id.nav_pets));
        binding.navRequests.setOnClickListener(v -> selectTab(R.id.nav_requests));

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.admin_nav_host_fragment, requestsFragment, "3").hide(requestsFragment)
                    .add(R.id.admin_nav_host_fragment, petsFragment, "2").hide(petsFragment)
                    .add(R.id.admin_nav_host_fragment, dashboardFragment, "1")
                    .commit();
            selectTab(R.id.nav_dashboard);
        }
    }

    private void selectTab(int itemId) {
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
