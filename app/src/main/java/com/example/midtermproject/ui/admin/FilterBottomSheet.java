package com.example.midtermproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.midtermproject.R;
import com.example.midtermproject.databinding.BottomSheetAdminFilterBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashSet;
import java.util.Set;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAdminFilterBinding binding;
    private final OnFilterApplied listener;
    private final Set<String> selectedTypes;
    private final Set<String> selectedStatuses;
    private boolean updatingTypes, updatingStatuses;

    public interface OnFilterApplied {
        void onApply(Set<String> types, Set<String> statuses);
    }

    public FilterBottomSheet(Set<String> selectedTypes, Set<String> selectedStatuses, OnFilterApplied listener) {
        this.selectedTypes = new HashSet<>(selectedTypes);
        this.selectedStatuses = new HashSet<>(selectedStatuses);
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAdminFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.chipFilterDog.setChecked(selectedTypes.contains("DOG"));
        binding.chipFilterCat.setChecked(selectedTypes.contains("CAT"));
        binding.chipFilterBird.setChecked(selectedTypes.contains("BIRD"));

        binding.chipFilterAvailable.setChecked(selectedStatuses.contains("AVAILABLE"));
        binding.chipFilterAdopted.setChecked(selectedStatuses.contains("ADOPTED"));

        // Enforce at least one type selected
        binding.chipGroupType.setOnCheckedStateChangeListener((group, ids) -> {
            if (updatingTypes) return;
            if (ids.isEmpty()) {
                updatingTypes = true;
                binding.chipFilterDog.setChecked(true);
                binding.chipFilterCat.setChecked(true);
                binding.chipFilterBird.setChecked(true);
                updatingTypes = false;
            }
        });

        // Enforce at least one status selected
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, ids) -> {
            if (updatingStatuses) return;
            if (ids.isEmpty()) {
                updatingStatuses = true;
                binding.chipFilterAvailable.setChecked(true);
                binding.chipFilterAdopted.setChecked(true);
                updatingStatuses = false;
            }
        });

        binding.btnApply.setOnClickListener(v -> {
            Set<String> types = new HashSet<>();
            if (binding.chipFilterDog.isChecked()) types.add("DOG");
            if (binding.chipFilterCat.isChecked()) types.add("CAT");
            if (binding.chipFilterBird.isChecked()) types.add("BIRD");

            Set<String> statuses = new HashSet<>();
            if (binding.chipFilterAvailable.isChecked()) statuses.add("AVAILABLE");
            if (binding.chipFilterAdopted.isChecked()) statuses.add("ADOPTED");

            listener.onApply(types, statuses);
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        View parent = (View) getView().getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
