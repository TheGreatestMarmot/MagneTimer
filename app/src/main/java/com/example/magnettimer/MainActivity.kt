package com.example.MagnetTimer

import DBHelper
import DBHelper.Companion.COLUMN_ELAPSED_TIME
import DBHelper.Companion.COLUMN_SUBJECT_NAME
import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.example.magnettimer.SubjectAdapter
import java.util.concurrent.TimeUnit
import com.example.MagnetTimer.timer_on
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.concurrent.fixedRateTimer


class MainActivity : AppCompatActivity() {

    // NFC 어댑터를 저장하는 변수
    private var nfcAdapter: NfcAdapter? = null

    // 전체 경과 시간을 표시하는 TextView
    private lateinit var elapsedTimeTextView: TextView

    // 전체 경과 시간을 저장하는 변수
    private var totalElapsedTime = 0L

    // NFC 이벤트가 발생했을 때 실행할 PendingIntent
    private var nfcPendingIntent: PendingIntent? = null

    private val TIMER_ON_REQUEST_CODE = 1
    private lateinit var totalElapsedTimeTextView: TextView

    // DBHelper 객체 생성
    private lateinit var dbHelper: DBHelper

    // 액티비티가 처음 생성될 때 호출되는 메서드
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        totalElapsedTimeTextView = findViewById(R.id.totalElapsedTimeTextView)

        // DBHelper 객체 초기화
        dbHelper = DBHelper(this)

        // UI 설정 메서드 호출
        setupUI()

        // NFC 설정 메서드 호출
        setupNFC()

        // 시작 버튼 설정 메서드 호출
        setupStartButton()

        // NFC PendingIntent 설정 메서드 호출
        setupNfcPendingIntent()

        // 회전 애니메이션 시작 메서드 호출
        val ellipse1 = findViewById<View>(R.id.ellipse_1)
        startRotationAnimation(ellipse1)

        fixedRateTimer("timer", false, 0L, 300) {
            this@MainActivity.runOnUiThread {
                updateTotalTime()
            }
        }
    }



    // 주어진 뷰에 대해 회전 애니메이션을 시작하는 메서드
    private fun startRotationAnimation(view: View) {
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        rotation.duration = 2000 // 애니메이션의 기간 설정
        rotation.repeatCount = ObjectAnimator.INFINITE // 무한 반복 설정
        rotation.interpolator = LinearInterpolator() // 선형 보간 사용

        rotation.start() // 애니메이션 시작
    }

    // UI를 설정하는 메서드
    private fun setupUI() {
        window.statusBarColor = Color.parseColor("#050625")
        elapsedTimeTextView = findViewById(R.id.totalElapsedTimeTextView)

        val onlyDate: LocalDate = LocalDate.now()
        val yesterday = onlyDate.minusDays(1)
        val tommorow = onlyDate.plusDays(1)

        val days = DateTimeFormatter.ofPattern("M월 d일")

        val todayView = findViewById<TextView>(R.id.today) ?: return
        val yesterdayView = findViewById<TextView>(R.id.yesterday) ?: return
        val tomorrowView = findViewById<TextView>(R.id.tommorow) ?: return

        val formattedToday = onlyDate.format(days)
        val formattedYesterday = yesterday.format(days)
        val formattedTommorow = tommorow.format(days)

        todayView.text = formattedToday
        yesterdayView.text = formattedYesterday
        tomorrowView.text = formattedTommorow
    }

    // NFC를 설정하는 메서드
    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        when {
            nfcAdapter == null -> showToast("이 기기에서는 NFC가 지원되지 않습니다.")
            !nfcAdapter!!.isEnabled -> showToast("NFC가 활성화되어 있지 않습니다.")
            else -> showToast("NFC가 활성화 되었습니다.")
        }
    }


    // 시작 버튼을 설정하는 메서드
    private fun setupStartButton() {
        val startButton = findViewById<Button>(R.id.timeStart)
        startButton.setOnClickListener {
            // NFC 받기 활성화
            enableNfcForegroundDispatch()
            // timer_on 액티비티로 이동
            startActivity(Intent(this, timer_on::class.java))
        }
    }

    // NFC PendingIntent를 설정하는 메서드
    private fun setupNfcPendingIntent() {
        // 현재 액티비티를 대상으로 하는 Intent 생성
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        // PendingIntent 생성
        nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    // 토스트 메시지를 보여주는 메서드
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // NFC 받기를 활성화하는 메서드
    private fun enableNfcForegroundDispatch() {
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
    }

    // 액티비티가 활성화될 때 호출되는 메서드
    override fun onResume() {
        super.onResume()
        // 과목 리스트 업데이트
        updateSubjectsList()
        // 전체 시간 업데이트
        updateTotalTime(totalElapsedTime)
        // NFC 받기 활성화
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)


    }


    // 과목 리스트를 업데이트하는 메서드
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
        adapter.viewBinder = SimpleCursorAdapter.ViewBinder { view, cursor, columnIndex ->
            when (view.id) {
                R.id.timeTextView -> {
                    // COLUMN_ELAPSED_TIME의 값을 "00:00:00" 형식으로 변환하여 설정
                    val elapsedTimeInMillis = cursor.getLong(columnIndex)
                    val formattedTime = convertMillisToTimeFormat(elapsedTimeInMillis)
                    (view as TextView).text = formattedTime
                    return@ViewBinder true
                }
                // 다른 View에 대한 처리가 필요하다면 추가
                // 예: R.id.subjectNameTextView -> { ... }
                else -> return@ViewBinder false
            }
        }



        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = adapter

        // 전체 시간 업데이트
        updateTotalTime(totalElapsedTime)
    }


    // 밀리초를 시간 포맷으로 변환하는 메서드
    private fun convertMillisToTimeFormat(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // 총 학습 시간을 업데이트하는 메서드
    private fun updateTotalTime() {
        val totalTime = dbHelper.getTotalElapsedTime()

        val hours:Int = (totalTime / (1000 * 60 * 60)).toInt()
        val minutes:Int = ((totalTime / (1000 * 60)) % 60).toInt()
        val seconds:Int = ((totalTime / 1000) % 60).toInt()

        val total_formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        totalElapsedTimeTextView.text = total_formattedTime
    }

    private fun updateShowTime() {
        val timerOnInstance = timer_on()
        val showTime = timerOnInstance.formattedTime
        val time = showTime
        COLUMN_ELAPSED_TIME = time
        findViewById<TextView>(R.id.timeTextView).text = COLUMN_ELAPSED_TIME
    }

    private fun updateTotalTime(totalElapsedTime: Long) {
        this.totalElapsedTime = totalElapsedTime
        elapsedTimeTextView.text = convertMillisToTimeFormat(totalElapsedTime)
    }

    private fun deleteSubjectAndUpdateTime(subjectId: Long) {
        dbHelper.deleteSubject(subjectId)
        updateTotalTime()
    }


}