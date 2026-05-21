package com.example.midtermproject.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.R;
import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.databinding.FragmentRegisterBinding;
import com.example.midtermproject.util.SessionManager;
import com.example.midtermproject.data.entity.UserEntity;

import java.util.concurrent.Executors;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRepository = new UserRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.cardLoginLink.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).switchToLoginTab();
            }
        });
    }

    private void attemptRegister() {
        // Clear previous errors
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
        binding.tvError.setVisibility(View.GONE);

        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validation
        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.setError(getString(R.string.field_required));
            valid = false;
        } else if (username.length() < 3 || username.length() > 20) {
            binding.tilUsername.setError("Username must be 3-20 characters");
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.field_required));
            valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.password_too_short));
            valid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.field_required));
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.password_mismatch));
            valid = false;
        }

        if (!valid) return;

        // Disable button while processing
        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText(getString(R.string.loading));

        // Attempt registration on background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            long userId = userRepository.register(username, password, username, "");

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.btnRegister.setEnabled(true);
                    binding.btnRegister.setText(getString(R.string.register));

                    if (userId > 0) {
                        // Success - auto login
                        sessionManager.createSession(userId, username, "USER", username);

                        Toast.makeText(requireContext(),
                                "Account created successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to main
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).navigateToMain();
                        }
                    } else {
                        // Failed - username exists
                        binding.tvError.setText(getString(R.string.register_error));
                        binding.tvError.setVisibility(View.VISIBLE);
                    }
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
