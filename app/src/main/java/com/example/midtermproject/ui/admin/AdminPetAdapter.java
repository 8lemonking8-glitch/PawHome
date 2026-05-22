package com.example.midtermproject.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.databinding.ItemAdminPetCardBinding;
import java.util.ArrayList;
import java.util.List;

public class AdminPetAdapter extends RecyclerView.Adapter<AdminPetAdapter.ViewHolder> {

    public interface OnPetActionListener {
        void onEditClick(PetEntity pet);
    }

    private List<PetEntity> pets = new ArrayList<>();
    private OnPetActionListener listener;

    public void setOnPetActionListener(OnPetActionListener listener) {
        this.listener = listener;
    }

    public void setPets(List<PetEntity> pets) {
        this.pets = pets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminPetCardBinding binding = ItemAdminPetCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(pets.get(position));
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminPetCardBinding binding;

        public ViewHolder(ItemAdminPetCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(pets.get(pos));
                }
            });
        }

        public void bind(PetEntity pet) {
            binding.tvName.setText(pet.getName());
            binding.tvDetails.setText(pet.getType() + " • " + pet.getBreed());
            binding.tvStatus.setText(pet.getStatus());
            
            if ("AVAILABLE".equals(pet.getStatus())) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
            } else {
                binding.tvStatus.setBackgroundColor(itemView.getContext().getColor(R.color.text_hint));
            }
            
            if (pet.getImageResId() != 0) {
                binding.ivIcon.setImageResource(pet.getImageResId());
                binding.ivIcon.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                binding.ivIcon.setPadding(0, 0, 0, 0);
            } else if (pet.getImageResIds() != null && pet.getImageResIds().length() > 2) {
                try {
                    org.json.JSONArray array = new org.json.JSONArray(pet.getImageResIds());
                    if (array.length() > 0) {
                        String uriStr = array.getString(0);
                        binding.ivIcon.setImageURI(android.net.Uri.parse(uriStr));
                        binding.ivIcon.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        binding.ivIcon.setPadding(0, 0, 0, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if ("DOG".equals(pet.getType())) {
                    binding.ivIcon.setBackgroundColor(0xFFE8734A); 
                } else if ("CAT".equals(pet.getType())) {
                    binding.ivIcon.setBackgroundColor(0xFFA78BDB); 
                } else {
                    binding.ivIcon.setBackgroundColor(0xFF5CB8A5); 
                }
                binding.ivIcon.setImageResource(R.drawable.ic_pets);
                binding.ivIcon.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            }
        }
    }
}
