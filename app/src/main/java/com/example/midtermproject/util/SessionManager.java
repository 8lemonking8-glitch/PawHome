package com.example.midtermproject.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user session using SharedPreferences.
 * Stores login state, user ID, username, and role for session persistence.
 */
public class SessionManager {

    private static final String PREF_NAME = "PawHomeSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_NICKNAME = "nickname";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Saves the user session after successful login.
     */
    public void createSession(long userId, String username, String role, String nickname) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_NICKNAME, nickname);
        editor.apply();
    }

    /**
     * Clears the session on logout.
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "USER");
    }

    public String getNickname() {
        return prefs.getString(KEY_NICKNAME, "");
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    /**
     * Updates the nickname in the session (after profile edit).
     */
    public void updateNickname(String nickname) {
        editor.putString(KEY_NICKNAME, nickname);
        editor.apply();
    }
}
