package com.example.midtermproject.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.midtermproject.R;
import com.example.midtermproject.data.repository.AdoptionRepository;
import com.example.midtermproject.databinding.BottomSheetAdoptionBinding;
import com.example.midtermproject.util.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.concurrent.Executors;

public class AdoptionBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAdoptionBinding binding;
    private final long petId;
    private final Runnable onSuccess;
    private AdoptionRepository adoptionRepository;
    private SessionManager sessionManager;

    public AdoptionBottomSheet(long petId, Runnable onSuccess) {
        this.petId = petId;
        this.onSuccess = onSuccess;
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

        binding.btnClear.setOnClickListener(v -> binding.signatureView.clear());
        
        binding.btnSubmit.setOnClickListener(v -> submitAdoption());
    }

    private void submitAdoption() {
        if (!binding.signatureView.isSigned()) {
            Toast.makeText(requireContext(), getString(R.string.signature_required), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSubmit.setEnabled(false);
        binding.btnSubmit.setText(getString(R.string.loading));

        long userId = sessionManager.getUserId();
        
        // In a real app we would convert the signature bitmap to a Base64 string or file.
        // For this midterm project, we just save a placeholder string indicating it was signed.
        long timestamp = System.currentTimeMillis();
        String signaturePath = "signature_captured_timestamp_" + timestamp;

        Executors.newSingleThreadExecutor().execute(() -> {
            long result = adoptionRepository.createRequest(userId, petId, signaturePath, timestamp);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (result > 0) {
                        Toast.makeText(requireContext(), getString(R.string.adoption_submitted), Toast.LENGTH_LONG).show();
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                        dismiss();
                    } else {
                        binding.btnSubmit.setEnabled(true);
                        binding.btnSubmit.setText(getString(R.string.confirm));
                        Toast.makeText(requireContext(), "You already have a pending request for this pet", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
