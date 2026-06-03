package com.receiptmanager.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    private const val PREF_NAME = "theme_prefs"
    private const val KEY_DARK  = "dark_mode"

    fun setDarkMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DARK, enabled).apply()
        applyTheme(enabled)
    }

    fun isDarkMode(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK, false)
    }

    fun applyTheme(darkMode: Boolean) {
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun applySavedTheme(context: Context) {
        applyTheme(isDarkMode(context))
    }
}