package com.example.MagnetTimer

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI 초기화
        window.statusBarColor = Color.parseColor("#050625")

        // NFC 어댑터 확인 및 활성화 상태 확인
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            // NFC가 지원되지 않는 경우
            showToast("이 기기에서는 NFC가 지원되지 않습니다.")
            return
        }

        if (!nfcAdapter!!.isEnabled) {
            // NFC가 비활성화된 경우
            showToast("NFC가 활성화되어 있지 않습니다.")
        } else {
            // NFC가 활성화된 경우
            showToast("NFC가 활성화 되었습니다.")
        }

        // 버튼 클릭 이벤트 처리
        val startButton = findViewById<Button>(R.id.timeStart)
        startButton.setOnClickListener {
            // NFC 전방향 디스패치 활성화
            enableNfcForegroundDispatch()

            // 타이머 액티비티로 이동
            val intent = Intent(this, timer_on::class.java)
            startActivity(intent)
        }

        // onPause 시 NFC 전방향 디스패치 비활성화
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onResume() {
        super.onResume()

        // 포그라운드 디스패치 활성화
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
    }

    // onPause 시 NFC 전방향 디스패치 비활성화
    override fun onPause() {
        super.onPause()

        // 포그라운드 디스패치 비활성화 (타이머 정지)
        nfcAdapter?.disableForegroundDispatch(this)
    }

    // NFC 태그 감지 시 호출되는 메서드
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    // NFC 태그 처리
    private fun handleNfcIntent(intent: Intent) {
        val action: String? = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action || NfcAdapter.ACTION_TECH_DISCOVERED == action) {
            // NFC 태그가 감지되었을 때 수행할 작업
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                // 여기에서 태그 관련 작업 수행
                showToast("NFC 태그가 감지되었습니다.")

                // 여기에 스톱워치 시작 코드를 추가하세요.
                // 예를 들어, startStopwatch() 또는 타이머 시작하는 관련 코드를 넣으면 됩니다.
            }
        }
    }

    // 토스트 메시지 표시
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // NFC 전방향 디스패치 활성화
    private fun enableNfcForegroundDispatch() {
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
    }
}
