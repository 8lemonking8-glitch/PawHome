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

    protected BottomSheetAddPetBinding binding;
    protected PetRepository petRepository;
    protected String selectedImageUri = null;

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
        View view = getView();
        if (view == null) return;
        View parent = (View) view.getParent();
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

    protected boolean validateFields() {
        if (binding.etName.getText().toString().trim().isEmpty()
            || binding.etBreed.getText().toString().trim().isEmpty()
            || binding.etAge.getText().toString().trim().isEmpty()
            || binding.spinnerType.getText().toString().isEmpty()
            || binding.spinnerGender.getText().toString().isEmpty()
            || binding.spinnerSize.getText().toString().isEmpty()) {
            Snackbar.make(requireView(), "Please fill all fields", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    protected String resolveDbType() {
        String type = binding.spinnerType.getText().toString();
        if ("Cats".equals(type)) return "CAT";
        if ("Birds".equals(type)) return "BIRD";
        return "DOG";
    }

    protected void applyPetFields(PetEntity pet, String dbType) {
        pet.setName(binding.etName.getText().toString().trim());
        pet.setBreed(binding.etBreed.getText().toString().trim());
        pet.setAge(binding.etAge.getText().toString().trim() + " Years");
        pet.setType(dbType);
        pet.setGender(binding.spinnerGender.getText().toString());
        pet.setSize(binding.spinnerSize.getText().toString());
        pet.setDescription(binding.etDescription.getText().toString().trim());

        int cardRes = getCarouselCardRes(dbType);
        if (selectedImageUri != null) {
            pet.setImageResIds("[\"" + selectedImageUri + "\"," + cardRes + "," + cardRes + "]");
            pet.setImageResId(0);
        } else {
            pet.setImageResId(getDefaultImageRes(dbType));
            pet.setImageResIds("[" + pet.getImageResId() + "," + cardRes + "," + cardRes + "]");
        }
    }

    private int getDefaultImageRes(String dbType) {
        switch (dbType) {
            case "CAT": return R.drawable.img_cat;
            case "BIRD": return R.drawable.img_bird;
            default: return R.drawable.img_dog;
        }
    }

    private int getCarouselCardRes(String dbType) {
        switch (dbType) {
            case "CAT": return R.drawable.bg_carousel_cat;
            case "BIRD": return R.drawable.bg_carousel_bird;
            default: return R.drawable.bg_carousel_dog;
        }
    }

    protected void savePet() {
        if (!validateFields()) return;

        String dbType = resolveDbType();
        PetEntity pet = new PetEntity();
        pet.setStatus("AVAILABLE");
        pet.setCreatedAt(System.currentTimeMillis());
        applyPetFields(pet, dbType);

        petRepository.insert(pet);
        Snackbar.make(requireView(), "Pet Added Successfully", Snackbar.LENGTH_SHORT).show();
        dismiss();
    }
}
