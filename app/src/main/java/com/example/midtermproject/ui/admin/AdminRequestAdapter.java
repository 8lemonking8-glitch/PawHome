package com.example.midtermproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.AdoptionRequestEntity;
import com.example.midtermproject.data.entity.AdoptionRequestWithDetails;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.databinding.ItemAdminRequestCardBinding;
import java.util.ArrayList;
import java.util.List;

public class AdminRequestAdapter extends RecyclerView.Adapter<AdminRequestAdapter.ViewHolder> {

    private List<AdoptionRequestWithDetails> requests = new ArrayList<>();
    private final OnRequestAction onApprove;
    private final OnRequestAction onReject;

    public interface OnRequestAction {
        void onAction(AdoptionRequestEntity request);
    }

    public AdminRequestAdapter(OnRequestAction onApprove, OnRequestAction onReject) {
        this.onApprove = onApprove;
        this.onReject = onReject;
    }

    public void setRequests(List<AdoptionRequestWithDetails> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminRequestCardBinding binding = ItemAdminRequestCardBinding.inflate(
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
        private final ItemAdminRequestCardBinding binding;

        public ViewHolder(ItemAdminRequestCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            binding.btnApprove.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onApprove.onAction(requests.get(pos).request);
                }
            });
            
            binding.btnReject.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onReject.onAction(requests.get(pos).request);
                }
            });
        }

        public void bind(AdoptionRequestWithDetails details) {
            AdoptionRequestEntity request = details.request;
            UserEntity user = details.user;
            PetEntity pet = details.pet;
            
            String contact = user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : user.getEmail();
            if (contact == null || contact.isEmpty()) contact = "No contact info";
            
            binding.tvUserName.setText(user.getUsername() + " (" + contact + ")");
            binding.tvPetName.setText(pet.getName() + " (" + pet.getBreed() + ")");
            binding.tvStatus.setText(request.getStatus());
            
            if ("PENDING".equals(request.getStatus())) {
                binding.layoutActions.setVisibility(View.VISIBLE);
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
            } else {
                binding.layoutActions.setVisibility(View.GONE);
                if ("APPROVED".equals(request.getStatus())) {
                    binding.tvStatus.setBackgroundColor(0xFF4CAF50);
                } else {
                    binding.tvStatus.setBackgroundColor(0xFFF44336);
                }
            }
        }
    }
}
