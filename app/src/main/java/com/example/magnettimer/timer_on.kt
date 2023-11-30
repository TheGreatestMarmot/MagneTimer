package com.example.MagnetTimer
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.magnettimer.MainActivity
import com.example.magnettimer.R

class timer_on : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null
    private var isNfcTagDetected = false
    private var isStopwatchRunning = false
    private var startTime: Long = 0
    private val handler = Handler()

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
        stopStopwatch() // NFC 감지 중단 시 타이머 중단
    }

    private fun handleNfcIntent(intent: Intent) {
        val action: String? = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action || NfcAdapter.ACTION_TECH_DISCOVERED == action) {
            // NFC 태그가 감지되었을 때 수행할 작업
            isNfcTagDetected = true
            disableUserInteraction()
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                startStopwatch()
                // 여기에서 태그 관련 작업 수행
                Toast.makeText(this, "NFC 태그가 감지되었습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // NFC 태그가 감지되지 않았을 때 수행할 작업
            isNfcTagDetected = false
            enableUserInteraction()
        }
    }

    // NFC 태그 감지 시 호출되는 메서드
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun disableUserInteraction() {
        // 사용자 상호 작용 비활성화 코드를 여기에 넣으세요
        // 예를 들어, 버튼을 비활성화하는 등의 작업을 수행합니다.
    }

    private fun enableUserInteraction() {
        // 사용자 상호 작용 활성화 코드를 여기에 넣으세요
        // 예를 들어, 버튼을 활성화하는 등의 작업을 수행합니다.
        if (!isNfcTagDetected) {
            // 만약 NFC 태그가 더 이상 감지되지 않으면 MainActivity로 전환합니다.
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(mainActivityIntent)
            finish()
        }
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
        handler.removeCallbacksAndMessages(null) // 핸들러의 콜백 및 메시지 제거
    }

    private fun updateStopwatch() {
        val stopWatchTextView = findViewById<TextView>(R.id.stopWatch)
        handler.post(object : Runnable {
            override fun run() {
                if (isStopwatchRunning) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val seconds = (elapsedTime / 1000).toInt()
                    val minutes = seconds / 60
                    val hours = minutes / 60

                    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
                    stopWatchTextView.text = formattedTime

                    // 1000ms마다 스톱워치를 업데이트합니다.
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }
}
