package com.example.midtermproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.midtermproject.R;
import com.example.midtermproject.data.entity.PetEntity;
import com.example.midtermproject.data.repository.PetRepository;
import com.example.midtermproject.databinding.BottomSheetAddPetBinding;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.net.Uri;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import android.graphics.Bitmap;

public class AddPetBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddPetBinding binding;
    private PetRepository petRepository;
    private String selectedImageUri = null;

    private final ActivityResultLauncher<CropImageContractOptions> cropLauncher =
        registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Uri croppedUri = result.getUriContent();
                if (croppedUri != null) {
                    InputStream is = null;
                    FileOutputStream fos = null;
                    try {
                        is = requireContext().getContentResolver().openInputStream(croppedUri);
                        File file = new File(requireContext().getFilesDir(), "pet_" + System.currentTimeMillis() + ".jpg");
                        fos = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        selectedImageUri = Uri.fromFile(file).toString();
                        binding.ivPetPreview.setImageURI(Uri.parse(selectedImageUri));
                        binding.layoutImageHint.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Snackbar.make(requireView(), "Failed to save cropped image", Snackbar.LENGTH_SHORT).show();
                    } finally {
                        if (is != null) { try { is.close(); } catch (java.io.IOException ignored) {} }
                        if (fos != null) { try { fos.close(); } catch (java.io.IOException ignored) {} }
                    }
                }
            }
        });

    private void launchCrop(Uri sourceUri) {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        options.fixAspectRatio = false;
        options.imageSourceIncludeGallery = false;
        options.imageSourceIncludeCamera = false;
        cropLauncher.launch(new CropImageContractOptions(sourceUri, options));
    }

    private final ActivityResultLauncher<String> pickImageLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) launchCrop(uri);
        });

    private final ActivityResultLauncher<Void> takePhotoLauncher =
        registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap != null) {
                FileOutputStream fos = null;
                try {
                    File file = new File(requireContext().getFilesDir(), "pet_raw_" + System.currentTimeMillis() + ".jpg");
                    fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    launchCrop(Uri.fromFile(file));
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(requireView(), "Failed to save photo", Snackbar.LENGTH_SHORT).show();
                } finally {
                    if (fos != null) { try { fos.close(); } catch (java.io.IOException ignored) {} }
                }
            }
        });

    private void showImagePickerDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Select Image")
            .setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                if (which == 0) {
                    takePhotoLauncher.launch(null);
                } else {
                    pickImageLauncher.launch("image/*");
                }
            })
            .show();
    }

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

        binding.cardImagePicker.setOnClickListener(v -> showImagePickerDialog());
        binding.btnSave.setOnClickListener(v -> savePet());
    }

    @Override
    public void onStart() {
        super.onStart();
        View parent = (View) getView().getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void setupSpinners() {
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
            Snackbar.make(requireView(), "Please fill all fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        PetEntity pet = new PetEntity();
        pet.setName(name);
        pet.setBreed(breed);
        pet.setAge(ageStr + " Years");
        String dbType = "DOG";
        if ("Cats".equals(type)) dbType = "CAT";
        else if ("Birds".equals(type)) dbType = "BIRD";

        pet.setType(dbType);
        pet.setGender(gender);
        pet.setSize(size);
        pet.setDescription(description);
        pet.setStatus("AVAILABLE");
        int cardRes;
        switch (dbType) {
            case "CAT": cardRes = R.drawable.bg_carousel_cat; break;
            case "BIRD": cardRes = R.drawable.bg_carousel_bird; break;
            default: cardRes = R.drawable.bg_carousel_dog; break;
        }
        if (selectedImageUri != null) {
            pet.setImageResIds("[\"" + selectedImageUri + "\"," + cardRes + "," + cardRes + "]");
            pet.setImageResId(0);
        } else {
            if ("DOG".equals(dbType)) pet.setImageResId(R.drawable.img_dog);
            else if ("CAT".equals(dbType)) pet.setImageResId(R.drawable.img_cat);
            else pet.setImageResId(R.drawable.img_bird);
            pet.setImageResIds("[" + pet.getImageResId() + "," + cardRes + "," + cardRes + "]");
        }
        pet.setCreatedAt(System.currentTimeMillis());

        petRepository.insert(pet);
        Snackbar.make(requireView(), "Pet Added Successfully", Snackbar.LENGTH_SHORT).show();
        dismiss();
    }
}
