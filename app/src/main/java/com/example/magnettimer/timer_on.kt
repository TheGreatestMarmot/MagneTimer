package com.example.MagnetTimer

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_on)
        window.statusBarColor = Color.parseColor("#050625")

        // NFC 어댑터 및 PendingIntent 초기화
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )

        val finishButton = findViewById<Button>(R.id.finish)
        finishButton.setOnClickListener {
            stopStopwatch()
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

        // 1초마다 NFC 감지 여부 확인
        nfcHandler.postDelayed(nfcDetectionRunnable, 1000)
    }

    // onPause 시 NFC 전방향 디스패치 비활성화
    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        stopStopwatch() // NFC 감지 중단 시 타이머 중단
        nfcHandler.removeCallbacks(nfcDetectionRunnable) // 핸들러의 콜백 제거
    }

    // 1초마다 NFC 감지 여부 확인하는 Runnable
    private val nfcDetectionRunnable = object : Runnable {
        override fun run() {
            Log.d("NFC_DEBUGS", "isNfcTagDetected: $isNfcTagDetected")
            Log.d("NFC_DEBUGS", "isStopwatchRunning: $isStopwatchRunning")

            if (isNfcTagDetected && isStopwatchRunning) {
                // 3초 마다 태그 상태를 확인
                nfcHandler.postDelayed({
                    if (!isNfcTagDetected && isStopwatchRunning) {
                        // 태그가 풀린 경우
                        stopStopwatch()
                        runOnUiThread {
                            Toast.makeText(this@timer_on, "NFC 태그가 끊겼습니다.", Toast.LENGTH_SHORT).show()
                        }
                        // 태그가 풀릴 때 isNfcTagDetected를 false로 설정
                        isNfcTagDetected = false
                    }
                }, 3000)
            }

            nfcHandler.postDelayed(this, 3000)
        }
    }



    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }


    private fun handleNfcIntent(intent: Intent) {
        val action: String? = intent.action
        Log.d("NFC_DEBUG", "isNfcTagDetected: $isNfcTagDetected")
        Log.d("NFC_DEBUG", "isNfcTagDetected: $isStopwatchRunning")
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
            startTime = System.currentTimeMillis()
            updateStopwatch()
        }
    }

    private fun stopStopwatch() {
        isStopwatchRunning = false
        timerTask?.cancel()
        timerTask = null
    }

    private fun updateStopwatch() {
        val stopWatchTextView = findViewById<TextView>(R.id.stopWatch)
        nfcHandler.post(object : Runnable {
            override fun run() {
                if (isStopwatchRunning) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val seconds = (elapsedTime / 1000).toInt()
                    val minutes = seconds / 60
                    val hours = minutes / 60

                    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
                    stopWatchTextView.text = formattedTime

                    // 1000ms마다 스톱워치를 업데이트합니다.
                    nfcHandler.postDelayed(this, 1000)
                }
            }
        })
    }
}
