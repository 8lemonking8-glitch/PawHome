package com.example.midtermproject.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.repository.PetRepository;

public class EditPetBottomSheet extends AddPetBottomSheet {

    private final PetEntity pet;
    private final Runnable onUpdated;

    public EditPetBottomSheet(PetEntity pet, Runnable onUpdated) {
        this.pet = pet;
        this.onUpdated = onUpdated;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateFields();
    }

    private void populateFields() {
        binding.etName.setText(pet.getName() != null ? pet.getName() : "");
        binding.etBreed.setText(pet.getBreed() != null ? pet.getBreed() : "");
        binding.etAge.setText(pet.getAge() != null ? pet.getAge().replaceAll("[^0-9.]", "") : "");
        binding.etDescription.setText(pet.getDescription() != null ? pet.getDescription() : "");

        String typeLabel;
        switch (pet.getType()) {
            case "CAT":  typeLabel = "Cats"; break;
            case "BIRD": typeLabel = "Birds"; break;
            default:     typeLabel = "Dogs"; break;
        }
        binding.spinnerType.setText(typeLabel, false);
        binding.spinnerGender.setText(pet.getGender(), false);
        binding.spinnerSize.setText(pet.getSize(), false);

        if (com.example.midtermproject.util.PetImageUtils.loadFirstImage(binding.ivPetPreview, pet)) {
            binding.layoutImageHint.setVisibility(View.GONE);
        }
    }

    @Override
    protected void savePet() {
        if (!validateFields()) return;

        String dbType = resolveDbType();
        pet.setType(dbType);
        applyPetFields(pet, dbType);

        new PetRepository(requireActivity().getApplication()).update(pet);
        Snackbar.make(requireView(), "Pet Updated Successfully", Snackbar.LENGTH_SHORT).show();
        if (onUpdated != null) onUpdated.run();
        dismiss();
    }
}
