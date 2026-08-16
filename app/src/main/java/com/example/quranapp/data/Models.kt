package com.example.quranapp.data

data class AyahEntity(
    val globalAyahId: Int,
    val surahNumber: Int,
    val surahNameFa: String,
    val ayahNumber: Int,
    val textArabic: String
)

data class TranslationEntity(
    val globalAyahId: Int,
    val translator: String,
    val textFa: String
)

data class TafsirEntity(
    val id: Long,
    val source: String,
    val surahNumber: Int,
    val startAyahId: Int,
    val endAyahId: Int,
    val textFa: String,
    val language: String = "ar" // "ar" یا "fa"
)

data class SearchResult(
    val globalAyahId: Int,
    val surahNumber: Int,
    val surahNameFa: String,
    val ayahNumber: Int,
    val snippet: String,
    val kind: String // "quran" | "translation" | "tafsir"
)

data class SurahInfo(
    val surahNumber: Int,
    val surahNameFa: String,
    val ayahCount: Int
)

data class JuzInfo(
    val juzNumber: Int,
    val startAyahId: Int,
    val endAyahId: Int,
    val startSurahName: String,
    val startAyahNumber: Int
)

/** یک آیتم در فهرست پیوسته‌ی متن کامل قرآن (برای صفحه اصلی) */
sealed class ReadingItem {
    data class SurahHeader(val surahNumber: Int, val surahNameFa: String) : ReadingItem()
    data class Bismillah(val surahNumber: Int) : ReadingItem()
    data class Ayah(val ayah: AyahEntity) : ReadingItem()
}
