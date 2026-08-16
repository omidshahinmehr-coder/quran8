package com.example.quranapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

object QuranDatabaseHelper {

    @Volatile private var db: SQLiteDatabase? = null

    fun getDatabase(context: Context): SQLiteDatabase {
        db?.let { return it }
        synchronized(this) {
            db?.let { return it }
            val dbFile = context.getDatabasePath("quran.db")
            if (!dbFile.exists()) {
                dbFile.parentFile?.mkdirs()
                context.assets.open("quran.db").use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            val opened = SQLiteDatabase.openDatabase(
                dbFile.path, null, SQLiteDatabase.OPEN_READONLY
            )
            db = opened
            return opened
        }
    }
}
