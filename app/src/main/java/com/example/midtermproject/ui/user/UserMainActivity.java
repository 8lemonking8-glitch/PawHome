package com.example.midtermproject.ui.user;

import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.midtermproject.R;
import com.example.midtermproject.databinding.ActivityUserMainBinding;
import com.example.midtermproject.ui.home.HomeFragment;
import com.example.midtermproject.ui.favorites.FavoritesFragment;
import com.example.midtermproject.ui.profile.ProfileFragment;

public class UserMainActivity extends AppCompatActivity {

    private ActivityUserMainBinding binding;
    private final HomeFragment homeFragment = new HomeFragment();
    private final FavoritesFragment favoritesFragment = new FavoritesFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (binding.bottomNavCard != null) {
                android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) binding.bottomNavCard.getLayoutParams();
                int margin24dp = (int) (24 * getResources().getDisplayMetrics().density);
                params.bottomMargin = margin24dp + insets.bottom;
                binding.bottomNavCard.setLayoutParams(params);
            }
            return windowInsets;
        });


        binding.navHome.setOnClickListener(v -> selectTab(R.id.nav_home));
        binding.navFavorites.setOnClickListener(v -> selectTab(R.id.nav_favorites));
        binding.navProfile.setOnClickListener(v -> selectTab(R.id.nav_profile));

        // Load default fragments
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_host_fragment, profileFragment, "3").hide(profileFragment)
                    .add(R.id.nav_host_fragment, favoritesFragment, "2").hide(favoritesFragment)
                    .add(R.id.nav_host_fragment, homeFragment, "1")
                    .commit();
            selectTab(R.id.nav_home);
        }
    }

    private void selectTab(int itemId) {
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
