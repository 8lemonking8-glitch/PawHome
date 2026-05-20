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
import java.util.Random;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<PetEntity> pets = new ArrayList<>();
    private final OnPetClickListener listener;
    private final Random random = new Random();
    private final List<Integer> heightList = new ArrayList<>();
    private FavoriteManager favoriteManager;

    public interface OnPetClickListener {
        void onPetClick(PetEntity pet);
    }

    public PetAdapter(OnPetClickListener listener) {
        this.listener = listener;
    }
    
    public void setFavoriteManager(FavoriteManager favoriteManager) {
        this.favoriteManager = favoriteManager;
    }

    public void setPets(List<PetEntity> pets) {
        this.pets = pets;
        
        // Generate random heights for staggered grid effect
        heightList.clear();
        for (int i = 0; i < pets.size(); i++) {
            heightList.add(160 + random.nextInt(80)); // 160dp to 240dp
        }
        
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
        
        // Apply random height for staggered grid
        ViewGroup.LayoutParams layoutParams = holder.binding.ivPetImage.getLayoutParams();
        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;
        layoutParams.height = (int) (heightList.get(position) * density);
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
                    listener.onPetClick(pets.get(pos));
                }
            });
            
            binding.btnAdopt.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onPetClick(pets.get(pos));
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
                }
            });
        }

        public void bind(PetEntity pet) {
            binding.tvName.setText(pet.getName());
            binding.tvBreed.setText(pet.getBreed());
            binding.tvStatus.setText(pet.getStatus());
            
            if ("Female".equalsIgnoreCase(pet.getGender())) {
                binding.ivGender.setColorFilter(itemView.getContext().getColor(R.color.error)); // pink-ish
            } else {
                binding.ivGender.setColorFilter(itemView.getContext().getColor(R.color.info)); // blue-ish
            }
            
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
            
            if ("AVAILABLE".equals(pet.getStatus())) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_badge); 
            } else {
                binding.tvStatus.setBackgroundColor(itemView.getContext().getColor(R.color.text_hint));
            }
            
            // Set initial favorite state
            if (favoriteManager != null) {
                boolean isFav = favoriteManager.isFavorite(pet.getId());
                binding.btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                binding.btnFavorite.setColorFilter(isFav ? itemView.getContext().getColor(R.color.error) : itemView.getContext().getColor(R.color.text_primary));
            }
        }
    }
}
