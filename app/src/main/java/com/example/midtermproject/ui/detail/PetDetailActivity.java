package com.example.midtermproject.ui.detail;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.databinding.ActivityPetDetailBinding;
import com.example.midtermproject.util.FavoriteManager;
import com.example.midtermproject.util.PetImageUtils;
import com.example.midtermproject.util.SessionManager;
import com.example.midtermproject.ui.profile.EditProfileBottomSheet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class PetDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PET_ID = "extra_pet_id";
    public static final String EXTRA_PET_IMAGE_RES_ID = "extra_pet_image_res_id";
    public static final String EXTRA_PET_IMAGE_RES_IDS = "extra_pet_image_res_ids";
    public static final String EXTRA_PET_TYPE = "extra_pet_type";

    private ActivityPetDetailBinding binding;
    private PetRepository petRepository;
    private FavoriteManager favoriteManager;
    private ImagePagerAdapter imagePagerAdapter;
    private long petId;
    private PetEntity currentPet;

    private int petImageResId;
    private String petImageResIds;
    private String petType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPetDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            android.view.ViewGroup.MarginLayoutParams mlp = (android.view.ViewGroup.MarginLayoutParams) binding.toolbar.getLayoutParams();
            mlp.topMargin = insets.top;
            binding.toolbar.setLayoutParams(mlp);

            android.view.ViewGroup.MarginLayoutParams fabMlp = (android.view.ViewGroup.MarginLayoutParams) binding.fabAdopt.getLayoutParams();
            fabMlp.bottomMargin = insets.bottom + (int)(32 * getResources().getDisplayMetrics().density);
            binding.fabAdopt.setLayoutParams(fabMlp);

            return WindowInsetsCompat.CONSUMED;
        });

        petId = getIntent().getLongExtra(EXTRA_PET_ID, -1);
        if (petId == -1) {
            finish();
            return;
        }

        petImageResId = getIntent().getIntExtra(EXTRA_PET_IMAGE_RES_ID, 0);
        petImageResIds = getIntent().getStringExtra(EXTRA_PET_IMAGE_RES_IDS);
        petType = getIntent().getStringExtra(EXTRA_PET_TYPE);

        petRepository = new PetRepository(getApplication());
        favoriteManager = new FavoriteManager(this);

        setupToolbar();
        setupImagePager();

        // Set transition name for native shared element transition
        ViewCompat.setTransitionName(binding.vpPetImages, "pet_image_" + petId);

        loadPetDetails();

        binding.fabAdopt.setOnClickListener(v -> handleAdoptClick());

        binding.btnFavorite.setOnClickListener(v -> {
            favoriteManager.toggleFavorite(petId);
            updateFavoriteIcon();
            binding.btnFavorite.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_bounce));
        });
    }

    // ==================== Adoption Flow ====================

    private void handleAdoptClick() {
        if (currentPet == null || !"AVAILABLE".equals(currentPet.getStatus())) {
            Snackbar.make(binding.getRoot(), "Pet is no longer available", Snackbar.LENGTH_SHORT).show();
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();
        UserRepository userRepository = new UserRepository(getApplication());
        binding.fabAdopt.setEnabled(false);

        com.example.midtermproject.data.database.AppDatabase.databaseExecutor.execute(() -> {
            UserEntity user = userRepository.getUserByIdSync(userId);
            runOnUiThread(() -> {
                binding.fabAdopt.setEnabled(true);
                if (user == null) return;
                if (user.isProfileComplete()) {
                    showAdoptionBottomSheet();
                } else {
                    showIncompleteProfileDialog(user);
                }
            });
        });
    }

    private void showAdoptionBottomSheet() {
        new AdoptionBottomSheet(petId,
            () -> {
                Snackbar.make(binding.getRoot(), "Adoption request sent successfully!", Snackbar.LENGTH_SHORT).show();
                finish();
            },
            () -> Snackbar.make(binding.getRoot(), "Adoption request canceled", Snackbar.LENGTH_SHORT).show()
        ).show(getSupportFragmentManager(), "AdoptionBottomSheet");
    }

    private void showIncompleteProfileDialog(UserEntity user) {
        java.util.List<String> missing = user.getMissingProfileFields();
        StringBuilder sb = new StringBuilder("Complete the following:\n");
        for (String field : missing) sb.append("\n  •  ").append(field);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Incomplete Adopter Profile")
            .setMessage(sb.toString())
            .setPositiveButton("Complete Now", (d, w) ->
                new EditProfileBottomSheet(() -> binding.fabAdopt.performClick())
                    .show(getSupportFragmentManager(), "EditProfileBottomSheet"))
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ==================== Setup ====================

    private void setupImagePager() {
        imagePagerAdapter = new ImagePagerAdapter();
        binding.vpPetImages.setAdapter(imagePagerAdapter);

        // Pre-populate images and dots from intent immediately before DB loads
        imagePagerAdapter.setImages(petImageResId, petImageResIds);
        updateDots(0);

        binding.vpPetImages.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void updateDots(int selectedPosition) {
        binding.dotsIndicator.removeAllViews();
        int count = imagePagerAdapter.getRealCount();
        if (count <= 1) {
            binding.dotsIndicator.setVisibility(View.GONE);
            return;
        }
        binding.dotsIndicator.setVisibility(View.VISIBLE);

        float density = getResources().getDisplayMetrics().density;
        int inactiveSize = (int) (8 * density);
        int activeWidth = (int) (24 * density);
        int activeHeight = (int) (8 * density);
        int dotMargin = (int) (6 * density);

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            if (i == selectedPosition) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(activeWidth, activeHeight);
                params.setMargins(dotMargin, 0, dotMargin, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.bg_dot_active);
            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(inactiveSize, inactiveSize);
                params.setMargins(dotMargin, 0, dotMargin, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.bg_dot);
                dot.setAlpha(0.6f);
            }
            binding.dotsIndicator.addView(dot);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadPetDetails() {
        petRepository.getPetById(petId).observe(this, pet -> {
            if (pet != null) {
                currentPet = pet;
                binding.tvName.setText(pet.getName());
                binding.tvBreed.setText(pet.getBreed());
                binding.tvAge.setText(pet.getAge());
                setScaledDrawable(binding.tvAge, R.drawable.ic_history);

                binding.tvColor.setText(pet.getColor() != null && !pet.getColor().isEmpty() ? pet.getColor() : "N/A");
                setScaledDrawable(binding.tvColor, R.drawable.ic_favorite);

                binding.tvGender.setText(pet.getGender());
                setScaledDrawable(binding.tvGender, "Female".equalsIgnoreCase(pet.getGender()) ? R.drawable.ic_female : R.drawable.ic_male);

                binding.tvSize.setText(pet.getSize());
                setScaledDrawable(binding.tvSize, R.drawable.ic_pets);
                binding.tvDescription.setText(pet.getDescription());

                imagePagerAdapter.setPet(pet);
                updateDots(0);

                if (!"AVAILABLE".equals(pet.getStatus())) {
                    binding.fabAdopt.setEnabled(false);
                    binding.fabAdopt.setText(pet.getStatus());
                    binding.fabAdopt.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.divider)));
                }

                updateFavoriteIcon();
            }
        });
    }

    private void updateFavoriteIcon() {
        boolean isFav = favoriteManager.isFavorite(petId);
        binding.btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        binding.btnFavorite.setColorFilter(isFav ? getColor(R.color.error) : getColor(R.color.text_primary));
    }

    private void setScaledDrawable(android.widget.TextView textView, int drawableId) {
        android.graphics.drawable.Drawable drawable = androidx.core.content.ContextCompat.getDrawable(this, drawableId);
        if (drawable != null) {
            android.graphics.drawable.Drawable wrappedDrawable = androidx.core.graphics.drawable.DrawableCompat.wrap(drawable).mutate();
            float density = getResources().getDisplayMetrics().density;
            int size = (int) (14 * density);
            wrappedDrawable.setBounds(0, 0, size, size);
            androidx.core.graphics.drawable.DrawableCompat.setTint(wrappedDrawable, getColor(R.color.primary));
            textView.setCompoundDrawablesRelative(wrappedDrawable, null, null, null);
        }
    }
}
