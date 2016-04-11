package com.brandonferrell.trumpsweeper.Activities;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;

import com.brandonferrell.trumpsweeper.R;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        switch(key) {
            case "tutorial_preference":

                break;
            case "theme_preference":
                Log.d("DEBUG", sharedPreferences.getString(key, null));
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        StartActivity.themeMediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("music_preference", true))
            StartActivity.themeMediaPlayer.start();
    }
}
