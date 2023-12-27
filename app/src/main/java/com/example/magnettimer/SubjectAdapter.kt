package com.example.magnettimer

import DBHelper
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.example.MagnetTimer.R

class SubjectAdapter(
    context: Context,
    layout: Int,
    c: Cursor,
    from: Array<String>,
    to: IntArray,
    flags: Int
) : SimpleCursorAdapter(context, layout, c, from, to, flags) {

    // LayoutInflater를 가져오고 Context를 저장합니다.
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mContext = context

    // 새로운 뷰를 생성하는 메서드입니다. 여기서는 subject_item 레이아웃을 인플레이트합니다.
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup): View {
        return mInflater.inflate(R.layout.subject_item, parent, false)
    }

    // 뷰에 데이터를 바인딩하는 메서드입니다.
    // 여기서는 SimpleCursorAdapter가 제공하는 기본 바인딩 작업을 수행한 후, 삭제 버튼의 클릭 리스너를 설정합니다.
    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        super.bindView(view, context, cursor)

        // 삭제 버튼을 가져옵니다.
        val deleteButton = view?.findViewById<Button>(R.id.deleteButton)
        deleteButton?.setOnClickListener {
            // 항목의 ID를 가져옵니다.
            val id = cursor?.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID))
            id?.let {
                // DBHelper를 이용해 항목을 삭제하고, 변경된 데이터로 Cursor를 교체합니다.
                val dbHelper = DBHelper(mContext)
                dbHelper.deleteSubject(it)
                this.swapCursor(dbHelper.getAllSubjects())
            }
        }
    }
}
