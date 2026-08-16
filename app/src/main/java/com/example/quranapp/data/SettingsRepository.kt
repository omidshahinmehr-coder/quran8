package com.example.quranapp.data

import android.content.Context

data class AppSettings(
    val quranFontKey: String = "neirizi",
    val quranFontSize: Float = 22f,
    val translationFontKey: String = "estedad",
    val translationFontSize: Float = 16f,
    val translationLanguage: String = "fa" // "fa" or "en"
)

class SettingsRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("quran_settings", Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        quranFontKey = prefs.getString(KEY_QURAN_FONT, "neirizi") ?: "neirizi",
        quranFontSize = prefs.getFloat(KEY_QURAN_SIZE, 22f),
        translationFontKey = prefs.getString(KEY_TR_FONT, "estedad") ?: "estedad",
        translationFontSize = prefs.getFloat(KEY_TR_SIZE, 16f),
        translationLanguage = prefs.getString(KEY_TR_LANG, "fa") ?: "fa"
    )

    fun save(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_QURAN_FONT, settings.quranFontKey)
            .putFloat(KEY_QURAN_SIZE, settings.quranFontSize)
            .putString(KEY_TR_FONT, settings.translationFontKey)
            .putFloat(KEY_TR_SIZE, settings.translationFontSize)
            .putString(KEY_TR_LANG, settings.translationLanguage)
            .apply()
    }

    companion object {
        private const val KEY_QURAN_FONT = "quran_font_key"
        private const val KEY_QURAN_SIZE = "quran_font_size"
        private const val KEY_TR_FONT = "translation_font_key"
        private const val KEY_TR_SIZE = "translation_font_size"
        private const val KEY_TR_LANG = "translation_language"
    }
}
