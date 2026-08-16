package com.example.quranapp.data

import android.content.Context
import android.database.Cursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val TRANSLATOR_FA = "انصاریان"
const val TRANSLATOR_EN = "Saheeh International"

class QuranRepository(private val context: Context) {

    private val db get() = QuranDatabaseHelper.getDatabase(context)

    suspend fun getAllAyat(): List<AyahEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<AyahEntity>()
        val cursor = db.rawQuery(
            "SELECT globalAyahId, surahNumber, surahNameFa, ayahNumber, textArabic " +
                "FROM ayah ORDER BY globalAyahId",
            null
        )
        cursor.use {
            while (it.moveToNext()) result += it.toAyah()
        }
        result
    }

    suspend fun getAllTranslations(translator: String = TRANSLATOR_FA): Map<Int, TranslationEntity> = withContext(Dispatchers.IO) {
        val result = HashMap<Int, TranslationEntity>()
        val cursor = db.rawQuery(
            "SELECT globalAyahId, translator, textFa FROM translation WHERE translator = ?",
            arrayOf(translator)
        )
        cursor.use {
            while (it.moveToNext()) {
                val gid = it.getInt(0)
                result[gid] = TranslationEntity(gid, it.getString(1), it.getString(2))
            }
        }
        result
    }

    suspend fun getAllTafsirAyahIds(): Set<Int> = withContext(Dispatchers.IO) {
        val result = mutableSetOf<Int>()
        val cursor = db.rawQuery("SELECT startAyahId, endAyahId FROM tafsir", null)
        cursor.use {
            while (it.moveToNext()) {
                val start = it.getInt(0)
                val end = it.getInt(1)
                for (id in start..end) result += id
            }
        }
        result
    }

    suspend fun getJuzList(): List<JuzInfo> = withContext(Dispatchers.IO) {
        val result = mutableListOf<JuzInfo>()
        val cursor = db.rawQuery(
            """
            SELECT j.juzNumber, j.startAyahId, j.endAyahId, a.surahNameFa, a.ayahNumber
            FROM juz j
            JOIN ayah a ON a.globalAyahId = j.startAyahId
            ORDER BY j.juzNumber
            """.trimIndent(),
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                result += JuzInfo(
                    juzNumber = it.getInt(0),
                    startAyahId = it.getInt(1),
                    endAyahId = it.getInt(2),
                    startSurahName = it.getString(3),
                    startAyahNumber = it.getInt(4)
                )
            }
        }
        result
    }

    suspend fun getAyahRange(startId: Int, endId: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<AyahEntity>()
        val cursor = db.rawQuery(
            "SELECT globalAyahId, surahNumber, surahNameFa, ayahNumber, textArabic " +
                "FROM ayah WHERE globalAyahId BETWEEN ? AND ? ORDER BY globalAyahId",
            arrayOf(startId.toString(), endId.toString())
        )
        cursor.use {
            while (it.moveToNext()) result += it.toAyah()
        }
        result
    }

    suspend fun getAyahIdsWithTafsirInRange(startId: Int, endId: Int): Set<Int> = withContext(Dispatchers.IO) {
        val result = mutableSetOf<Int>()
        val cursor = db.rawQuery(
            "SELECT startAyahId, endAyahId FROM tafsir WHERE startAyahId <= ? AND endAyahId >= ?",
            arrayOf(endId.toString(), startId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                val s = maxOf(it.getInt(0), startId)
                val e = minOf(it.getInt(1), endId)
                for (id in s..e) result += id
            }
        }
        result
    }

    suspend fun getSurahList(): List<SurahInfo> = withContext(Dispatchers.IO) {
        val result = mutableListOf<SurahInfo>()
        val cursor = db.rawQuery(
            "SELECT surahNumber, surahNameFa, COUNT(*) as cnt FROM ayah " +
                "GROUP BY surahNumber ORDER BY surahNumber",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                result += SurahInfo(
                    surahNumber = it.getInt(0),
                    surahNameFa = it.getString(1),
                    ayahCount = it.getInt(2)
                )
            }
        }
        result
    }

    suspend fun getSurah(surah: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<AyahEntity>()
        val cursor = db.rawQuery(
            "SELECT globalAyahId, surahNumber, surahNameFa, ayahNumber, textArabic " +
                "FROM ayah WHERE surahNumber = ? ORDER BY ayahNumber",
            arrayOf(surah.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                result += it.toAyah()
            }
        }
        result
    }

    suspend fun getTranslations(startId: Int, endId: Int, translator: String = TRANSLATOR_FA): List<TranslationEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<TranslationEntity>()
        val cursor = db.rawQuery(
            "SELECT globalAyahId, translator, textFa FROM translation " +
                "WHERE globalAyahId BETWEEN ? AND ? AND translator = ?",
            arrayOf(startId.toString(), endId.toString(), translator)
        )
        cursor.use {
            while (it.moveToNext()) {
                result += TranslationEntity(it.getInt(0), it.getString(1), it.getString(2))
            }
        }
        result
    }

    suspend fun getTafsirForSurah(surah: Int, language: String = "ar"): List<TafsirEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<TafsirEntity>()
        val cursor = db.rawQuery(
            "SELECT id, source, surahNumber, startAyahId, endAyahId, textFa, language FROM tafsir " +
                "WHERE surahNumber = ? AND language = ? ORDER BY startAyahId",
            arrayOf(surah.toString(), language)
        )
        cursor.use {
            while (it.moveToNext()) {
                result += TafsirEntity(
                    id = it.getLong(0),
                    source = it.getString(1),
                    surahNumber = it.getInt(2),
                    startAyahId = it.getInt(3),
                    endAyahId = it.getInt(4),
                    textFa = it.getString(5),
                    language = it.getString(6)
                )
            }
        }
        result
    }

    suspend fun getTafsirForAyah(globalAyahId: Int, language: String = "ar"): List<TafsirEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<TafsirEntity>()
        val cursor = db.rawQuery(
            "SELECT id, source, surahNumber, startAyahId, endAyahId, textFa, language FROM tafsir " +
                "WHERE ? BETWEEN startAyahId AND endAyahId AND language = ? ORDER BY startAyahId",
            arrayOf(globalAyahId.toString(), language)
        )
        cursor.use {
            while (it.moveToNext()) {
                result += TafsirEntity(
                    id = it.getLong(0),
                    source = it.getString(1),
                    surahNumber = it.getInt(2),
                    startAyahId = it.getInt(3),
                    endAyahId = it.getInt(4),
                    textFa = it.getString(5),
                    language = it.getString(6)
                )
            }
        }
        result
    }

    /** برای نمایش سریع اینکه کدام آیات تفسیر دارند (بدون بارگذاری کل متن) */
    suspend fun getAyahIdsWithTafsir(surah: Int): Set<Int> = withContext(Dispatchers.IO) {
        val result = mutableSetOf<Int>()
        val cursor = db.rawQuery(
            "SELECT startAyahId, endAyahId FROM tafsir WHERE surahNumber = ?",
            arrayOf(surah.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                val start = it.getInt(0)
                val end = it.getInt(1)
                for (id in start..end) result += id
            }
        }
        result
    }

    suspend fun search(
        rawQuery: String,
        includeQuran: Boolean = true,
        includeTranslation: Boolean = true,
        includeTafsir: Boolean = true
    ): List<SearchResult> = withContext(Dispatchers.IO) {
        val ftsQuery = toFtsQuery(rawQuery)
        if (ftsQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<SearchResult>()

        if (includeQuran) {
            val cursor = db.rawQuery(
                """
                SELECT a.globalAyahId, a.surahNumber, a.surahNameFa, a.ayahNumber,
                       snippet(ayah_fts, 0, '«', '»', '...', 8)
                FROM ayah_fts
                JOIN ayah a ON a.rowid = ayah_fts.rowid
                WHERE ayah_fts MATCH ?
                LIMIT 100
                """.trimIndent(),
                arrayOf(ftsQuery)
            )
            cursor.use {
                while (it.moveToNext()) results += it.toSearchResult("quran")
            }
        }

        if (includeTranslation) {
            val cursor = db.rawQuery(
                """
                SELECT a.globalAyahId, a.surahNumber, a.surahNameFa, a.ayahNumber,
                       snippet(translation_fts, 0, '«', '»', '...', 10)
                FROM translation_fts
                JOIN translation t ON t.rowid = translation_fts.rowid
                JOIN ayah a ON a.globalAyahId = t.globalAyahId
                WHERE translation_fts MATCH ?
                LIMIT 100
                """.trimIndent(),
                arrayOf(ftsQuery)
            )
            cursor.use {
                while (it.moveToNext()) results += it.toSearchResult("translation")
            }
        }

        if (includeTafsir) {
            val cursor = db.rawQuery(
                """
                SELECT a.globalAyahId, tf.surahNumber, a.surahNameFa, a.ayahNumber,
                       snippet(tafsir_fts, 0, '«', '»', '...', 12)
                FROM tafsir_fts
                JOIN tafsir tf ON tf.rowid = tafsir_fts.rowid
                JOIN ayah a ON a.globalAyahId = tf.startAyahId
                WHERE tafsir_fts MATCH ?
                LIMIT 100
                """.trimIndent(),
                arrayOf(ftsQuery)
            )
            cursor.use {
                while (it.moveToNext()) results += it.toSearchResult("tafsir")
            }
        }

        results
    }

    private fun Cursor.toAyah() = AyahEntity(
        globalAyahId = getInt(0),
        surahNumber = getInt(1),
        surahNameFa = getString(2),
        ayahNumber = getInt(3),
        textArabic = getString(4)
    )

    private fun Cursor.toSearchResult(kind: String) = SearchResult(
        globalAyahId = getInt(0),
        surahNumber = getInt(1),
        surahNameFa = getString(2),
        ayahNumber = getInt(3),
        snippet = getString(4) ?: "",
        kind = kind
    )

    /** تبدیل عبارت کاربر به سینتکس FTS4؛ اگر ورودی خالی یا فقط علائم باشد رشته خالی برمی‌گرداند */
    private fun toFtsQuery(raw: String): String {
        val normalized = raw.trim()
            .replace("ي", "ی").replace("ك", "ک")
        if (normalized.isEmpty()) return ""
        val terms = normalized.split(Regex("\\s+"))
            .map { it.replace(Regex("[\"'*]"), "") }
            .filter { it.isNotBlank() }
        if (terms.isEmpty()) return ""
        return terms.joinToString(" ") { "$it*" }
    }
}
