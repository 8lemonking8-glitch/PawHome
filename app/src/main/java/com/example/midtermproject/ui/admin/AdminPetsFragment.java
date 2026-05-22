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
import java.util.List;

public class AdminPetsFragment extends Fragment {

    private FragmentAdminPetsBinding binding;
    private AdminPetAdapter adapter;
    private PetRepository petRepository;
    private List<PetEntity> petsList = new ArrayList<>();
    private List<PetEntity> allPetsList = new ArrayList<>();
    private String currentCategory = "All";
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


        petRepository = new PetRepository(requireActivity().getApplication());

        setupRecyclerView();

        binding.fabAddPet.setOnClickListener(v -> {
            AddPetBottomSheet bottomSheet = new AddPetBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "AddPetBottomSheet");
        });

        binding.progressBar.setVisibility(View.VISIBLE);
        petRepository.getAllPets().observe(getViewLifecycleOwner(), pets -> {
            binding.progressBar.setVisibility(View.GONE);
            allPetsList = pets != null ? pets : new ArrayList<>();
            applyFilter();
        });

        setupChips();
        setupSearch();
    }

    private void setupChips() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chipAll) currentCategory = "All";
            else if (checkedId == R.id.chipDogs) currentCategory = "DOG";
            else if (checkedId == R.id.chipCats) currentCategory = "CAT";
            else if (checkedId == R.id.chipBirds) currentCategory = "BIRD";

            applyFilter();
        });
    }

    private void setupSearch() {
        binding.ivClear.setOnClickListener(v -> {
            binding.etSearch.setText("");
        });

        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                currentSearchQuery = s.toString().trim();
                binding.ivClear.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilter();
            }
        });
    }

    private void applyFilter() {
        List<PetEntity> filteredList = new ArrayList<>();
        for (PetEntity pet : allPetsList) {
            // Category filter
            boolean matchesCategory = false;
            if ("All".equals(currentCategory)) {
                matchesCategory = true;
            } else if ("DOG".equals(currentCategory) && "DOG".equals(pet.getType())) {
                matchesCategory = true;
            } else if ("CAT".equals(currentCategory) && "CAT".equals(pet.getType())) {
                matchesCategory = true;
            } else if ("BIRD".equals(currentCategory) && "BIRD".equals(pet.getType())) {
                matchesCategory = true;
            }

            // Search filter
            boolean matchesSearch = false;
            if (currentSearchQuery.isEmpty()) {
                matchesSearch = true;
            } else {
                String query = currentSearchQuery.toLowerCase();
                String name = pet.getName() != null ? pet.getName().toLowerCase() : "";
                String breed = pet.getBreed() != null ? pet.getBreed().toLowerCase() : "";
                if (name.contains(query) || breed.contains(query)) {
                    matchesSearch = true;
                }
            }

            if (matchesCategory && matchesSearch) {
                filteredList.add(pet);
            }
        }

        petsList = filteredList;
        adapter.setPets(filteredList);
        binding.layoutEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new AdminPetAdapter();
        adapter.setOnPetActionListener(pet -> {
            EditPetBottomSheet bottomSheet = new EditPetBottomSheet(pet, () -> {
                // Refresh is handled by LiveData
            });
            bottomSheet.show(getChildFragmentManager(), "EditPetBottomSheet");
        });
        binding.rvPets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPets.setAdapter(adapter);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PetEntity pet = petsList.get(position);
                    
                    new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Pet")
                        .setMessage("Are you sure you want to delete " + pet.getName() + "? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            petRepository.delete(pet);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            adapter.notifyItemChanged(position);
                        })
                        .setCancelable(false)
                        .show();
                }
            }
        }).attachToRecyclerView(binding.rvPets);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
