package com.life.lifelink.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.life.lifelink.model.JwtResponse;

public class SessionManager {
    private static final String PREF_NAME = "LifeLinkPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(JwtResponse response) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, response.getToken());
        editor.putString(KEY_REFRESH_TOKEN, response.getRefreshToken());
        editor.putString(KEY_USER_ID, response.getId());
        editor.putString(KEY_EMAIL, response.getEmail());
        editor.putString(KEY_NAME, response.getName());
        editor.putString(KEY_ROLE, response.getRole());
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    // Add getters for other fields as needed
}
