package com.example.midtermproject.ui.home;

import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.core.app.ActivityOptionsCompat;
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
    private android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.tvGreeting, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            android.view.ViewGroup.MarginLayoutParams mlp = (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.topMargin = insets.top;
            v.setLayoutParams(mlp);
            return windowInsets;
        });


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
        adapter = new PetAdapter((pet, sharedImageView) -> {
            Intent intent = new Intent(requireContext(), PetDetailActivity.class);
            intent.putExtra(PetDetailActivity.EXTRA_PET_ID, pet.getId());
            
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(), sharedImageView, "pet_image_" + pet.getId());
            
            startActivity(intent, options.toBundle());
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
                String query = s.toString().trim();
                binding.ivClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                searchRunnable = () -> {
                    if (!query.isEmpty()) {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        petRepository.searchPets(query).observe(getViewLifecycleOwner(), pets -> {
                            binding.progressBar.setVisibility(View.GONE);
                            adapter.setPets(pets);
                            binding.layoutEmpty.setVisibility(pets.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    } else {
                        observePets(); // Reset to category view
                    }
                };
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void observePets() {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (currentCategory.equals("All")) {
            petRepository.getAvailablePets().observe(getViewLifecycleOwner(), pets -> {
                binding.progressBar.setVisibility(View.GONE);
                adapter.setPets(pets);
                binding.layoutEmpty.setVisibility(pets.isEmpty() ? View.VISIBLE : View.GONE);
            });
        } else {
            petRepository.getAvailablePetsByType(currentCategory).observe(getViewLifecycleOwner(), pets -> {
                binding.progressBar.setVisibility(View.GONE);
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
