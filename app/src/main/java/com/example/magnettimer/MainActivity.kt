package com.example.magnettimer

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
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

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val intent = Intent(this, javaClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // 버튼 클릭 이벤트 처리
        val startButton = findViewById<Button>(R.id.Timestart)
        startButton.setOnClickListener {
            // NFC 기다림
            nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        }
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null) {
            nfcAdapter!!.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfcAdapter != null) {
            nfcAdapter!!.disableForegroundDispatch(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            showToast("NFC가 작동되었습니다.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
