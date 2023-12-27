import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "subjects.db"
        const val DATABASE_VERSION = 2
        const val TABLE_SUBJECTS = "subjects"
        const val COLUMN_ID = "_id"
        const val COLUMN_SUBJECT_NAME = "subject_name"
        var COLUMN_ELAPSED_TIME = "elapsed_time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_SUBJECTS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_SUBJECT_NAME TEXT NOT NULL, $COLUMN_ELAPSED_TIME INTEGER NOT NULL)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SUBJECTS")
        onCreate(db)
    }

    fun insertSubject(subjectName: String, elapsedTime: Long): Long {
        val values = ContentValues()
        values.put(COLUMN_SUBJECT_NAME, subjectName)
        values.put(COLUMN_ELAPSED_TIME, elapsedTime)

        val db = this.writableDatabase
        return db.insert(TABLE_SUBJECTS, null, values)
    }

    fun getTotalElapsedTime(): Long {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COLUMN_ELAPSED_TIME) AS total FROM $TABLE_SUBJECTS", null)

        var total: Long = 0
        if (cursor.moveToFirst()) {
            total = cursor.getLong(cursor.getColumnIndex("total"))
        }
        cursor.close()
        return total
    }

    fun getAllSubjects(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_SUBJECTS", null)
    }

    fun deleteSubject(subjectId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_SUBJECTS, "$COLUMN_ID=?", arrayOf(subjectId.toString()))
        db.close()
    }
}
