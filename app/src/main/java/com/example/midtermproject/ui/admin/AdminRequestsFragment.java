package com.example.midtermproject.ui.admin;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.midtermproject.data.repository.AdoptionRepository;
import com.example.midtermproject.databinding.FragmentAdminRequestsBinding;
import com.google.android.material.tabs.TabLayout;

public class AdminRequestsFragment extends Fragment {

    private FragmentAdminRequestsBinding binding;
    private AdminRequestAdapter adapter;
    private AdoptionRepository adoptionRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.rvRequests, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top + (int)(8 * getResources().getDisplayMetrics().density), v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });


        adoptionRepository = new AdoptionRepository(requireActivity().getApplication());

        setupTabs();
        setupRecyclerView();
        loadPendingRequests();
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Pending"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadPendingRequests();
                } else {
                    loadAllRequests();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminRequestAdapter(
            request -> adoptionRepository.approveRequest(request.getId(), request.getPetId()),
            request -> adoptionRepository.rejectRequest(request.getId())
        );
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRequests.setAdapter(adapter);
    }

    private void loadPendingRequests() {
        // Remove observers before adding new ones
        adoptionRepository.getAllRequestsWithDetails().removeObservers(getViewLifecycleOwner());
        binding.progressBar.setVisibility(View.VISIBLE);
        adoptionRepository.getPendingRequestsWithDetails().observe(getViewLifecycleOwner(), requests -> {
            binding.progressBar.setVisibility(View.GONE);
            adapter.setRequests(requests);
            binding.layoutEmpty.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void loadAllRequests() {
        adoptionRepository.getPendingRequestsWithDetails().removeObservers(getViewLifecycleOwner());
        binding.progressBar.setVisibility(View.VISIBLE);
        adoptionRepository.getAllRequestsWithDetails().observe(getViewLifecycleOwner(), requests -> {
            binding.progressBar.setVisibility(View.GONE);
            adapter.setRequests(requests);
            binding.layoutEmpty.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
