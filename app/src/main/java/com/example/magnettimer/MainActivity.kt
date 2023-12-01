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

    // 토스트 메시지 표시
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // NFC 전방향 디스패치 활성화
    private fun enableNfcForegroundDispatch() {
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
    }
}
