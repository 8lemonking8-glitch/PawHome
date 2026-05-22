package com.example.midtermproject.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.databinding.BottomSheetEditProfileBinding;
import com.example.midtermproject.util.SessionManager;
import com.example.midtermproject.data.database.AppDatabase;
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

        binding.tilNickname.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);

        String nickname = binding.etNickname.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();

        boolean valid = true;

        if (nickname.isEmpty()) {
            binding.tilNickname.setError("Nickname cannot be empty");
            valid = false;
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email cannot be empty");
            valid = false;
        } else if (!email.contains("@")) {
            binding.tilEmail.setError("Invalid email format (must contain '@')");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Invalid email address format");
            valid = false;
        }

        if (!phone.isEmpty()) {
            if (!phone.matches("^[0-9+\\-]+$")) {
                binding.tilPhone.setError("Phone must contain only numbers, +, and -");
                valid = false;
            }
        }

        if (!valid) return;

        currentUser.setNickname(nickname);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        binding.btnSave.setEnabled(false);
        
        AppDatabase.databaseExecutor.execute(() -> {
            userRepository.update(currentUser);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Snackbar.make(requireView(), "Profile updated", Snackbar.LENGTH_SHORT).show();
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
