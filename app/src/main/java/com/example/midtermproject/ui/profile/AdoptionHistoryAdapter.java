package com.example.midtermproject.ui.profile;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.AdoptionRequestEntity;
import com.example.midtermproject.data.entity.AdoptionRequestWithDetails;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.databinding.ItemAdoptionHistoryBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdoptionHistoryAdapter extends RecyclerView.Adapter<AdoptionHistoryAdapter.ViewHolder> {

    private List<AdoptionRequestWithDetails> requests = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public void setRequests(List<AdoptionRequestWithDetails> newRequests) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return requests.size();
            }

            @Override
            public int getNewListSize() {
                return newRequests.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return requests.get(oldItemPosition).request.getId() == newRequests.get(newItemPosition).request.getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                AdoptionRequestEntity oldReq = requests.get(oldItemPosition).request;
                AdoptionRequestEntity newReq = newRequests.get(newItemPosition).request;
                return oldReq.getStatus().equals(newReq.getStatus()) &&
                       oldReq.getCreatedAt() == newReq.getCreatedAt();
            }
        });

        this.requests = newRequests;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdoptionHistoryBinding binding = ItemAdoptionHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(requests.get(position));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdoptionHistoryBinding binding;

        public ViewHolder(ItemAdoptionHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AdoptionRequestWithDetails details) {
            AdoptionRequestEntity request = details.request;
            PetEntity pet = details.pet;

            if (pet != null) {
                binding.tvPetName.setText(pet.getName());
                binding.tvPetBreed.setText(pet.getBreed());

                if (!com.example.midtermproject.util.PetImageUtils.loadFirstImage(binding.ivPetImage, pet)) {
                    if ("DOG".equals(pet.getType())) {
                        binding.ivPetImage.setBackgroundColor(0xFFE8734A); 
                    } else if ("CAT".equals(pet.getType())) {
                        binding.ivPetImage.setBackgroundColor(0xFFA78BDB); 
                    } else {
                        binding.ivPetImage.setBackgroundColor(0xFF5CB8A5); 
                    }
                }
            } else {
                binding.tvPetName.setText("Unknown Pet");
                binding.tvPetBreed.setText("");
                binding.ivPetImage.setImageResource(R.drawable.ic_pets);
            }

            binding.tvRequestDate.setText("Requested: " + dateFormat.format(new Date(request.getCreatedAt())));

            binding.tvStatus.setText(request.getStatus());
            int tintColor;
            if ("PENDING".equals(request.getStatus())) {
                tintColor = itemView.getContext().getColor(R.color.status_pending);
            } else if ("APPROVED".equals(request.getStatus())) {
                tintColor = itemView.getContext().getColor(R.color.status_available);
            } else { 
                tintColor = itemView.getContext().getColor(R.color.status_rejected);
            }
            ViewCompat.setBackgroundTintList(binding.tvStatus, ColorStateList.valueOf(tintColor));
        }
    }
}
