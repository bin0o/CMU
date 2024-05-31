package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Pharmacy;

public class CacheUtils {

    private static final String PREFS_NAME = "PharmacyCache";
    private static final String KEY_PHARMACIES = "pharmacies";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final long CACHE_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(5); // 5 min

    public static void savePharmacies(Context context, List<Pharmacy> pharmacies) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(pharmacies);
        editor.putString(KEY_PHARMACIES, json);
        editor.putLong(KEY_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
        Log.d("CacheUtils", "Saving pharmacies in cache.");
    }

    public static List<Pharmacy> getPharmacies(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = preferences.getString(KEY_PHARMACIES, null);
        if (json == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Pharmacy>>() {}.getType();
        Log.d("CacheUtils", "Retrieving pharmacies from cache.");
        return gson.fromJson(json, type);
    }

    public static boolean isCacheValid(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long timestamp = preferences.getLong(KEY_TIMESTAMP, 0);
        return (System.currentTimeMillis() - timestamp) < CACHE_EXPIRATION_TIME;
    }
}

