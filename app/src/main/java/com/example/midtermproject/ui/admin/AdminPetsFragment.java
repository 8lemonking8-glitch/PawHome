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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.databinding.FragmentAdminPetsBinding;
import com.example.midtermproject.R;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminPetsFragment extends Fragment {

    private FragmentAdminPetsBinding binding;
    private AdminPetAdapter adapter;
    private PetRepository petRepository;
    private List<PetEntity> petsList = new ArrayList<>();
    private List<PetEntity> allPetsList = new ArrayList<>();
    private Set<String> selectedTypes = new HashSet<>();
    private Set<String> selectedStatuses = new HashSet<>();
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminPetsBinding.inflate(inflater, container, false);
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

        // Default: all selected
        selectedTypes.add("DOG");
        selectedTypes.add("CAT");
        selectedTypes.add("BIRD");
        selectedStatuses.add("AVAILABLE");
        selectedStatuses.add("ADOPTED");

        petRepository = new PetRepository(requireActivity().getApplication());

        setupRecyclerView();

        binding.fabAddPet.setOnClickListener(v ->
            new AddPetBottomSheet().show(getChildFragmentManager(), "AddPetBottomSheet"));

        binding.progressBar.setVisibility(View.VISIBLE);
        petRepository.getAllPets().observe(getViewLifecycleOwner(), pets -> {
            binding.progressBar.setVisibility(View.GONE);
            allPetsList = pets != null ? pets : new ArrayList<>();
            applyFilter();
        });

        binding.ivFilter.setOnClickListener(v -> showFilterSheet());
        setupSearch();
    }

    private void showFilterSheet() {
        new FilterBottomSheet(selectedTypes, selectedStatuses, (types, statuses) -> {
            selectedTypes = types;
            selectedStatuses = statuses;
            applyFilter();
        }).show(getChildFragmentManager(), "FilterBottomSheet");
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                currentSearchQuery = s.toString().trim();
                applyFilter();
            }
        });
    }

    private void applyFilter() {
        List<PetEntity> filteredList = new ArrayList<>();
        for (PetEntity pet : allPetsList) {
            if (!selectedTypes.contains(pet.getType())) continue;

            if (!currentSearchQuery.isEmpty()) {
                String q = currentSearchQuery.toLowerCase();
                String name = pet.getName() != null ? pet.getName().toLowerCase() : "";
                String breed = pet.getBreed() != null ? pet.getBreed().toLowerCase() : "";
                if (!name.contains(q) && !breed.contains(q)) continue;
            }

            if (!selectedStatuses.contains(pet.getStatus())) continue;

            filteredList.add(pet);
        }

        petsList = filteredList;
        adapter.setPets(filteredList);
        binding.layoutEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new AdminPetAdapter();
        adapter.setOnPetActionListener(pet ->
            new EditPetBottomSheet(pet, () -> {})
                .show(getChildFragmentManager(), "EditPetBottomSheet"));
        binding.rvPets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPets.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                PetEntity pet = petsList.get(pos);
                new AlertDialog.Builder(requireContext())
                    .setTitle("Archive Pet")
                    .setMessage("Archive " + pet.getName() + "?")
                    .setPositiveButton("Archive", (d, w) -> {
                        pet.setStatus("ARCHIVED");
                        petRepository.update(pet);
                        adapter.notifyItemChanged(pos);
                    })
                    .setNegativeButton("Cancel", (d, w) -> adapter.notifyItemChanged(pos))
                    .setCancelable(false)
                    .show();
            }
        }).attachToRecyclerView(binding.rvPets);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
