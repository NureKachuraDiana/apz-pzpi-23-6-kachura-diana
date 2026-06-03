package com.example.ecomonitormobile.localization

import android.content.Context
import java.util.Locale

object AppLanguage {
    const val EN = "en"
    const val UK = "uk"
    const val DEFAULT = EN

    fun normalize(code: String?): String = when (code?.trim()?.lowercase()) {
        UK, "ua" -> UK
        else -> EN
    }

    fun toLocale(code: String): Locale = when (normalize(code)) {
        UK -> Locale.forLanguageTag("uk")
        else -> Locale.ENGLISH
    }
}

class LocalePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): String = AppLanguage.normalize(prefs.getString(KEY_LANGUAGE, AppLanguage.DEFAULT))

    fun set(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, AppLanguage.normalize(languageCode)).apply()
    }

    companion object {
        private const val PREFS_NAME = "eco_monitor_locale"
        private const val KEY_LANGUAGE = "language"
    }
}
