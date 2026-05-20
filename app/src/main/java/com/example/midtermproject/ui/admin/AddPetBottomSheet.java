package com.example.midtermproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.databinding.BottomSheetAddPetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddPetBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddPetBinding binding;
    private PetRepository petRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddPetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        petRepository = new PetRepository(requireActivity().getApplication());

        setupSpinners();

        binding.btnSave.setOnClickListener(v -> savePet());
    }

    private void setupSpinners() {
        // Since we are using hardcoded string arrays in resources, AutoCompleteTextView gets populated via simpleItems in XML.
        // But some devices require manual adapter setting. Let's make sure it's populated.
        String[] types = getResources().getStringArray(R.array.pet_types);
        binding.spinnerType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types));
        
        String[] genders = getResources().getStringArray(R.array.pet_genders);
        binding.spinnerGender.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, genders));
        
        String[] sizes = getResources().getStringArray(R.array.pet_sizes);
        binding.spinnerSize.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, sizes));
    }

    private void savePet() {
        String name = binding.etName.getText().toString().trim();
        String breed = binding.etBreed.getText().toString().trim();
        String ageStr = binding.etAge.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String type = binding.spinnerType.getText().toString();
        String gender = binding.spinnerGender.getText().toString();
        String size = binding.spinnerSize.getText().toString();

        if (name.isEmpty() || breed.isEmpty() || ageStr.isEmpty() || type.isEmpty() || gender.isEmpty() || size.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        PetEntity pet = new PetEntity();
        pet.setName(name);
        pet.setBreed(breed);
        pet.setAge(ageStr + " Years");
        pet.setType(type);
        pet.setGender(gender);
        pet.setSize(size);
        pet.setDescription(description);
        pet.setStatus("Available");
        // Set a default image based on type
        if ("Dogs".equals(type)) pet.setImageResId(R.drawable.ic_dog);
        else if ("Cats".equals(type)) pet.setImageResId(R.drawable.ic_cat);
        else pet.setImageResId(R.drawable.ic_bird);
        pet.setCreatedAt(System.currentTimeMillis());

        petRepository.insert(pet);
        Toast.makeText(requireContext(), "Pet Added Successfully", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
