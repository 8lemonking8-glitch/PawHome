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
    
    private int lastImeHeight = 0;
    private final android.os.Handler insetHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable pendingImeRunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseInitializer.initialize(this);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
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
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            
            int newImeHeight = ime.bottom;
            
            if (newImeHeight > 0) {
                
                if (pendingImeRunnable != null) {
                    insetHandler.removeCallbacks(pendingImeRunnable);
                    pendingImeRunnable = null;
                }

                v.setPadding(0, 0, 0, newImeHeight);
                lastImeHeight = newImeHeight;
            } else {

                if (lastImeHeight > 0) {
                    if (pendingImeRunnable == null) {
                        pendingImeRunnable = () -> {
                            v.setPadding(0, 0, 0, 0);
                            lastImeHeight = 0;
                            pendingImeRunnable = null;
                        };
                        
                        insetHandler.postDelayed(pendingImeRunnable, 150);
                    }
                } else {
                    
                    v.setPadding(0, 0, 0, 0);
                }
            }

            binding.layoutCardContent.setPadding(0, 0, 0, systemBars.bottom);
            
            return windowInsets;
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
        binding.ivLoginBg.setAlpha(0f);
        binding.ivLoginBg.animate()
                .alpha(1f)
                .setDuration(800)
                .start();

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
            intent = new Intent(this, com.example.midtermproject.ui.admin.AdminMainActivity.class);
        } else {
            intent = new Intent(this, com.example.midtermproject.ui.user.UserMainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
