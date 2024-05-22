package com.example.code_binder;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {
    private final String fileName = "ApplicationPrefs";
    private Context context;
    private SharedPreferences preferences;

    public Preferences(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(fileName, MODE_PRIVATE);
    }

    public Map<String, String> getAll() {
//        Map<String, String> loadedSettings = new HashMap<>();
//        loadedSettings.put("hostIP", preferences.getString("hostIP", "192.168.89.108"));
//        loadedSettings.put("hostPort", preferences.getString("hostPort", "11000"));

        return (Map<String, String>) preferences.getAll();
    }

    public String get(String key) {
        return preferences.getString(key, "");
    }

    public void saveAll(Map<String, String> settings) {
        SharedPreferences.Editor editor = preferences.edit();

        for (Map.Entry<String, String> item : settings.entrySet()) {
            editor.putString(item.getKey(), item.getValue());
        }
        editor.apply();
    }

    public void save(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean isEmpty() {
        return preferences.getAll() == null;
    }
}
