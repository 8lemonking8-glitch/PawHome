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
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.databinding.ActivityPetDetailBinding;
import com.example.midtermproject.util.FavoriteManager;

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

        binding.fabAdopt.setOnClickListener(v -> {
            if (currentPet != null && "AVAILABLE".equals(currentPet.getStatus())) {
                AdoptionBottomSheet bottomSheet = new AdoptionBottomSheet(petId,
                    () -> {
                        Snackbar.make(binding.getRoot(), "Adoption request sent successfully!", Snackbar.LENGTH_SHORT).show();
                        finish();
                    },
                    () -> finish()
                );
                bottomSheet.show(getSupportFragmentManager(), "AdoptionBottomSheet");
            } else {
                Snackbar.make(binding.getRoot(), "Pet is no longer available", Snackbar.LENGTH_SHORT).show();
            }
        });

        binding.btnFavorite.setOnClickListener(v -> {
            favoriteManager.toggleFavorite(petId);
            updateFavoriteIcon();
            binding.btnFavorite.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_bounce));
        });

        postponeEnterTransition();
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
        if (count <= 1) return;

        float density = getResources().getDisplayMetrics().density;
        int dotSize = (int) (8 * density);
        int dotMargin = (int) (4 * density);

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotMargin, 0, dotMargin, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_dot);
            if (i == selectedPosition) {
                dot.setAlpha(1.0f);
            } else {
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
                binding.tvColor.setText(pet.getColor() != null && !pet.getColor().isEmpty() ? pet.getColor() : "N/A");
                binding.tvGender.setText(pet.getGender());
                if ("Female".equalsIgnoreCase(pet.getGender())) {
                    binding.tvGender.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_female, 0, 0, 0);
                } else {
                    binding.tvGender.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_male, 0, 0, 0);
                }
                binding.tvSize.setText(pet.getSize());
                binding.tvDescription.setText(pet.getDescription());

                binding.collapsingToolbar.setTitle(pet.getName());

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
}
