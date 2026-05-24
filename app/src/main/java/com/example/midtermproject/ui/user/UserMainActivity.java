package com.example.midtermproject.ui.user;

import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.midtermproject.R;
import com.example.midtermproject.databinding.ActivityUserMainBinding;
import com.example.midtermproject.ui.home.HomeFragment;
import com.example.midtermproject.ui.favorites.FavoritesFragment;
import com.example.midtermproject.ui.profile.ProfileFragment;

public class UserMainActivity extends AppCompatActivity {

    private ActivityUserMainBinding binding;
    private HomeFragment homeFragment;
    private FavoritesFragment favoritesFragment;
    private ProfileFragment profileFragment;
    private Fragment activeFragment;
    private int activeTabId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserMainBinding.inflate(getLayoutInflater());
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
            if (binding.bottomNavCard != null) {
                android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) binding.bottomNavCard.getLayoutParams();
                int margin24dp = (int) (24 * getResources().getDisplayMetrics().density);
                params.bottomMargin = margin24dp + insets.bottom;
                binding.bottomNavCard.setLayoutParams(params);
            }
            for (int i = 0; i < binding.getRoot().getChildCount(); i++) {
                ViewCompat.dispatchApplyWindowInsets(binding.getRoot().getChildAt(i), windowInsets);
            }
            return windowInsets;
        });

        binding.navHome.setOnClickListener(v -> selectTab(R.id.nav_home));
        binding.navFavorites.setOnClickListener(v -> selectTab(R.id.nav_favorites));
        binding.navProfile.setOnClickListener(v -> selectTab(R.id.nav_profile));

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            favoritesFragment = new FavoritesFragment();
            profileFragment = new ProfileFragment();
            activeFragment = homeFragment;
            activeTabId = R.id.nav_home;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_host_fragment, profileFragment, "3").hide(profileFragment)
                    .add(R.id.nav_host_fragment, favoritesFragment, "2").hide(favoritesFragment)
                    .add(R.id.nav_host_fragment, homeFragment, "1")
                    .commit();
            selectTab(R.id.nav_home);
        } else {
            homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("1");
            favoritesFragment = (FavoritesFragment) getSupportFragmentManager().findFragmentByTag("2");
            profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("3");
            
            activeTabId = savedInstanceState.getInt("activeTabId", R.id.nav_home);
            if (activeTabId == R.id.nav_home) {
                activeFragment = homeFragment;
            } else if (activeTabId == R.id.nav_favorites) {
                activeFragment = favoritesFragment;
            } else if (activeTabId == R.id.nav_profile) {
                activeFragment = profileFragment;
            }
            
            binding.navHome.setSelected(activeTabId == R.id.nav_home);
            binding.navFavorites.setSelected(activeTabId == R.id.nav_favorites);
            binding.navProfile.setSelected(activeTabId == R.id.nav_profile);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("activeTabId", activeTabId);
    }

    private void selectTab(int itemId) {
        activeTabId = itemId;
        binding.navHome.setSelected(itemId == R.id.nav_home);
        binding.navFavorites.setSelected(itemId == R.id.nav_favorites);
        binding.navProfile.setSelected(itemId == R.id.nav_profile);

        Fragment selectedFragment = null;
        if (itemId == R.id.nav_home) {
            selectedFragment = homeFragment;
        } else if (itemId == R.id.nav_favorites) {
            selectedFragment = favoritesFragment;
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = profileFragment;
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
