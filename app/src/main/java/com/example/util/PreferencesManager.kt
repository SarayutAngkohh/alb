package com.example.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("along_the_books_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
        private const val KEY_DEFAULT_ALERT_DISTANCE = "default_alert_distance"
    }

    var isDisclaimerAccepted: Boolean
        get() = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, value).apply()

    var defaultAlertDistance: Int
        get() = prefs.getInt(KEY_DEFAULT_ALERT_DISTANCE, 500)
        set(value) = prefs.edit().putInt(KEY_DEFAULT_ALERT_DISTANCE, value).apply()
}
