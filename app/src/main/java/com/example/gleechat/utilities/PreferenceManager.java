package com.example.gleechat.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context){
        // Initializes the PreferenceManager with a shared preference object based on the provided context.
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, Boolean value){
        // Stores a boolean value in the shared preferences with the provided key.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBoolean(String key){
        // Retrieves a boolean value from the shared preferences based on the provided key.
        // If the value does not exist, it returns a default value of false.
        return sharedPreferences.getBoolean(key, false);
    }

    public void putString(String key, String value){
        // Stores a string value in the shared preferences with the provided key
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key){
        // Retrieves a string value from the shared preferences based on the provided key.
        // If the value does not exist, it returns null.
        return sharedPreferences.getString(key, null);
    }
    public void clear(){
        // Clears all the values stored in the shared preferences.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
