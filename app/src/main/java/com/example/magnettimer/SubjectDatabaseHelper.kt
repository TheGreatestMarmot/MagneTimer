package com.example.magnettimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object SubjectContract {
    // 테이블 내의 각 열 정의
    object SubjectEntry : BaseColumns {
        const val TABLE_NAME = "subjects"
        const val COLUMN_NAME_SUBJECT_NAME = "subject_name"
        const val COLUMN_NAME_SUBJECT_TIME = "subject_time"
    }
}

class SubjectDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // 테이블 생성 SQL 문
    private val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${SubjectContract.SubjectEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${SubjectContract.SubjectEntry.COLUMN_NAME_SUBJECT_NAME} TEXT," +
                "${SubjectContract.SubjectEntry.COLUMN_NAME_SUBJECT_TIME} INTEGER)"

    // 테이블 삭제 SQL 문
    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SubjectContract.SubjectEntry.TABLE_NAME}"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Subjects.db"
    }
}