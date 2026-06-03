package com.receiptmanager.util

import android.content.Context

object PinManager {

    private const val PREF_NAME   = "pin_prefs"
    private const val KEY_PIN     = "user_pin"
    private const val KEY_ENABLED = "pin_enabled"
    private const val KEY_FINGER  = "fingerprint_enabled"

    fun savePin(context: Context, pin: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PIN, null)
    }

    fun setPinEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun isPinEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
    }

    fun setFingerprintEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_FINGER, enabled).apply()
    }

    fun isFingerprintEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_FINGER, false)
    }

    fun clearPin(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    fun verifyPin(context: Context, input: String): Boolean {
        return getPin(context) == input
    }
}