package com.example.midtermproject.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;

import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.midtermproject.R;
import com.example.midtermproject.data.database.DatabaseInitializer;
import com.example.midtermproject.databinding.ActivityAuthBinding;
import com.example.midtermproject.util.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize database with seed data
        DatabaseInitializer.initialize(this);

        // Check if already logged in
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, insets.bottom);

            return WindowInsetsCompat.CONSUMED;
        });


        setupViewPager();
        playEntryAnimations();
    }

    private void setupViewPager() {
        AuthPagerAdapter adapter = new AuthPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(position == 0 ? getString(R.string.login) : getString(R.string.register));
        }).attach();
    }

    private void playEntryAnimations() {
        // Fade in background slightly
        binding.ivLoginBg.setAlpha(0f);
        binding.ivLoginBg.animate()
                .alpha(1f)
                .setDuration(800)
                .start();

        // Slide up card
        binding.cardForm.setAlpha(0f);
        binding.cardForm.setTranslationY(100);
        binding.cardForm.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(800)
                .setStartDelay(200)
                .start();
    }

    public void switchToLoginTab() {
        if (binding.viewPager.getScrollState() == ViewPager2.SCROLL_STATE_IDLE) {
            binding.viewPager.setCurrentItem(0, true);
        }
    }

    public void switchToRegisterTab() {
        if (binding.viewPager.getScrollState() == ViewPager2.SCROLL_STATE_IDLE) {
            binding.viewPager.setCurrentItem(1, true);
        }
    }

    public void navigateToMain() {
        Intent intent;
        if (sessionManager.isAdmin()) {
            // Admin user
            intent = new Intent(this, com.example.midtermproject.ui.admin.AdminMainActivity.class);
        } else {
            // Go to the newly created UserMainActivity
            intent = new Intent(this, com.example.midtermproject.ui.user.UserMainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
