package com.example.midtermproject.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.graphics.Insets;

import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.databinding.ActivityPetDetailBinding;
import com.example.midtermproject.util.FavoriteManager;
import com.example.midtermproject.util.SessionManager;
import com.example.midtermproject.ui.profile.EditProfileBottomSheet;
import com.example.midtermproject.data.database.AppDatabase;

public class PetDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PET_ID = "extra_pet_id";

    private ActivityPetDetailBinding binding;
    private PetRepository petRepository;
    private FavoriteManager favoriteManager;
    private ImagePagerAdapter imagePagerAdapter;
    private long petId;
    private PetEntity currentPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Smooth shared-element transition with corner-radius interpolation
        com.google.android.material.transition.platform.MaterialContainerTransform transform =
            new com.google.android.material.transition.platform.MaterialContainerTransform();
        transform.setDuration(350);
        transform.setScrimColor(android.graphics.Color.TRANSPARENT);
        transform.setAllContainerColors(getColor(com.google.android.material.R.attr.colorSurface));
        getWindow().setSharedElementEnterTransition(transform);
        getWindow().setSharedElementReturnTransition(transform);

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

        petRepository = new PetRepository(getApplication());
        favoriteManager = new FavoriteManager(this);

        setupToolbar();
        setupImagePager();
        loadPetDetails();

        binding.fabAdopt.setOnClickListener(v -> handleAdoptClick());

        binding.btnFavorite.setOnClickListener(v -> {
            favoriteManager.toggleFavorite(petId);
            updateFavoriteIcon();
            binding.btnFavorite.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_bounce));
        });

        postponeEnterTransition();
    }

    private void handleAdoptClick() {
        if (currentPet == null || !"AVAILABLE".equals(currentPet.getStatus())) {
            Snackbar.make(binding.getRoot(), "Pet is no longer available", Snackbar.LENGTH_SHORT).show();
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();
        UserRepository userRepository = new UserRepository(getApplication());
        binding.fabAdopt.setEnabled(false);

        new Thread(() -> {
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
        }).start();
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

    private void setupImagePager() {
        imagePagerAdapter = new ImagePagerAdapter();
        binding.vpPetImages.setAdapter(imagePagerAdapter);

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
        int inactiveSize = (int) (6 * density);
        int activeWidth = (int) (16 * density);
        int activeHeight = (int) (6 * density);
        int dotMargin = (int) (4 * density);

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
                dot.setAlpha(0.4f);
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

                // Set transition name on the first image view
                binding.vpPetImages.post(() -> {
                    RecyclerView rv = (RecyclerView) binding.vpPetImages.getChildAt(0);
                    if (rv != null && rv.getChildAt(0) != null) {
                        rv.getChildAt(0).setTransitionName("pet_image_" + petId);
                        startPostponedEnterTransition();
                    }
                });

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
            int size = (int) (14 * density); // 14dp size, perfect fit for 12sp Poppins text
            wrappedDrawable.setBounds(0, 0, size, size);
            androidx.core.graphics.drawable.DrawableCompat.setTint(wrappedDrawable, getColor(R.color.primary));
            textView.setCompoundDrawablesRelative(wrappedDrawable, null, null, null);
        }
    }
}
