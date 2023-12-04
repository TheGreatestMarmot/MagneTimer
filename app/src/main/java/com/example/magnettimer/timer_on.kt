package com.example.MagnetTimer

import DBHelper
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception
import java.util.TimerTask

class timer_on : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null
    private var isNfcTagDetected = false
    private var isStopwatchRunning = false
    private var startTime: Long = 0
    private val handler = Handler()
    private var timerTask: TimerTask? = null
    private var lastElapsedTime: Long = 0
    private lateinit var gradient_1: ImageView
    private lateinit var gradient_2: ImageView
    private lateinit var locking : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_on)
        window.statusBarColor = Color.parseColor("#050625")
        gradient_1 = findViewById(R.id.ellipse_1)
        gradient_2 = findViewById(R.id.ellipse_2)
        locking = findViewById<ImageView>(R.id.lock)
        // NFC 어댑터 및 PendingIntent 초기화
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val stopWatchTextView = findViewById<TextView>(R.id.stopWatch)
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )

        val finishButton = findViewById<Button>(R.id.finish)
        finishButton.setOnClickListener {
            stopStopwatch()

            // 과목 저장
            val subjectEditText = findViewById<EditText>(R.id.subjectEditText)
            val subjectName = subjectEditText.text.toString()

            if (subjectName.isNotBlank()) { // 빈 문자열이 아닌 경우에만 저장
                val dbHelper = DBHelper(this)
                dbHelper.insertSubject(subjectName, lastElapsedTime) // 수정된 부분
            } else {
                Toast.makeText(this, "과목명을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultIntent = Intent()
            setResult(RESULT_OK, resultIntent)
            finish()

        }

    }



        // nfcHandler를 사용하여 1초마다 NFC 감지 여부 확인
    private val nfcHandler = Handler()

    override fun onResume() {
        super.onResume()
        // 포그라운드 디스패치 활성화
        nfcAdapter?.let {
            it.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        }
    }

    // onPause 시 NFC 전방향 디스패치 비활성화
    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

    private fun handleNfcIntent(intent: Intent) {
        val action: String? = intent.action
        Log.d("NFC_DEBUG", "isNfcTagDetected: $isNfcTagDetected")
        Log.d("NFC_DEBUG", "isNfcTagDetected: $isStopwatchRunning")
        val isNfcTextView = findViewById<TextView>(R.id.isNfcText)
        val lockingTextView = findViewById<TextView>(R.id.lockText)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action || NfcAdapter.ACTION_TECH_DISCOVERED == action) {
            // NFC 태그가 감지되었을 때 수행할 작업
            isNfcTagDetected = true
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                startStopwatch()
                // 여기에서 태그 관련 작업 수행
                Toast.makeText(this, "NFC 태그가 감지되었습니다.", Toast.LENGTH_SHORT).show()
                val isoDep: IsoDep = IsoDep.get(tag)
                isoDep.connect()
                gradient_1.setImageResource(R.drawable.eclipse)
                gradient_2.setImageResource(R.drawable.eclipse)
                locking.setImageResource(R.drawable.lock_24px)
                isNfcTextView?.text ="NFC 태그 완료"
                lockingTextView?.text="딴 짓 방지 켜짐"
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)


                Thread(Runnable {
                    try {
                        while (true) {
                            val request = byteArrayOfInts(0x00, 0x01, 0x02, 0x03);
                            val response = isoDep.transceive(request)
                            Thread.sleep(500)
                        }
                    }
                    catch (ex : Exception) {
                        ex.printStackTrace()
                        stopStopwatch()

                    }
                }).start()
            }
        } else {
            // NFC 태그가 감지되지 않았을 때 수행할 작업
            isNfcTagDetected = false
            stopStopwatch() // NFC 태그가 끊겼을 때 스톱워치 멈춤
        }
    }

    // NFC 태그 감지 시 호출되는 메서드
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun startStopwatch() {
        if (!isStopwatchRunning) {
            isStopwatchRunning = true
            if (lastElapsedTime == 0L) {
                // 처음 시작할 때
                startTime = System.currentTimeMillis()
            } else {
                // 이전에 저장된 시간이 있는 경우
                startTime = System.currentTimeMillis() - lastElapsedTime
            }
            updateStopwatch()
        }
    }

    private fun stopStopwatch() {
        isStopwatchRunning = false
        timerTask?.cancel()
        timerTask = null
        lastElapsedTime = System.currentTimeMillis() - startTime
    }

    private fun updateStopwatch() {
        val stopWatchTextView = findViewById<TextView>(R.id.stopWatch)
        nfcHandler.post(object : Runnable {
            override fun run() {
                if (isStopwatchRunning) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime > 0) {
                        val hours = (elapsedTime / (1000 * 60 * 60)).toInt()
                        val minutes = ((elapsedTime / (1000 * 60)) % 60).toInt()
                        val seconds = ((elapsedTime / 1000) % 60).toInt()

                        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        stopWatchTextView.text = formattedTime
                    } else {
                        stopWatchTextView.text = "00:00:00"
                    }

                    // 1000ms마다 스톱워치를 업데이트합니다.
                    nfcHandler.postDelayed(this, 1000)
                } else {
                    val isNfcTextView = findViewById<TextView>(R.id.isNfcText)
                    val lockingTextView = findViewById<TextView>(R.id.lockText)
                    gradient_1.setImageResource(R.drawable.nfc_off)
                    gradient_2.setImageResource(R.drawable.nfc_off)
                    locking.setImageResource(R.drawable.lock_open)
                    isNfcTextView?.text = "NFC 태그 미완료"
                    lockingTextView?.text = "딴 짓 방지 꺼짐"
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                }
            }
        })
    }


}
