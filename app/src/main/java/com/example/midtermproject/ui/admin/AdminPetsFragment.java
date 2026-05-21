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

import java.util.ArrayList;
import java.util.List;

public class AdminPetsFragment extends Fragment {

    private FragmentAdminPetsBinding binding;
    private AdminPetAdapter adapter;
    private PetRepository petRepository;
    private List<PetEntity> petsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminPetsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.rvPets, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top + (int)(8 * getResources().getDisplayMetrics().density), v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });


        petRepository = new PetRepository(requireActivity().getApplication());

        setupRecyclerView();

        binding.fabAddPet.setOnClickListener(v -> {
            AddPetBottomSheet bottomSheet = new AddPetBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "AddPetBottomSheet");
        });

        petRepository.getAllPets().observe(getViewLifecycleOwner(), pets -> {
            petsList = pets;
            adapter.setPets(pets);
            binding.layoutEmpty.setVisibility(pets.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminPetAdapter();
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
                    petRepository.delete(pet);
                    // LiveData will automatically update the list
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
