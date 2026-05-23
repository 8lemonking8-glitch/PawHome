package com.example.midtermproject.util;

import android.net.Uri;
import android.widget.ImageView;

import com.example.midtermproject.data.entity.PetEntity;

import org.json.JSONArray;

public class PetImageUtils {

    /** Load the first available pet image into the given ImageView. Returns true if successful. */
    public static boolean loadFirstImage(ImageView view, PetEntity pet) {
        if (pet.getImageResId() != 0) {
            view.setImageResource(pet.getImageResId());
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return true;
        }
        if (pet.getImageResIds() != null && pet.getImageResIds().length() > 2) {
            try {
                JSONArray array = new JSONArray(pet.getImageResIds());
                if (array.length() > 0) {
                    Object first = array.get(0);
                    if (first instanceof Integer) {
                        view.setImageResource((Integer) first);
                    } else {
                        view.setImageURI(Uri.parse(first.toString()));
                    }
                    view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    return true;
                }
            } catch (Exception e) {
                android.util.Log.w("PetImageUtils", "Failed to parse image JSON", e);
            }
        }
        return false;
    }
}
