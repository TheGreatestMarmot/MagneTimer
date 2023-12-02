package com.example.magnettimer

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns

class SubjectDataSource(context: Context) {

    private val dbHelper: SubjectDatabaseHelper = SubjectDatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    fun insertSubject(subjectName: String, subjectTime: Int): Long {
        val values = ContentValues().apply {
            put(SubjectContract.SubjectEntry.COLUMN_NAME_SUBJECT_NAME, subjectName)
            put(SubjectContract.SubjectEntry.COLUMN_NAME_SUBJECT_TIME, subjectTime)
        }

        return db.insert(SubjectContract.SubjectEntry.TABLE_NAME, null, values)
    }

    fun getAllSubjects(): List<Subject> {
        val projection = arrayOf(
            BaseColumns._ID,
            SubjectContract.SubjectEntry.COLUMN_NAME_SUBJECT_NAME,
            SubjectContract.SubjectEntry.COLUMN_NAME_SUBJECT_TIME
        )

        val sortOrder = "${BaseColumns._ID} DESC"
        val cursor = db.query(
            SubjectContract.SubjectEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )

        val subjects = mutableListOf<Subject>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val name =
                    getString(getColumnIndexOrThrow(SubjectContract.SubjectEntry.COLUMN_NAME_SUBJECT_NAME))
                val time =
                    getInt(getColumnIndexOrThrow(SubjectContract.SubjectEntry.COLUMN_NAME_SUBJECT_TIME))
                subjects.add(Subject(id, name, time))
            }
        }

        cursor.close()
        return subjects
    }
}