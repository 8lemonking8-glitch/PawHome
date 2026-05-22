package com.example.midtermproject.ui.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.AdoptionRequestWithDetails;
import com.example.midtermproject.data.repository.AdoptionRepository;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.databinding.FragmentAdminDashboardBinding;
import com.example.midtermproject.ui.auth.AuthActivity;
import com.example.midtermproject.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private PetRepository petRepository;
    private AdoptionRepository adoptionRepository;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        petRepository = new PetRepository(requireActivity().getApplication());
        adoptionRepository = new AdoptionRepository(requireActivity().getApplication());
        userRepository = new UserRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnQuickAddPet.setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).switchToTab(R.id.nav_pets);
            }
        });

        binding.btnQuickManageRequests.setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).switchToTab(R.id.nav_requests);
            }
        });

        loadStatistics();
        loadRecentActivities();
    }

    private void loadStatistics() {
        petRepository.getTotalPetCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvTotalPets.setText(String.valueOf(count != null ? count : 0));
        });

        petRepository.getAvailablePetCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvAvailablePets.setText(String.valueOf(count != null ? count : 0));
        });

        petRepository.getAdoptedPetCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvAdoptedPets.setText(String.valueOf(count != null ? count : 0));
        });

        adoptionRepository.getPendingCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvPendingRequests.setText(String.valueOf(count != null ? count : 0));
        });

        adoptionRepository.getApprovedCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvApprovedRequests.setText(String.valueOf(count != null ? count : 0));
        });

        userRepository.getUserCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvTotalAdopters.setText(String.valueOf(count != null ? count : 0));
        });
    }

    private void loadRecentActivities() {
        adoptionRepository.getAllRequestsWithDetails().observe(getViewLifecycleOwner(), requests -> {
            binding.layoutRecentActivities.removeAllViews();

            if (requests == null || requests.isEmpty()) {
                binding.tvNoActivities.setVisibility(View.VISIBLE);
                return;
            }
            binding.tvNoActivities.setVisibility(View.GONE);

            int shown = 0;
            for (int i = requests.size() - 1; i >= 0 && shown < 5; i--) {
                AdoptionRequestWithDetails req = requests.get(i);
                View itemView = createActivityItem(req);
                if (itemView != null) {
                    binding.layoutRecentActivities.addView(itemView);
                    shown++;
                }
            }
        });
    }

    private View createActivityItem(AdoptionRequestWithDetails req) {
        float density = getResources().getDisplayMetrics().density;

        String petName = req.pet != null && req.pet.getName() != null ? req.pet.getName() : "Pet";
        String petBreed = req.pet != null && req.pet.getBreed() != null ? req.pet.getBreed() : "";
        String petType = req.pet != null && req.pet.getType() != null ? req.pet.getType() : "";
        String username = req.user != null && req.user.getUsername() != null ? req.user.getUsername() : "User";
        String status = req.request != null && req.request.getStatus() != null ? req.request.getStatus() : "PENDING";

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin = (int) (16 * density);
        row.setLayoutParams(rowParams);

        CircleImageView avatar = new CircleImageView(requireContext());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(
                (int) (40 * density), (int) (40 * density));
        avatar.setLayoutParams(avatarParams);
        avatar.setBorderWidth((int) (1 * density));
        avatar.setBorderColor(getResources().getColor(R.color.divider));
        switch (petType) {
            case "DOG": avatar.setImageResource(R.drawable.img_dog); break;
            case "CAT": avatar.setImageResource(R.drawable.img_cat); break;
            default: avatar.setImageResource(R.drawable.img_bird); break;
        }
        row.addView(avatar);

        LinearLayout textCol = new LinearLayout(requireContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textColParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        textColParams.setMargins((int) (12 * density), 0, 0, 0);
        textCol.setLayoutParams(textColParams);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(petName + " (" + petBreed + ")");
        tvTitle.setTextColor(getResources().getColor(R.color.text_primary));
        tvTitle.setTextSize(14);
        tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);
        textCol.addView(tvTitle);

        TextView tvSubtitle = new TextView(requireContext());
        tvSubtitle.setText("Applied by " + username);
        tvSubtitle.setTextColor(getResources().getColor(R.color.text_secondary));
        tvSubtitle.setTextSize(11);
        textCol.addView(tvSubtitle);

        row.addView(textCol);

        TextView tvStatus = new TextView(requireContext());
        tvStatus.setText(status);
        tvStatus.setTextSize(10);
        tvStatus.setTypeface(tvStatus.getTypeface(), android.graphics.Typeface.BOLD);
        tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
        tvStatus.setPadding((int) (8 * density), (int) (4 * density), (int) (8 * density), (int) (4 * density));

        android.content.Context ctx = requireContext();
        switch (status) {
            case "APPROVED":
                tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.on_success));
                tvStatus.getBackground().mutate().setTint(androidx.core.content.ContextCompat.getColor(ctx, R.color.status_available));
                break;
            case "REJECTED":
                tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.on_error));
                tvStatus.getBackground().mutate().setTint(androidx.core.content.ContextCompat.getColor(ctx, R.color.error));
                break;
            default:
                tvStatus.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.on_secondary));
                tvStatus.getBackground().mutate().setTint(androidx.core.content.ContextCompat.getColor(ctx, R.color.status_pending));
                break;
        }
        row.addView(tvStatus);

        return row;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
