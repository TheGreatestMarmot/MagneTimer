import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

//    private var nfcAdapter: NfcAdapter? = null
//    private var nfcPendingIntent: PendingIntent? = null
//    private val writeTagFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
//
//        val intent = Intent(this, javaClass).apply {
//            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//        }
//        nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        nfcAdapter?.disableForegroundDispatch(this)
//    }
//
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        handleIntent(intent)
//    }
//
//    private fun handleIntent(intent: Intent) {
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
//            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { tag ->
//                val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
//                if (messages != null) {
//                    for (message in messages) {
//                        val records = (message as NdefMessage).records
//                        for (record in records) {
//                            val payload = record.payload
//                            val text = String(payload, Charsets.UTF_8)
//                            // 읽은 데이터를 처리합니다.
//                            val textView = findViewById<TextView>(R.id.textView)
//                            textView.text = text
//                        }
//                    }
//                }
//            }
//        }
    }
}
