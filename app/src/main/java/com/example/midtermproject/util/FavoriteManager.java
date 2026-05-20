package com.example.midtermproject.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class FavoriteManager {
    private static final String PREF_NAME = "PawHomeFavorites";
    private static final String KEY_FAVORITES = "favorite_pet_ids";
    
    private final SharedPreferences prefs;
    private final SessionManager sessionManager;

    public FavoriteManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sessionManager = new SessionManager(context);
    }

    private String getUserKey() {
        return KEY_FAVORITES + "_" + sessionManager.getUserId();
    }

    public boolean isFavorite(long petId) {
        Set<String> favorites = prefs.getStringSet(getUserKey(), new HashSet<>());
        return favorites.contains(String.valueOf(petId));
    }

    public void toggleFavorite(long petId) {
        Set<String> favorites = new HashSet<>(prefs.getStringSet(getUserKey(), new HashSet<>()));
        String idStr = String.valueOf(petId);
        
        if (favorites.contains(idStr)) {
            favorites.remove(idStr);
        } else {
            favorites.add(idStr);
        }
        
        prefs.edit().putStringSet(getUserKey(), favorites).apply();
    }
    
    public Set<String> getFavoriteIds() {
        return prefs.getStringSet(getUserKey(), new HashSet<>());
    }
}
