package com.example.midtermproject.ui.admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.AdoptionRequestEntity;
import com.example.midtermproject.data.entity.AdoptionRequestWithDetails;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.databinding.ItemAdminRequestCardBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

            binding.btnViewProfile.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    showApplicantProfile(v.getContext(), requests.get(pos));
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

            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
            int color;
            if ("PENDING".equals(request.getStatus())) {
                binding.btnApprove.setVisibility(View.VISIBLE);
                binding.btnReject.setVisibility(View.VISIBLE);
                color = itemView.getContext().getColor(R.color.status_pending);
            } else {
                binding.btnApprove.setVisibility(View.GONE);
                binding.btnReject.setVisibility(View.GONE);
                if ("APPROVED".equals(request.getStatus())) {
                    color = itemView.getContext().getColor(R.color.status_available);
                } else {
                    color = itemView.getContext().getColor(R.color.status_rejected);
                }
            }
            binding.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

            // View Profile button is always visible
            binding.btnViewProfile.setVisibility(View.VISIBLE);
        }

        private void showApplicantProfile(Context context, AdoptionRequestWithDetails details) {
            UserEntity user = details.user;
            AdoptionRequestEntity request = details.request;

            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_applicant_profile, null);

            // Avatar
            de.hdodenhof.circleimageview.CircleImageView ivAvatar =
                dialogView.findViewById(R.id.ivDialogAvatar);
            String avatarUri = user.getAvatarUri();
            if (avatarUri != null && !avatarUri.isEmpty()) {
                ivAvatar.setImageURI(android.net.Uri.parse(avatarUri));
            } else if (user.getAvatarResId() != 0) {
                ivAvatar.setImageResource(user.getAvatarResId());
            }

            // Populate user basic info
            TextView tvName = dialogView.findViewById(R.id.tvDialogUserName);
            TextView tvContact = dialogView.findViewById(R.id.tvDialogUserContact);
            String displayName = user.getNickname() != null && !user.getNickname().isEmpty()
                    ? user.getNickname() : user.getUsername();
            tvName.setText(displayName);

            StringBuilder contactInfo = new StringBuilder();
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                contactInfo.append(user.getEmail());
            }
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                if (contactInfo.length() > 0) contactInfo.append(" · ");
                contactInfo.append(user.getPhone());
            }
            tvContact.setText(contactInfo.length() > 0 ? contactInfo.toString() : "No contact info");

            // Personal details
            TextView tvGender = dialogView.findViewById(R.id.tvDialogGender);
            TextView tvAge = dialogView.findViewById(R.id.tvDialogAge);
            TextView tvAddress = dialogView.findViewById(R.id.tvDialogAddress);
            tvGender.setText(user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "Not provided");
            tvAge.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "Not provided");
            tvAddress.setText(user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Not provided");

            // Living conditions
            TextView tvHousing = dialogView.findViewById(R.id.tvDialogHousing);
            TextView tvIncome = dialogView.findViewById(R.id.tvDialogIncome);
            tvHousing.setText(user.getHousingCondition() != null && !user.getHousingCondition().isEmpty()
                    ? user.getHousingCondition() : "Not provided");
            tvIncome.setText(user.getMonthlyIncome() != null && !user.getMonthlyIncome().isEmpty()
                    ? user.getMonthlyIncome() : "Not provided");

            // Pet experience
            TextView tvPetExp = dialogView.findViewById(R.id.tvDialogPetExperience);
            tvPetExp.setText(user.getPetExperience() != null && !user.getPetExperience().isEmpty()
                    ? user.getPetExperience() : "Not provided");

            // Signature image
            ImageView ivSignature = dialogView.findViewById(R.id.ivDialogSignature);
            TextView tvSignatureDate = dialogView.findViewById(R.id.tvDialogSignatureDate);

            String sigPath = request.getSignaturePath();
            if (sigPath != null && !sigPath.isEmpty()) {
                File sigFile = new File(sigPath);
                if (sigFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(sigPath);
                    if (bitmap != null) {
                        ivSignature.setImageBitmap(bitmap);
                    }
                }
            }

            if (request.getSignatureTimestamp() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                tvSignatureDate.setText("Signed: " + sdf.format(new Date(request.getSignatureTimestamp())));
            } else {
                tvSignatureDate.setText("Signed: N/A");
            }

            new MaterialAlertDialogBuilder(context)
                    .setTitle("Applicant Profile")
                    .setView(dialogView)
                    .setPositiveButton("Close", null)
                    .show();
        }
    }
}
