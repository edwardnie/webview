package main.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

import static main.MainApp.appContext;

/**
 * Created by yons on 17/3/8.
 */

public class SpUtil {
    //============== sp utils ===========================
    public static SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);
    public static void save(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public static void save(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public static String get(String key) {
        return sp.getString(key, "");
    }

    public static int getInt(String key) {
        return sp.getInt(key, -1);
    }

    public static void save(String key, Set<String> values) {
        sp.edit().putStringSet(key, values).apply();
    }

    public static Set<String> getSet(String key) {
        return sp.getStringSet(key, new HashSet<String>());
    }

    public static void remove(String key) {
        sp.edit().remove(key).apply();
    }
}
