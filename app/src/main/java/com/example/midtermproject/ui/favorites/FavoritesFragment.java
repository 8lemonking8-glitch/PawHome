package com.example.midtermproject.ui.favorites;

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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.databinding.FragmentFavoritesBinding;
import com.example.midtermproject.ui.detail.PetDetailActivity;
import com.example.midtermproject.ui.home.PetAdapter;
import com.example.midtermproject.util.FavoriteManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private PetAdapter adapter;
    private PetRepository petRepository;
    private FavoriteManager favoriteManager;
    private List<PetEntity> favoritePets = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.rvFavorites, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top + (int)(8 * getResources().getDisplayMetrics().density), v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });


        petRepository = new PetRepository(requireActivity().getApplication());
        favoriteManager = new FavoriteManager(requireContext());

        setupRecyclerView();
        loadFavorites();
    }

    private void setupRecyclerView() {
        adapter = new PetAdapter((pet, sharedImageView) -> {
            Intent intent = new Intent(requireContext(), PetDetailActivity.class);
            intent.putExtra(PetDetailActivity.EXTRA_PET_ID, pet.getId());
            
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(), sharedImageView, "pet_image_" + pet.getId());
                    
            startActivity(intent, options.toBundle());
        });
        
        adapter.setFavoriteManager(favoriteManager);
        
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvFavorites.setLayoutManager(layoutManager);
        binding.rvFavorites.setAdapter(adapter);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < favoritePets.size()) {
                    PetEntity pet = favoritePets.get(position);
                    favoriteManager.toggleFavorite(pet.getId());
                    favoritePets.remove(position);
                    adapter.setPets(new ArrayList<>(favoritePets)); // Trigger Diff/Update
                    updateEmptyState();
                }
            }
        }).attachToRecyclerView(binding.rvFavorites);
    }

    private List<PetEntity> allPetsList = new ArrayList<>();

    private void loadFavorites() {
        petRepository.getAllPets().observe(getViewLifecycleOwner(), allPets -> {
            this.allPetsList = allPets;
            updateFavoriteList();
        });
    }

    private void updateFavoriteList() {
        if (allPetsList == null || favoriteManager == null) return;
        
        Set<String> favIds = favoriteManager.getFavoriteIds();
        favoritePets.clear();
        
        if (!favIds.isEmpty()) {
            for (PetEntity pet : allPetsList) {
                if (favIds.contains(String.valueOf(pet.getId()))) {
                    favoritePets.add(pet);
                }
            }
        }
        
        if (adapter != null) {
            adapter.setPets(new ArrayList<>(favoritePets));
        }
        updateEmptyState();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFavoriteList();
    }

    private void updateEmptyState() {
        binding.layoutEmpty.setVisibility(favoritePets.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
