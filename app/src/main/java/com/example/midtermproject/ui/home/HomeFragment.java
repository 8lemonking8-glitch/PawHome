package com.example.midtermproject.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.Intent;
import com.example.midtermproject.R;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.databinding.FragmentHomeBinding;
import com.example.midtermproject.ui.detail.PetDetailActivity;
import com.example.midtermproject.util.FavoriteManager;
import com.example.midtermproject.util.SessionManager;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PetAdapter adapter;
    private PetRepository petRepository;
    private String currentCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        petRepository = new PetRepository(requireActivity().getApplication());
        SessionManager sessionManager = new SessionManager(requireContext());
        
        // Greeting
        String nickname = sessionManager.getNickname();
        binding.tvGreeting.setText("Hello, " + (nickname != null ? nickname : "Adopter") + "!");

        setupRecyclerView();
        setupChips();
        setupSearch();
        observePets();
    }

    private void setupRecyclerView() {
        adapter = new PetAdapter(pet -> {
            Intent intent = new Intent(requireContext(), PetDetailActivity.class);
            intent.putExtra(PetDetailActivity.EXTRA_PET_ID, pet.getId());
            startActivity(intent);
        });
        
        FavoriteManager favoriteManager = new FavoriteManager(requireContext());
        adapter.setFavoriteManager(favoriteManager);
        
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvPets.setLayoutManager(layoutManager);
        binding.rvPets.setAdapter(adapter);
    }

    private void setupChips() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            
            if (checkedId == R.id.chipAll) currentCategory = "All";
            else if (checkedId == R.id.chipDogs) currentCategory = "DOG";
            else if (checkedId == R.id.chipCats) currentCategory = "CAT";
            else if (checkedId == R.id.chipBirds) currentCategory = "BIRD";
            
            observePets(); // Reload data
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    petRepository.searchPets(query).observe(getViewLifecycleOwner(), pets -> {
                        adapter.setPets(pets);
                        binding.layoutEmpty.setVisibility(pets.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                } else {
                    observePets(); // Reset to category view
                }
            }
        });
    }

    private void observePets() {
        if (currentCategory.equals("All")) {
            petRepository.getAvailablePets().observe(getViewLifecycleOwner(), pets -> {
                adapter.setPets(pets);
                binding.layoutEmpty.setVisibility(pets.isEmpty() ? View.VISIBLE : View.GONE);
            });
        } else {
            petRepository.getAvailablePetsByType(currentCategory).observe(getViewLifecycleOwner(), pets -> {
                adapter.setPets(pets);
                binding.layoutEmpty.setVisibility(pets.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
