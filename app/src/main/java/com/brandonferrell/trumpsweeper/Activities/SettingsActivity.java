package com.brandonferrell.trumpsweeper.activities;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.brandonferrell.trumpsweeper.R;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        Preference noAds = findPreference("no_ads");
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removePreference(noAds);

        Preference chinaVar = findPreference("china_variable");
        preferenceScreen.removePreference(chinaVar);

        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("china_variable", false)) {
            getPreferenceScreen().findPreference("china_slider_preference").setEnabled(false);
            getPreferenceScreen().findPreference("china_slider_preference").setSummary("Premium Feature: " + getPreferenceScreen().findPreference("china_slider_preference").getSummary());
        }

        getPreferenceScreen().findPreference("theme_preference").setEnabled(false);
        getPreferenceScreen().findPreference("theme_preference").setSummary("Coming Soon: " + getPreferenceScreen().findPreference("theme_preference").getSummary());


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

        if(StartActivity.themeMediaPlayer.isPlaying())
            StartActivity.themeMediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("music_preference", true))
            StartActivity.themeMediaPlayer.start();
    }
}
