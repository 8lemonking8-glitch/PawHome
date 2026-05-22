package com.example.midtermproject.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.UserEntity;
import com.example.midtermproject.data.repository.UserRepository;
import com.example.midtermproject.data.repository.AdoptionRepository;
import com.example.midtermproject.databinding.FragmentProfileBinding;
import com.example.midtermproject.ui.auth.AuthActivity;
import com.example.midtermproject.util.SessionManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.midtermproject.data.database.AppDatabase;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private UserRepository userRepository;
    private AdoptionRepository adoptionRepository;
    private AdoptionHistoryAdapter historyAdapter;

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT);
        View bottomNav = requireActivity().findViewById(R.id.bottom_nav_card);
        if (bottomNav != null) snackbar.setAnchorView(bottomNav);
        snackbar.show();
    }

    private final ActivityResultLauncher<CropImageContractOptions> cropLauncher =
        registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Uri croppedUri = result.getUriContent();
                if (croppedUri != null) {
                    saveAvatarFromUri(croppedUri);
                }
            }
        });

    private void launchAvatarCrop(Uri sourceUri) {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        options.fixAspectRatio = true;
        options.aspectRatioX = 1;
        options.aspectRatioY = 1;
        options.imageSourceIncludeGallery = false;
        options.imageSourceIncludeCamera = false;
        cropLauncher.launch(new CropImageContractOptions(sourceUri, options));
    }

    private final ActivityResultLauncher<String> pickImageLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                launchAvatarCrop(uri);
            }
        });

    private final ActivityResultLauncher<Void> takePhotoLauncher =
        registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap != null) {
                File file = new File(requireContext().getFilesDir(), "avatar_raw_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    launchAvatarCrop(Uri.fromFile(file));
                } catch (Exception e) {
                    e.printStackTrace();
                    showSnackbar("Failed to save photo");
                } finally {
                    if (fos != null) { try { fos.close(); } catch (java.io.IOException ignored) {} }
                }
            }
        });

    private void saveAvatarFromUri(Uri uri) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = requireContext().getContentResolver().openInputStream(uri);
            long userId = sessionManager.getUserId();
            File dir = requireContext().getFilesDir();

            File[] existingAvatars = dir.listFiles((d, name) -> name.startsWith("avatar_" + userId));
            if (existingAvatars != null) {
                for (File f : existingAvatars) {
                    f.delete();
                }
            }

            File file = new File(dir, "avatar_" + userId + "_" + System.currentTimeMillis() + ".jpg");
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            updateAvatarUri(Uri.fromFile(file).toString());
        } catch (Exception e) {
            e.printStackTrace();
            showSnackbar("Failed to load image");
        } finally {
            if (is != null) { try { is.close(); } catch (java.io.IOException ignored) {} }
            if (fos != null) { try { fos.close(); } catch (java.io.IOException ignored) {} }
        }
    }

    private void updateAvatarUri(String uriString) {
        AppDatabase.databaseExecutor.execute(() -> {
            UserEntity user = userRepository.getUserByIdSync(sessionManager.getUserId());
            if (user != null) {
                user.setAvatarUri(uriString);
                userRepository.update(user);
                android.app.Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        if (binding == null) return;
                        binding.ivAvatar.setImageURI(null);
                        binding.ivAvatar.setImageURI(Uri.parse(uriString));
                        showSnackbar("Avatar updated");
                    });
                }
            }
        });
    }

    private void showAvatarPickerDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Change Avatar")
            .setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                if (which == 0) {
                    takePhotoLauncher.launch(null);
                } else {
                    pickImageLauncher.launch("image/*");
                }
            })
            .show();
    }

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
        int extraPadding = (int) (12 * density);
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

        historyAdapter = new AdoptionHistoryAdapter();
        binding.rvAdoptionHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdoptionHistory.setAdapter(historyAdapter);

        loadUserProfile();
        loadAdoptionHistory();

        binding.ivAvatar.setOnClickListener(v -> showAvatarPickerDialog());

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
                
                binding.tvAgeGender.setText("Age: " + (user.getAge() > 0 ? user.getAge() : "--") + "  |  Gender: " + (user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "--"));
                binding.tvAddress.setText("Home Address: " + (user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Not provided"));
                binding.tvHousing.setText("Housing Status: " + (user.getHousingCondition() != null && !user.getHousingCondition().isEmpty() ? user.getHousingCondition() : "Not provided"));
                binding.tvIncome.setText("Monthly Income: " + (user.getMonthlyIncome() != null && !user.getMonthlyIncome().isEmpty() ? user.getMonthlyIncome() : "Not provided"));
                binding.tvExperience.setText("Pet Care Experience: " + (user.getPetExperience() != null && !user.getPetExperience().isEmpty() ? user.getPetExperience() : "Not provided"));

                if (user.getAvatarUri() != null && !user.getAvatarUri().isEmpty()) {
                    binding.ivAvatar.setImageURI(null);
                    binding.ivAvatar.setImageURI(Uri.parse(user.getAvatarUri()));
                } else if (user.getAvatarResId() != 0) {
                    binding.ivAvatar.setImageResource(user.getAvatarResId());
                }
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
