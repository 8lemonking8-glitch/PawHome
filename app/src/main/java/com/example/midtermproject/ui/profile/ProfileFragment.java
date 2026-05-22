package com.example.midtermproject.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.data.repository.AdoptionRepository;
import com.example.midtermproject.databinding.FragmentProfileBinding;
import com.example.midtermproject.ui.auth.AuthActivity;
import com.example.midtermproject.util.SessionManager;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private UserRepository userRepository;
    private AdoptionRepository adoptionRepository;
    private AdoptionHistoryAdapter historyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        float density = getResources().getDisplayMetrics().density;
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int extraPadding = (int) (12 * density); // Add extra padding for elegant layout breathing space
        int defaultTopPadding = statusBarHeight > 0 ? (statusBarHeight + extraPadding) : (int) (44 * density);
        binding.appBarLayout.setPadding(
            binding.appBarLayout.getPaddingLeft(),
            defaultTopPadding,
            binding.appBarLayout.getPaddingRight(),
            binding.appBarLayout.getPaddingBottom()
        );

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int topInset = insets.top;
            if (topInset > 0) {
                v.setPadding(
                    v.getPaddingLeft(),
                    topInset + extraPadding,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
                );
            }
            return windowInsets;
        });

        sessionManager = new SessionManager(requireContext());
        userRepository = new UserRepository(requireActivity().getApplication());
        adoptionRepository = new AdoptionRepository(requireActivity().getApplication());

        // Setup RecyclerView
        historyAdapter = new AdoptionHistoryAdapter();
        binding.rvAdoptionHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdoptionHistory.setAdapter(historyAdapter);

        loadUserProfile();
        loadAdoptionHistory();

        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            EditProfileBottomSheet bottomSheet = new EditProfileBottomSheet(() -> loadUserProfile());
            bottomSheet.show(getChildFragmentManager(), "EditProfileBottomSheet");
        });
    }

    private void loadUserProfile() {
        long userId = sessionManager.getUserId();
        userRepository.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvNickname.setText(user.getNickname() != null && !user.getNickname().isEmpty() ? user.getNickname() : user.getUsername());
                binding.tvUsername.setText("@" + user.getUsername());
                
                binding.tvEmail.setText(user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : "Not provided");
                binding.tvPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Not provided");
            }
        });
    }

    private void loadAdoptionHistory() {
        long userId = sessionManager.getUserId();
        adoptionRepository.getRequestsByUserWithDetails(userId).observe(getViewLifecycleOwner(), requests -> {
            if (requests == null || requests.isEmpty()) {
                binding.rvAdoptionHistory.setVisibility(View.GONE);
                binding.cardEmptyHistory.setVisibility(View.VISIBLE);
            } else {
                binding.cardEmptyHistory.setVisibility(View.GONE);
                binding.rvAdoptionHistory.setVisibility(View.VISIBLE);
                historyAdapter.setRequests(requests);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
