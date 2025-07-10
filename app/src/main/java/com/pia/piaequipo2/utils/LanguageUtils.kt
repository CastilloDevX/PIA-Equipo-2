package com.pia.piaequipo2.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.*

fun saveLanguage(context: Context, langCode: String) {
    val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    prefs.edit().putString("lang", langCode).apply()
}

fun getSavedLanguage(context: Context): String {
    val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    return prefs.getString("lang", "es") ?: "es"
}

fun setLocale(context: Context, langCode: String): Context {
    val locale = Locale(langCode)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    return context.createConfigurationContext(config)
}