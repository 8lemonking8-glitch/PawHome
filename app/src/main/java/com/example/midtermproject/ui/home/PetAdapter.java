package com.example.midtermproject.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.databinding.ItemPetCardBinding;
import com.example.midtermproject.util.FavoriteManager;
import java.util.ArrayList;
import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<PetEntity> pets = new ArrayList<>();
    private final OnPetClickListener listener;
    private FavoriteManager favoriteManager;

    public interface OnPetClickListener {
        void onPetClick(PetEntity pet, View sharedImageView);
    }

    public PetAdapter(OnPetClickListener listener) {
        this.listener = listener;
    }
    
    public void setFavoriteManager(FavoriteManager favoriteManager) {
        this.favoriteManager = favoriteManager;
    }

    public void setPets(List<PetEntity> newPets) {
        this.pets = newPets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPetCardBinding binding = ItemPetCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        PetEntity pet = pets.get(position);
        
        // Apply deterministic random height for staggered grid
        int randomHeight = 180 + new java.util.Random(pet.getId()).nextInt(80);
        ViewGroup.LayoutParams layoutParams = holder.binding.ivPetImage.getLayoutParams();
        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;
        layoutParams.height = (int) (randomHeight * density);
        holder.binding.ivPetImage.setLayoutParams(layoutParams);
        
        holder.bind(pet);
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    class PetViewHolder extends RecyclerView.ViewHolder {
        private final ItemPetCardBinding binding;

        public PetViewHolder(ItemPetCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onPetClick(pets.get(pos), binding.ivPetImage);
                }
            });
            
            binding.btnAdopt.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onPetClick(pets.get(pos), binding.ivPetImage);
                }
            });
            
            binding.btnFavorite.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && favoriteManager != null) {
                    PetEntity pet = pets.get(pos);
                    favoriteManager.toggleFavorite(pet.getId());
                    // Update visual state locally
                    boolean isFav = favoriteManager.isFavorite(pet.getId());
                    binding.btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                    binding.btnFavorite.setColorFilter(isFav ? itemView.getContext().getColor(R.color.error) : itemView.getContext().getColor(R.color.text_primary));
                    // Bounce Animation
                    binding.btnFavorite.startAnimation(android.view.animation.AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_bounce));
                }
            });
        }

        public void bind(PetEntity pet) {
            binding.ivPetImage.setTransitionName("pet_image_" + pet.getId());
            binding.tvName.setText(pet.getName());
            binding.tvBreed.setText(pet.getBreed());
            binding.tvStatus.setText(pet.getStatus());
            
            if ("Female".equalsIgnoreCase(pet.getGender())) {
                binding.ivGender.setImageResource(R.drawable.ic_female);
                binding.ivGender.setColorFilter(itemView.getContext().getColor(R.color.error)); // pink-ish
            } else {
                binding.ivGender.setImageResource(R.drawable.ic_male);
                binding.ivGender.setColorFilter(itemView.getContext().getColor(R.color.info)); // blue-ish
            }
            
            if (!com.example.midtermproject.util.PetImageUtils.loadFirstImage(binding.ivPetImage, pet)) {
                if ("DOG".equals(pet.getType())) {
                    binding.ivPetImage.setBackgroundColor(0xFFE8734A); 
                } else if ("CAT".equals(pet.getType())) {
                    binding.ivPetImage.setBackgroundColor(0xFFA78BDB); 
                } else {
                    binding.ivPetImage.setBackgroundColor(0xFF5CB8A5); 
                }
            }
            
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
            int color = "AVAILABLE".equals(pet.getStatus()) ? 
                itemView.getContext().getColor(R.color.status_available) : 
                itemView.getContext().getColor(R.color.text_hint);
            binding.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
            
            // Set initial favorite state
            if (favoriteManager != null) {
                boolean isFav = favoriteManager.isFavorite(pet.getId());
                binding.btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                binding.btnFavorite.setColorFilter(isFav ? itemView.getContext().getColor(R.color.error) : itemView.getContext().getColor(R.color.text_primary));
            }
        }
    }
}
