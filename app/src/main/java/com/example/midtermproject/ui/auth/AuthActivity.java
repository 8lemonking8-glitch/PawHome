package com.example.midtermproject.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.midtermproject.R;
import com.example.midtermproject.data.database.DatabaseInitializer;
import com.example.midtermproject.databinding.ActivityAuthBinding;
import com.example.midtermproject.util.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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
        // Fade in logo
        binding.ivLogo.setAlpha(0f);
        binding.ivLogo.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(200)
                .start();

        // Fade in title
        binding.tvAppName.setAlpha(0f);
        binding.tvAppName.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(800)
                .setStartDelay(400)
                .start();
        binding.tvAppName.setTranslationY(20);

        // Fade in subtitle
        binding.tvSubtitle.setAlpha(0f);
        binding.tvSubtitle.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(600)
                .start();

        // Slide up card
        binding.cardForm.setAlpha(0f);
        binding.cardForm.setTranslationY(100);
        binding.cardForm.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(800)
                .setStartDelay(500)
                .start();
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
