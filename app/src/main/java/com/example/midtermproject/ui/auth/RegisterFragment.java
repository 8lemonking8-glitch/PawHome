package com.example.midtermproject.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextWatcher;
import android.text.Editable;
import android.content.res.ColorStateList;

import com.example.midtermproject.R;
import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.databinding.FragmentRegisterBinding;
import com.example.midtermproject.util.SessionManager;
import com.example.midtermproject.data.database.AppDatabase;

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

        setupRealTimeValidation();
    }

    private Runnable usernameCheckRunnable;
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    private void setupRealTimeValidation() {
        binding.etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mainHandler.removeCallbacks(usernameCheckRunnable);
                String username = s.toString().trim();
                if (username.isEmpty()) {
                    binding.tilUsername.setError(null);
                    binding.tilUsername.setHelperText(null);
                } else if (username.length() < 3 || username.length() > 20) {
                    binding.tilUsername.setError("Username must be 3-20 characters");
                    binding.tilUsername.setHelperText(null);
                } else {
                    usernameCheckRunnable = () -> {
                        AppDatabase.databaseExecutor.execute(() -> {
                            boolean exists = userRepository.isUsernameExists(username);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (binding.etUsername.getText() != null && 
                                            binding.etUsername.getText().toString().trim().equals(username)) {
                                        if (exists) {
                                            binding.tilUsername.setError("Username already exists");
                                            binding.tilUsername.setHelperText(null);
                                        } else {
                                            binding.tilUsername.setError(null);
                                            binding.tilUsername.setHelperText("Username is available ✔");
                                            binding.tilUsername.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.success)));
                                        }
                                    }
                                });
                            }
                        });
                    };
                    mainHandler.postDelayed(usernameCheckRunnable, 300);
                }
            }
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                if (password.isEmpty()) {
                    binding.tilPassword.setHelperText(null);
                    binding.tilPassword.setError(null);
                } else if (password.length() < 6) {
                    binding.tilPassword.setHelperText("Password must be at least 6 characters ❌");
                    binding.tilPassword.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.error)));
                    binding.tilPassword.setError(null);
                } else {
                    binding.tilPassword.setHelperText("Password meets length requirements ✔");
                    binding.tilPassword.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.success)));
                    binding.tilPassword.setError(null);
                }

                String confirmPassword = binding.etConfirmPassword.getText() != null ? 
                        binding.etConfirmPassword.getText().toString() : "";
                if (!confirmPassword.isEmpty()) {
                    validateConfirmPassword(password, confirmPassword);
                }
            }
        });

        binding.etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = binding.etPassword.getText() != null ? 
                        binding.etPassword.getText().toString() : "";
                String confirmPassword = s.toString();
                validateConfirmPassword(password, confirmPassword);
            }
        });
    }

    private void validateConfirmPassword(String password, String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError(null);
            binding.tilConfirmPassword.setHelperText(null);
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.password_mismatch));
            binding.tilConfirmPassword.setHelperText(null);
        } else {
            binding.tilConfirmPassword.setError(null);
            binding.tilConfirmPassword.setHelperText("Passwords match ✔");
            binding.tilConfirmPassword.setHelperTextColor(ColorStateList.valueOf(getResources().getColor(R.color.success)));
        }
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
        } else if (binding.tilUsername.getError() != null) {
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.field_required));
            valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.password_too_short));
            valid = false;
        } else if (binding.tilPassword.getError() != null) {
            valid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.field_required));
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.password_mismatch));
            valid = false;
        } else if (binding.tilConfirmPassword.getError() != null) {
            valid = false;
        }

        if (!valid) return;

        // Disable button while processing
        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText(getString(R.string.loading));

        // Attempt registration on background thread
        AppDatabase.databaseExecutor.execute(() -> {
            long userId = userRepository.register(username, password, username, "");

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.btnRegister.setEnabled(true);
                    binding.btnRegister.setText(getString(R.string.register));

                    if (userId > 0) {
                        // Success - auto login
                        sessionManager.createSession(userId, username, "USER", username);

                        Snackbar.make(requireView(),
                                "Account created successfully!", Snackbar.LENGTH_SHORT).show();

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
