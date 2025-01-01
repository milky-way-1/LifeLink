package com.life.lifelink.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.life.lifelink.model.JwtResponse;

import org.json.JSONObject;

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

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // Helper method to check if session is valid
    public boolean isSessionValid() {
        String token = getToken();
        return token != null && !isTokenExpired(token);
    }

    // Helper method to check if token is expired
    private boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return true;

            String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT));
            JSONObject json = new JSONObject(payload);

            // Get expiration time
            long exp = json.getLong("exp");
            // Compare with current time
            return (exp * 1000) < System.currentTimeMillis();
        } catch (Exception e) {
            return true;
        }
    }

    // Method to update token
    public void updateToken(String newToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, newToken);
        editor.apply();
    }

    // Add getters for other fields as needed
}
