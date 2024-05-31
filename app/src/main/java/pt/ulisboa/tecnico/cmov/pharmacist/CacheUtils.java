package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Pharmacy;

public class CacheUtils {
    private static final String TAG = "CacheUtils";
    private static final String PHARMACY_CACHE_FILE = "pharmacy_cache.json";
    private static final String FAVORITE_PHARMACY_CACHE_FILE = "favorite_pharmacy_cache.json";
    private static final long CACHE_VALIDITY_PERIOD = TimeUnit.MINUTES.toMillis(1);


    public static boolean isCacheValid(Context context) {
        File cacheFile = new File(context.getCacheDir(), PHARMACY_CACHE_FILE);
        boolean isValid = cacheFile.exists() && (System.currentTimeMillis() - cacheFile.lastModified()) < CACHE_VALIDITY_PERIOD;
        Log.d(TAG, "isCacheValid: " + isValid);
        return isValid;
    }

    public static List<Pharmacy> getPharmacies(Context context) {
        List<Pharmacy> pharmacies = getCachedData(context, PHARMACY_CACHE_FILE, new TypeToken<List<Pharmacy>>() {}.getType());
        Log.d(TAG, "getPharmacies: " + (pharmacies != null ? "Loaded from cache" : "Cache is empty"));
        return pharmacies;
    }

    public static void savePharmacies(Context context, List<Pharmacy> pharmacies) {
        Log.d(TAG, "savePharmacies: Saving pharmacies to cache");
        saveData(context, pharmacies, PHARMACY_CACHE_FILE);
    }

    public static List<String> getFavoritePharmacies(Context context) {
        List<String> favoritePharmacies = getCachedData(context, FAVORITE_PHARMACY_CACHE_FILE, new TypeToken<List<String>>() {}.getType());
        Log.d(TAG, "getFavoritePharmacies: " + (favoritePharmacies != null ? "Loaded from cache" : "Cache is empty"));
        return favoritePharmacies;
    }

    public static void saveFavoritePharmacies(Context context, Set<String> favoritePharmacies) {
        Log.d(TAG, "saveFavoritePharmacies: Saving favorite pharmacies to cache");
        saveData(context, new ArrayList<>(favoritePharmacies), FAVORITE_PHARMACY_CACHE_FILE);
    }

    private static <T> T getCachedData(Context context, String fileName, Type type) {
        File cacheFile = new File(context.getCacheDir(), fileName);
        if (cacheFile.exists()) {
            Log.d(TAG, "getCachedData: Reading from cache file " + fileName);
            try (FileReader reader = new FileReader(cacheFile)) {
                T data = new Gson().fromJson(reader, type);
                Log.d(TAG, "getCachedData: Successfully read from cache file " + fileName);
                return data;
            } catch (IOException e) {
                Log.e(TAG, "getCachedData: Error reading from cache file " + fileName, e);
            }
        } else {
            Log.d(TAG, "getCachedData: Cache file " + fileName + " does not exist");
        }
        return null;
    }

    private static void saveData(Context context, Object data, String fileName) {
        File cacheFile = new File(context.getCacheDir(), fileName);
        Log.d(TAG, "saveData: Writing to cache file " + fileName);
        try (FileWriter writer = new FileWriter(cacheFile)) {
            new Gson().toJson(data, writer);
            Log.d(TAG, "saveData: Successfully wrote to cache file " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "saveData: Error writing to cache file " + fileName, e);
        }
    }
}



