package com.example.quranapp.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.quranapp.R

val NeiriziFont = FontFamily(Font(R.font.neirizi))
val EstedadFont = FontFamily(Font(R.font.estedad))
val SGKaraFont = FontFamily(Font(R.font.sgkara))
val Quran1Font = FontFamily(Font(R.font.quran1))
val BadrFont = FontFamily(Font(R.font.badr))
val MoshafFont = FontFamily(Font(R.font.moshaf))

data class FontOption(val key: String, val label: String, val family: FontFamily?)

/** فهرست یکسان همه فونت‌های موجود در برنامه؛ هم برای متن قرآن و هم برای ترجمه/تفسیر قابل انتخاب است */
val AllFontOptions = listOf(
    FontOption("neirizi", "نیریزی", NeiriziFont),
    FontOption("sgkara", "اس‌جی‌کارا", SGKaraFont),
    FontOption("quran1", "قرآن ۱", Quran1Font),
    FontOption("badr", "بدر", BadrFont),
    FontOption("moshaf", "مصحف", MoshafFont),
    FontOption("estedad", "استعداد", EstedadFont),
    FontOption("system", "پیش‌فرض سیستم", null)
)

// نام‌های قدیمی برای سازگاری با بقیه کد؛ هر دو از همان فهرست یکسان می‌خوانند
val QuranFontOptions = AllFontOptions
val TranslationFontOptions = AllFontOptions

fun quranFontByKey(key: String): FontFamily? =
    AllFontOptions.firstOrNull { it.key == key }?.family ?: NeiriziFont

fun translationFontByKey(key: String): FontFamily? =
    AllFontOptions.firstOrNull { it.key == key }?.family ?: EstedadFont
