package com.finalyear.fieldtrack.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.finalyear.fieldtrack.model.Visit

/**
 * Central SQLite database helper for the app.
 * Demonstrates: CREATE TABLE, INSERT, UPDATE, DELETE, SELECT (full CRUD).
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "fieldtrack.db"
        const val DB_VERSION = 1

        const val TABLE_VISITS = "visits"
        const val COL_ID = "id"
        const val COL_SITE = "site_name"
        const val COL_NOTES = "notes"
        const val COL_LAT = "latitude"
        const val COL_LNG = "longitude"
        const val COL_PHOTO = "photo_path"
        const val COL_TIME = "timestamp"
        const val COL_SYNCED = "synced"

        @Volatile private var INSTANCE: DatabaseHelper? = null
        fun getInstance(context: Context): DatabaseHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context.applicationContext).also { INSTANCE = it }
            }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_VISITS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SITE TEXT NOT NULL,
                $COL_NOTES TEXT,
                $COL_LAT REAL,
                $COL_LNG REAL,
                $COL_PHOTO TEXT,
                $COL_TIME INTEGER,
                $COL_SYNCED INTEGER DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VISITS")
        onCreate(db)
    }

    // ---------- INSERT ----------
    fun insertVisit(v: Visit): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_SITE, v.siteName)
            put(COL_NOTES, v.notes)
            put(COL_LAT, v.latitude)
            put(COL_LNG, v.longitude)
            put(COL_PHOTO, v.photoPath)
            put(COL_TIME, v.timestamp)
            put(COL_SYNCED, v.synced)
        }
        return db.insert(TABLE_VISITS, null, cv)
    }

    // ---------- UPDATE ----------
    fun updateVisit(v: Visit): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_SITE, v.siteName)
            put(COL_NOTES, v.notes)
            put(COL_LAT, v.latitude)
            put(COL_LNG, v.longitude)
            put(COL_PHOTO, v.photoPath)
            put(COL_SYNCED, v.synced)
        }
        return db.update(TABLE_VISITS, cv, "$COL_ID = ?", arrayOf(v.id.toString()))
    }

    fun markSynced(id: Long) {
        val db = writableDatabase
        val cv = ContentValues().apply { put(COL_SYNCED, 1) }
        db.update(TABLE_VISITS, cv, "$COL_ID = ?", arrayOf(id.toString()))
    }

    // ---------- DELETE ----------
    fun deleteVisit(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_VISITS, "$COL_ID = ?", arrayOf(id.toString()))
    }

    // ---------- SELECT ----------
    fun getAllVisits(): List<Visit> {
        val list = mutableListOf<Visit>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_VISITS, null, null, null, null, null, "$COL_TIME DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToVisit(it))
            }
        }
        return list
    }

    fun getUnsyncedVisits(): List<Visit> {
        val list = mutableListOf<Visit>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_VISITS, null, "$COL_SYNCED = ?", arrayOf("0"), null, null, null
        )
        cursor.use {
            while (it.moveToNext()) list.add(cursorToVisit(it))
        }
        return list
    }

    fun getVisitCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_VISITS", null)
        cursor.use {
            it.moveToFirst()
            return it.getInt(0)
        }
    }

    private fun cursorToVisit(c: android.database.Cursor): Visit {
        return Visit(
            id = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
            siteName = c.getString(c.getColumnIndexOrThrow(COL_SITE)),
            notes = c.getString(c.getColumnIndexOrThrow(COL_NOTES)) ?: "",
            latitude = c.getDouble(c.getColumnIndexOrThrow(COL_LAT)),
            longitude = c.getDouble(c.getColumnIndexOrThrow(COL_LNG)),
            photoPath = c.getString(c.getColumnIndexOrThrow(COL_PHOTO)),
            timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TIME)),
            synced = c.getInt(c.getColumnIndexOrThrow(COL_SYNCED))
        )
    }
}
