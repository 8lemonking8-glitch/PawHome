package com.example.midtermproject.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.databinding.FragmentLoginBinding;
import com.example.midtermproject.util.SessionManager;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRepository = new UserRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.cardRegisterLink.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).switchToRegisterTab();
            }
        });
    }

    private void attemptLogin() {
        // Clear previous errors
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);
        binding.tvError.setVisibility(View.GONE);

        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.setError(getString(R.string.field_required));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.field_required));
            return;
        }

        // Disable button while processing
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText(getString(R.string.loading));

        com.example.midtermproject.data.database.AppDatabase.databaseExecutor.execute(() -> {
            UserEntity user = userRepository.login(username, password);

            android.app.Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.btnLogin.setEnabled(true);
                    binding.btnLogin.setText(getString(R.string.login));

                    if (user != null) {
                        sessionManager.createSession(
                                user.getId(),
                                user.getUsername(),
                                user.getRole(),
                                user.getNickname() != null ? user.getNickname() : user.getUsername()
                        );

                        if (activity instanceof AuthActivity) {
                            ((AuthActivity) activity).navigateToMain();
                        }
                    } else {
                        binding.tvError.setText(getString(R.string.login_error));
                        binding.tvError.setVisibility(View.VISIBLE);

                        binding.tvError.animate()
                                .translationX(10)
                                .setDuration(50)
                                .withEndAction(() -> binding.tvError.animate()
                                        .translationX(-10)
                                        .setDuration(50)
                                        .withEndAction(() -> binding.tvError.animate()
                                                .translationX(0)
                                                .setDuration(50)
                                                .start())
                                        .start())
                                .start();
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
