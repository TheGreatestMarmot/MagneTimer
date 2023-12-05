package com.example.MagnetTimer

import DBHelper
import DBHelper.Companion.COLUMN_ELAPSED_TIME
import DBHelper.Companion.COLUMN_SUBJECT_NAME
import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.magnettimer.SubjectAdapter
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var elapsedTimeTextView: TextView
    private var totalElapsedTime = 0L
    private var nfcPendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setupUI()
        setupNFC()
        setupStartButton()
        setupNfcPendingIntent()

        val ellipse1 = findViewById<View>(R.id.ellipse_1)
        startRotationAnimation(ellipse1)


    }

    private fun startRotationAnimation(view: View) {
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        rotation.duration = 2000 // 애니메이션의 기간 설정
        rotation.repeatCount = ObjectAnimator.INFINITE // 무한 반복 설정
        rotation.interpolator = LinearInterpolator() // 선형 보간 사용

        rotation.start()
    }

        private fun setupUI() {
            window.statusBarColor = Color.parseColor("#050625")
            elapsedTimeTextView = findViewById(R.id.totalElapsedTimeTextView)
        }

        private fun setupNFC() {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            when {
                nfcAdapter == null -> showToast("이 기기에서는 NFC가 지원되지 않습니다.")
                !nfcAdapter!!.isEnabled -> showToast("NFC가 활성화되어 있지 않습니다.")
                else -> showToast("NFC가 활성화 되었습니다.")
            }
        }

        private fun setupStartButton() {
            val startButton = findViewById<Button>(R.id.timeStart)
            startButton.setOnClickListener {
                enableNfcForegroundDispatch()
                startActivity(Intent(this, timer_on::class.java))
            }
        }

        private fun setupNfcPendingIntent() {
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            nfcPendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        }

        private fun showToast(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        private fun enableNfcForegroundDispatch() {
            nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        }

        override fun onResume() {
            super.onResume()
            updateSubjectsList()
            nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        }

        private fun updateSubjectsList() {
            val dbHelper = DBHelper(this)
            val cursor = dbHelper.getAllSubjects()
            totalElapsedTime = 0
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val columnIndex = cursor.getColumnIndex(COLUMN_ELAPSED_TIME)
                    if (columnIndex != -1) {
                        totalElapsedTime += cursor.getLong(columnIndex)
                    }
                    cursor.moveToNext()
                }
            }
            elapsedTimeTextView.text = convertMillisToTimeFormat(totalElapsedTime)
            val adapter = SubjectAdapter(
                this,
                R.layout.subject_item,
                cursor,
                arrayOf(COLUMN_SUBJECT_NAME, COLUMN_ELAPSED_TIME),
                intArrayOf(R.id.subjectNameTextView, R.id.timeTextView),
                0
            )
            val listView = findViewById<ListView>(R.id.listView)
            listView.adapter = adapter
        }

        private fun convertMillisToTimeFormat(millis: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(millis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

