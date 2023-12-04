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

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mContext = context

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return mInflater.inflate(R.layout.subject_item, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        super.bindView(view, context, cursor)

        val deleteButton = view?.findViewById<Button>(R.id.deleteButton)
        deleteButton?.setOnClickListener {
            val id = cursor?.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID))
            id?.let {
                val dbHelper = DBHelper(mContext)
                dbHelper.deleteSubject(it)
                this.swapCursor(dbHelper.getAllSubjects())
            }
        }
    }
}
