package com.example.midtermproject.ui.detail;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.midtermproject.R;
import com.example.midtermproject.data.repository.AdoptionRepository;
import com.example.midtermproject.databinding.BottomSheetAdoptionBinding;
import com.example.midtermproject.util.SessionManager;
import com.example.midtermproject.data.database.AppDatabase;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.FileOutputStream;

public class AdoptionBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAdoptionBinding binding;
    private final long petId;
    private final Runnable onAdopted;
    private final Runnable onDecline;
    private AdoptionRepository adoptionRepository;
    private SessionManager sessionManager;

    public AdoptionBottomSheet(long petId, Runnable onAdopted, Runnable onDecline) {
        this.petId = petId;
        this.onAdopted = onAdopted;
        this.onDecline = onDecline;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAdoptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adoptionRepository = new AdoptionRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        binding.btnAccept.setOnClickListener(v -> {
            binding.layoutAgreementButtons.setVisibility(View.GONE);
            binding.layoutSignature.setVisibility(View.VISIBLE);
        });

        binding.btnDecline.setOnClickListener(v -> {
            Snackbar.make(requireView(), "Adoption declined", Snackbar.LENGTH_SHORT).show();
            if (onDecline != null) {
                onDecline.run();
            }
            dismiss();
        });

        binding.btnClear.setOnClickListener(v -> binding.signatureView.clear());

        binding.btnSubmit.setOnClickListener(v -> submitAdoption());
    }

    private void submitAdoption() {
        if (!binding.signatureView.isSigned()) {
            Snackbar.make(requireView(), getString(R.string.signature_required), Snackbar.LENGTH_SHORT).show();
            return;
        }

        binding.btnSubmit.setEnabled(false);
        binding.btnSubmit.setText(getString(R.string.loading));

        long userId = sessionManager.getUserId();
        long timestamp = System.currentTimeMillis();

        Bitmap signatureBitmap = binding.signatureView.getSignatureBitmap();
        String signaturePath = saveSignatureToFile(signatureBitmap, userId, timestamp);

        AppDatabase.databaseExecutor.execute(() -> {
            long result = adoptionRepository.createRequest(userId, petId, signaturePath, timestamp);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (result > 0) {
                        Snackbar.make(requireView(), getString(R.string.adoption_submitted), Snackbar.LENGTH_LONG).show();
                        if (onAdopted != null) {
                            onAdopted.run();
                        }
                        dismiss();
                    } else {
                        binding.btnSubmit.setEnabled(true);
                        binding.btnSubmit.setText(getString(R.string.confirm));
                        Snackbar.make(requireView(), "You already have a pending request for this pet", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private String saveSignatureToFile(Bitmap bitmap, long userId, long timestamp) {
        try {
            File dir = new File(requireContext().getFilesDir(), "signatures");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "sig_" + userId + "_" + timestamp + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "signature_captured_timestamp_" + timestamp;
        }
    }
}
