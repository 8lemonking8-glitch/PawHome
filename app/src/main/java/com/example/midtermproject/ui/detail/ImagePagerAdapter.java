package com.example.midtermproject.ui.detail;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import com.example.midtermproject.databinding.ItemPetImageBinding;
import com.example.midtermproject.data.entity.PetEntity;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    private final List<Object> images = new ArrayList<>();

    public void setPet(PetEntity pet) {
        setImages(pet.getImageResId(), pet.getImageResIds());
    }

    public void setImages(int fallbackResId, String resIdsJson) {
        images.clear();
        if (resIdsJson != null && resIdsJson.length() > 2) {
            try {
                JSONArray array = new JSONArray(resIdsJson);
                for (int i = 0; i < array.length(); i++) {
                    Object val = array.get(i);
                    if (val instanceof String) {
                        images.add((String) val);
                    } else if (val instanceof Integer) {
                        images.add((Integer) val);
                    }
                }
            } catch (Exception e) {
                Log.w("ImagePagerAdapter", "Failed to parse image list", e);
            }
        }
        if (images.isEmpty() && fallbackResId != 0) {
            images.add(fallbackResId);
        }
        notifyDataSetChanged();
    }

    public int getRealCount() {
        return images.size();
    }

    public Object getImageAt(int position) {
        if (position >= 0 && position < images.size()) {
            return images.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPetImageBinding binding = ItemPetImageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object item = images.get(position);
        if (item instanceof Integer) {
            holder.binding.ivImage.setImageResource((Integer) item);
        } else if (item instanceof String) {
            holder.binding.ivImage.setImageURI(Uri.parse((String) item));
        }
        holder.binding.ivImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ItemPetImageBinding binding;
        ImageViewHolder(ItemPetImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
