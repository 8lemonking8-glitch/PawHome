package com.example.midtermproject.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.databinding.BottomSheetEditProfileBinding;
import com.example.midtermproject.util.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.concurrent.Executors;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetEditProfileBinding binding;
    private SessionManager sessionManager;
    private UserRepository userRepository;
    private UserEntity currentUser;
    private final Runnable onProfileUpdated;

    public EditProfileBottomSheet(Runnable onProfileUpdated) {
        this.onProfileUpdated = onProfileUpdated;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        userRepository = new UserRepository(requireActivity().getApplication());

        loadCurrentUserData();

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentUserData() {
        long userId = sessionManager.getUserId();
        userRepository.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                binding.etNickname.setText(user.getNickname());
                binding.etEmail.setText(user.getEmail());
                binding.etPhone.setText(user.getPhone());
            }
        });
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String nickname = binding.etNickname.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();

        currentUser.setNickname(nickname);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        binding.btnSave.setEnabled(false);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            userRepository.update(currentUser);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    sessionManager.createSession(currentUser.getId(), currentUser.getUsername(), currentUser.getRole(), currentUser.getNickname());
                    if (onProfileUpdated != null) {
                        onProfileUpdated.run();
                    }
                    dismiss();
                });
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
