package com.example.midtermproject.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.databinding.FragmentProfileBinding;
import com.example.midtermproject.ui.auth.AuthActivity;
import com.example.midtermproject.util.SessionManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        userRepository = new UserRepository(requireActivity().getApplication());

        loadUserProfile();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
