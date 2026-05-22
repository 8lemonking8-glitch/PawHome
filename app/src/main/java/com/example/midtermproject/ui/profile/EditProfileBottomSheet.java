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
                binding.etAge.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
                binding.etGender.setText(user.getGender());
                binding.etAddress.setText(user.getAddress());
                binding.etHousing.setText(user.getHousingCondition());
                binding.etIncome.setText(user.getMonthlyIncome());
                binding.etExperience.setText(user.getPetExperience());
            }
        });
    }

    private void saveProfile() {
        if (currentUser == null) return;

        binding.tilNickname.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilAge.setError(null);
        binding.tilGender.setError(null);
        binding.tilAddress.setError(null);
        binding.tilHousing.setError(null);
        binding.tilIncome.setError(null);
        binding.tilExperience.setError(null);

        String nickname = binding.etNickname.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String ageStr = binding.etAge.getText().toString().trim();
        String gender = binding.etGender.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String housing = binding.etHousing.getText().toString().trim();
        String income = binding.etIncome.getText().toString().trim();
        String experience = binding.etExperience.getText().toString().trim();

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
        } else {
            binding.tilPhone.setError("Phone cannot be empty");
            valid = false;
        }

        int age = 0;
        if (ageStr.isEmpty()) {
            binding.tilAge.setError("Age cannot be empty");
            valid = false;
        } else {
            try {
                age = Integer.parseInt(ageStr);
                if (age <= 0 || age > 120) {
                    binding.tilAge.setError("Invalid age (1-120)");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                binding.tilAge.setError("Must be a number");
                valid = false;
            }
        }

        if (gender.isEmpty()) {
            binding.tilGender.setError("Gender cannot be empty");
            valid = false;
        }

        if (address.isEmpty()) {
            binding.tilAddress.setError("Address cannot be empty");
            valid = false;
        }

        if (housing.isEmpty()) {
            binding.tilHousing.setError("Housing status cannot be empty");
            valid = false;
        }

        if (income.isEmpty()) {
            binding.tilIncome.setError("Income cannot be empty");
            valid = false;
        }

        if (experience.isEmpty()) {
            binding.tilExperience.setError("Experience cannot be empty");
            valid = false;
        }

        if (!valid) return;

        currentUser.setNickname(nickname);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);
        currentUser.setAge(age);
        currentUser.setGender(gender);
        currentUser.setAddress(address);
        currentUser.setHousingCondition(housing);
        currentUser.setMonthlyIncome(income);
        currentUser.setPetExperience(experience);

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
