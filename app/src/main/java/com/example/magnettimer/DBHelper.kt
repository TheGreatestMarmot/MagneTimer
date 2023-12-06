import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // 정적 필드들을 선언하는 companion object
    companion object {
        // 데이터베이스의 이름
        const val DATABASE_NAME = "subjects.db"
        // 데이터베이스의 버전
        const val DATABASE_VERSION = 2
        // 테이블의 이름
        const val TABLE_SUBJECTS = "subjects"
        // 테이블의 컬럼 이름들
        const val COLUMN_ID = "_id"
        const val COLUMN_SUBJECT_NAME = "subject_name"
        var COLUMN_ELAPSED_TIME = "elapsed_time"
    }

    // 데이터베이스가 생성될 때 호출되는 메서드
    override fun onCreate(db: SQLiteDatabase) {
        // 테이블 생성 쿼리
        val createTableQuery = "CREATE TABLE $TABLE_SUBJECTS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_SUBJECT_NAME TEXT NOT NULL, $COLUMN_ELAPSED_TIME INTEGER NOT NULL)"
        // 쿼리 실행
        db.execSQL(createTableQuery)
    }

    // 데이터베이스의 버전이 변경될 때 호출되는 메서드
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 기존 테이블을 삭제하고 새로 만듦
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SUBJECTS")
        onCreate(db)
    }

    // 과목을 데이터베이스에 추가하는 메서드
    fun insertSubject(subjectName: String, elapsedTime: Long): Long {
        // 추가할 값을 저장하는 ContentValues 객체 생성
        val values = ContentValues()
        values.put(COLUMN_SUBJECT_NAME, subjectName)
        values.put(COLUMN_ELAPSED_TIME, elapsedTime)

        // 쓰기 가능한 데이터베이스를 가져옴
        val db = this.writableDatabase
        // 데이터를 삽입하고 삽입된 행의 ID를 반환
        return db.insert(TABLE_SUBJECTS, null, values)
    }

    // 모든 과목의 시간을 합산하여 반환하는 메서드
    fun getTotalElapsedTime(): Long {
        // 읽기 가능한 데이터베이스를 가져옴
        val db = this.readableDatabase
        // 모든 과목의 시간을 합산하는 쿼리를 실행하고 결과를 Cursor로 반환
        val cursor = db.rawQuery("SELECT SUM($COLUMN_ELAPSED_TIME) AS total FROM $TABLE_SUBJECTS", null)

        var total: Long = 0
        if (cursor.moveToFirst()) {
            total = cursor.getLong(cursor.getColumnIndex("total"))
        }
        cursor.close()
        return total
    }


    // 모든 과목을 가져오는 메서드
    fun getAllSubjects(): Cursor {
        // 읽기 가능한 데이터베이스를 가져옴
        val db = this.readableDatabase
        // 모든 과목을 선택하는 쿼리를 실행하고 결과를 Cursor로 반환
        return db.rawQuery("SELECT * FROM $TABLE_SUBJECTS", null)
    }

    // 특정 과목을 삭제하는 메서드
    fun deleteSubject(id: Long) {
        // 쓰기 가능한 데이터베이스를 가져옴
        val db = this.writableDatabase
        // 주어진 ID의 과목을 삭제
        db.delete(TABLE_SUBJECTS, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
    }

}