package com.priyansh.vedicMaths;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private static final String PREF_NAME = "vedic_settings";
    private static final String KEY_HAPTIC = "haptic_enabled";
    private static final String KEY_MUSIC = "music_enabled";
    private static final String KEY_SOUND = "sound_enabled"; // NEW

    // HAPTICS
    public static void setHapticsEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply();
    }

    public static boolean isHapticsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_HAPTIC, true);
    }

    // MUSIC
    public static void setMusicEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_MUSIC, enabled).apply();
    }

    public static boolean isMusicEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_MUSIC, true);
    }

    // SOUND EFFECTS (NEW)
    public static void setSoundEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public static boolean isSoundEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SOUND, true);
    }
}