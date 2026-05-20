package com.example.midtermproject.ui.admin;

import android.os.Bundle;
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
        adoptionRepository.getAllRequests().removeObservers(getViewLifecycleOwner());
        adoptionRepository.getPendingRequests().observe(getViewLifecycleOwner(), requests -> {
            adapter.setRequests(requests);
            binding.layoutEmpty.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void loadAllRequests() {
        adoptionRepository.getPendingRequests().removeObservers(getViewLifecycleOwner());
        adoptionRepository.getAllRequests().observe(getViewLifecycleOwner(), requests -> {
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
