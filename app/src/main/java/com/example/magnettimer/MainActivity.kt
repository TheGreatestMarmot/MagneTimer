package com.example.MagnetTimer

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = Color.parseColor("#050625")

        // NFC 어댑터 확인 및 활성화 상태 확인
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "이 기기에서는 NFC가 지원되지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }


        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "NFC가 활성화되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        } else {
            Toast.makeText(this, "NFC가 활성화 되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 버튼 클릭 이벤트 처리
        val startButton = findViewById<Button>(R.id.timeStart)
        startButton.setOnClickListener {
            // NFC 전방향 디스패치 활성화
            nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)

            // ActivityTimerOnd로 이동
            val intent = Intent(this, timer_on::class.java)
            startActivity(intent)
        }

        // onPause 시 NFC 전방향 디스패치 비활성화
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )
    }


}
