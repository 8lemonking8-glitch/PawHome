package com.example.midtermproject.ui.home;

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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.Intent;
import com.example.midtermproject.R;
import com.example.midtermproject.data.database.AppDatabase;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.databinding.FragmentHomeBinding;
import com.example.midtermproject.ui.detail.PetDetailActivity;
import com.example.midtermproject.util.FavoriteManager;
import com.example.midtermproject.util.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        float density = getResources().getDisplayMetrics().density;
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int extraPadding = (int) (32 * density);
        int defaultTopPadding = statusBarHeight > 0 ? (statusBarHeight + extraPadding) : (int) (64 * density);

        binding.headerContainer.setPadding(
            binding.headerContainer.getPaddingLeft(),
            defaultTopPadding,
            binding.headerContainer.getPaddingRight(),
            binding.headerContainer.getPaddingBottom()
        );

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int topInset = insets.top;
            if (topInset > 0) {
                binding.headerContainer.setPadding(
                    binding.headerContainer.getPaddingLeft(),
                    topInset + extraPadding,
                    binding.headerContainer.getPaddingRight(),
                    binding.headerContainer.getPaddingBottom()
                );
            }
            return windowInsets;
        });

        petRepository = new PetRepository(requireActivity().getApplication());
        SessionManager sessionManager = new SessionManager(requireContext());

        String nickname = sessionManager.getNickname();
        binding.tvGreeting.setText("Hello, " + (nickname != null ? nickname : "Adopter") + "!");

        setupRecyclerView();
        setupChips();
        setupSearch();
        setupSwipeRefresh();
        loadPets();
    }

    private void setupRecyclerView() {
        adapter = new PetAdapter((pet, sharedImageView) -> {
            Intent intent = new Intent(requireContext(), PetDetailActivity.class);
            intent.putExtra(PetDetailActivity.EXTRA_PET_ID, pet.getId());
            
            androidx.core.app.ActivityOptionsCompat options = androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(), sharedImageView, androidx.core.view.ViewCompat.getTransitionName(sharedImageView));

            intent.putExtra(PetDetailActivity.EXTRA_PET_IMAGE_RES_ID, pet.getImageResId());
            intent.putExtra(PetDetailActivity.EXTRA_PET_IMAGE_RES_IDS, pet.getImageResIds());
            intent.putExtra(PetDetailActivity.EXTRA_PET_TYPE, pet.getType());

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

            loadPets();
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> loadPets());
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
                        binding.swipeRefresh.setEnabled(false);
                        binding.progressBar.setVisibility(View.VISIBLE);
                        petRepository.searchPets(query).observe(getViewLifecycleOwner(), pets -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.swipeRefresh.setRefreshing(false);
                            adapter.setPets(pets);
                            binding.layoutEmpty.setVisibility(pets.isEmpty() ? View.VISIBLE : View.GONE);
                            binding.rvPets.setVisibility(pets.isEmpty() ? View.GONE : View.VISIBLE);
                        });
                    } else {
                        binding.swipeRefresh.setEnabled(true);
                        loadPets();
                    }
                };
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void loadPets() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.swipeRefresh.setRefreshing(true);

        AppDatabase.databaseExecutor.execute(() -> {
            List<PetEntity> result;
            if (currentCategory.equals("All")) {
                result = petRepository.getAvailablePetsSync();
            } else {
                result = petRepository.getAvailablePetsByTypeSync(currentCategory);
            }

            final List<PetEntity> pets = result != null ? result : new ArrayList<>();
            Collections.shuffle(pets);

            android.app.Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    adapter.setPets(pets);
                    boolean empty = pets.isEmpty();
                    binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    binding.rvPets.setVisibility(empty ? View.GONE : View.VISIBLE);
                    if (!empty) {
                        binding.rvPets.scrollToPosition(0);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
