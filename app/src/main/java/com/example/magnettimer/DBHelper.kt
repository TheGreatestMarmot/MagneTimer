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
        const val COLUMN_ELAPSED_TIME = "elapsed_time" // 추가된 컬럼
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE $TABLE_SUBJECTS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_SUBJECT_NAME TEXT NOT NULL, $COLUMN_ELAPSED_TIME INTEGER NOT NULL);") // 테이블 생성 쿼리 수정
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SUBJECTS")
        onCreate(db)
    }

    fun insertSubject(subjectName: String, elapsedTime: Long): Long { // 메서드 수정
        val hours = (elapsedTime / (1000 * 60 * 60)).toInt()
        val minutes = ((elapsedTime / (1000 * 60)) % 60).toInt()
        val seconds = ((elapsedTime / 1000) % 60).toInt()
        var formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        val values = ContentValues()
        values.put(COLUMN_SUBJECT_NAME, subjectName)
        values.put(COLUMN_ELAPSED_TIME, formattedTime)
        val db = this.writableDatabase
        return db.insert(TABLE_SUBJECTS, null, values)
    }

    fun getAllSubjects(): Cursor { // 메서드 수정
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_SUBJECTS", null)
    }

    fun deleteSubject(id: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_SUBJECTS, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
    }
}
