package io.wyrmise.meen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;


public class SettingsActivity extends ActionBarActivity
{
    private Toolbar toolbar;
    public static final String KEY_PREF_THEME = "pref_background_pic";
    public static final String KEY_PREF_ACTION_BAR = "pref_actionbar_color";
    public static final String KEY_NIGHT_MODE = "pref_night_mode";
    public static final String KEY_POPUP_MODE = "pref_popup_mode";
    public static final String KEY_FAB_THEME = "pref_fab_color";
    public static final String KEY_DELAY_MODE = "pref_delay_mode";
    public static final String KEY_FONT_MODE = "pref_font_mode";
    public static final String KEY_SWIPE_BACK = "pref_swipe_back";

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {

        if(MainActivity.isNightMode){
            setTheme(R.style.NightActionBar);
        } else {
            getActionBarColor();
        }
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

    }

    public void getActionBarColor(){
        switch(MainActivity.colorCode){
            case -1:
                setTheme(R.style.DefaultTheme);
                break;
            case 1:
                setTheme(R.style.GreenActionBar);
                break;
            case 2:
                setTheme(R.style.LightGreenActionBar);
                break;
            case 3:
                break;
            case 4:
                setTheme(R.style.BlueActionBar);
                break;
            case 5:
                setTheme(R.style.CyanActionBar);
                break;
            case 6:
                setTheme(R.style.TealActionBar);
                break;
            case 7:
                setTheme(R.style.RedActionBar);
                break;
            case 8:
                setTheme(R.style.OrangeActionBar);
                break;
            case 9:
                break;
            case 10:
                setTheme(R.style.PurpleActionBar);
                break;
            case 11:
                setTheme(R.style.PinkActionBar);
                break;
            case 12:
                setTheme(R.style.BrownActionBar);
                break;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

    }
    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            final Preference backgroundPref = (ListPreference) getPreferenceManager().findPreference(KEY_PREF_THEME);
            backgroundPref.setSummary(((ListPreference) backgroundPref).getEntry());
            backgroundPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    if(backgroundPref instanceof ListPreference)
                        backgroundPref.setSummary(((ListPreference) backgroundPref).getEntry());
                    return true;
                }
            });
            final Preference actionBarPref = (ListPreference) getPreferenceManager().findPreference(KEY_PREF_ACTION_BAR);
            actionBarPref.setSummary(((ListPreference) actionBarPref).getEntry());
            actionBarPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    if(actionBarPref instanceof ListPreference)
                        actionBarPref.setSummary(((ListPreference) actionBarPref).getEntry());
                    return true;
                }
            });
            final Preference fabPref = (ListPreference) getPreferenceManager().findPreference(KEY_FAB_THEME);
            fabPref.setSummary(((ListPreference) fabPref).getEntry());
            fabPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    if(fabPref instanceof ListPreference)
                        fabPref.setSummary(((ListPreference) fabPref).getEntry());
                    return true;
                }
            });
            final Preference fontPref = (ListPreference) getPreferenceManager().findPreference(KEY_FONT_MODE);
            fontPref.setSummary(((ListPreference) fontPref).getEntry());
            fontPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(fontPref instanceof ListPreference)
                        fontPref.setSummary(((ListPreference) fontPref).getEntry());
                    return true;
                }
            });
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            if (key.equals(KEY_PREF_THEME)) {
                Preference backgroundPref = findPreference(key);
                backgroundPref.setSummary(((ListPreference) backgroundPref).getEntry());
            } else if(key.equals(KEY_PREF_ACTION_BAR)){
                Preference actionBarPref = findPreference(key);
                actionBarPref.setSummary(((ListPreference) actionBarPref).getEntry());
            } else if(key.equals(KEY_FAB_THEME)){
                Preference fabPref = findPreference(key);
                fabPref.setSummary(((ListPreference) fabPref).getEntry());
            } else if(key.equals(KEY_FONT_MODE)){
                Preference fontPref = findPreference(key);
                fontPref.setSummary(((ListPreference) fontPref).getEntry());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
