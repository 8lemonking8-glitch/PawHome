package com.example.midtermproject.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.midtermproject.data.repository.AdoptionRepository;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.databinding.FragmentAdminDashboardBinding;
import com.example.midtermproject.ui.auth.AuthActivity;
import com.example.midtermproject.util.SessionManager;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private PetRepository petRepository;
    private AdoptionRepository adoptionRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });




        petRepository = new PetRepository(requireActivity().getApplication());
        adoptionRepository = new AdoptionRepository(requireActivity().getApplication());
        sessionManager = new SessionManager(requireContext());

        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadStatistics();
    }

    private void loadStatistics() {
        petRepository.getTotalPetCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvTotalPets.setText(String.valueOf(count != null ? count : 0));
        });

        petRepository.getAvailablePetCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvAvailablePets.setText(String.valueOf(count != null ? count : 0));
        });
        
        petRepository.getAdoptedPetCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvAdoptedPets.setText(String.valueOf(count != null ? count : 0));
        });

        adoptionRepository.getPendingCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvPendingRequests.setText(String.valueOf(count != null ? count : 0));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
