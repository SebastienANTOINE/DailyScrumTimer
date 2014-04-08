package fr.sebastien.antoine.dailyscrumtimer.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class permits to save data and get it from SharedPreferences.
 *
 * @author Sebastien
 */
public class PreferencesManager {

    /**
     * The preferences.
     */
    private SharedPreferences preferences;

    /**
     * Instantiates a new preferences manager.
     *
     * @param context the context
     */
    public PreferencesManager(Context context) {
        this.preferences = context.getSharedPreferences("fr.sebastien.antoine.dailyscrumtimer", Context.MODE_PRIVATE);
    }

    /**
     * Put data.
     *
     * @param key   the key
     * @param value the value
     */
    public void putDataString(String key, String value) {
        preferences.edit().putString(key, value).commit();
    }

    public void putDataInt(String key, int value) {
        preferences.edit().putInt(key, value).commit();
    }

    /**
     * Get data.
     *
     * @param key
     * @return the value
     */
    public String getDataString(String key) {
        return preferences.getString(key, null);
    }

    public int getDataInt(String key) {
        return preferences.getInt(key, 0);
    }
}