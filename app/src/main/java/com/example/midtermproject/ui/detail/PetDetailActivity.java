package com.example.midtermproject.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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
    private long petId;
    private PetEntity currentPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPetDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        petId = getIntent().getLongExtra(EXTRA_PET_ID, -1);
        if (petId == -1) {
            finish();
            return;
        }

        petRepository = new PetRepository(getApplication());
        favoriteManager = new FavoriteManager(this);

        setupToolbar();
        loadPetDetails();
        
        binding.fabAdopt.setOnClickListener(v -> {
            if (currentPet != null && "AVAILABLE".equals(currentPet.getStatus())) {
                AdoptionBottomSheet bottomSheet = new AdoptionBottomSheet(petId, () -> {
                    Toast.makeText(this, "Adoption request sent successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
                bottomSheet.show(getSupportFragmentManager(), "AdoptionBottomSheet");
            } else {
                Toast.makeText(this, "Pet is no longer available", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnFavorite.setOnClickListener(v -> {
            favoriteManager.toggleFavorite(petId);
            updateFavoriteIcon();
        });
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
                binding.tvGender.setText(pet.getGender());
                binding.tvSize.setText(pet.getSize());
                binding.tvDescription.setText(pet.getDescription());
                
                binding.collapsingToolbar.setTitle(pet.getName());
                
                // Color coding placeholder
                if (pet.getImageResId() != 0) {
                    binding.ivPetImage.setImageResource(pet.getImageResId());
                    binding.ivPetImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                } else if (pet.getImageResIds() != null && pet.getImageResIds().length() > 2) {
                    try {
                        org.json.JSONArray array = new org.json.JSONArray(pet.getImageResIds());
                        if (array.length() > 0) {
                            String uriStr = array.getString(0);
                            binding.ivPetImage.setImageURI(android.net.Uri.parse(uriStr));
                            binding.ivPetImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if ("DOG".equals(pet.getType())) {
                        binding.ivPetImage.setBackgroundColor(0xFFE8734A); 
                    } else if ("CAT".equals(pet.getType())) {
                        binding.ivPetImage.setBackgroundColor(0xFFA78BDB); 
                    } else {
                        binding.ivPetImage.setBackgroundColor(0xFF5CB8A5); 
                    }
                }
                
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
