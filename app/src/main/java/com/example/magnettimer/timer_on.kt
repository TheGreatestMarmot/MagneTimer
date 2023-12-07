package com.example.MagnetTimer

import DBHelper
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var locking: ImageView
    private lateinit var stopWatchTextView: TextView
    private lateinit var subjectEditText: TextView
    private var hours: Int = 0
    private var minutes: Int = 0
    private var seconds: Int = 0
    private val elapsedTime = System.currentTimeMillis() - startTime
    var formattedTime:String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_on)
        window.statusBarColor = Color.parseColor("#050625")
        gradient_1 = findViewById(R.id.ellipse_1)
        gradient_2 = findViewById(R.id.ellipse_2)
        locking = findViewById<ImageView>(R.id.lock)
        stopWatchTextView = findViewById<TextView>(R.id.stopWatch)
        subjectEditText = findViewById<EditText>(R.id.subjectEditText)
        var elapsedTime = System.currentTimeMillis() - startTime

        val hours:Int = (elapsedTime / (1000 * 60 * 60)).toInt()
        val minutes:Int = ((elapsedTime / (1000 * 60)) % 60).toInt()
        val seconds:Int = ((elapsedTime / 1000) % 60).toInt()
        // NFC 어댑터 및 PendingIntent 초기화
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val stopWatchTextView = findViewById<TextView>(R.id.stopWatch)
        val ellipse1 = findViewById<View>(R.id.ellipse_1)
        val ellipse2 = findViewById<View>(R.id.ellipse_2)
        startRotationAnimation(ellipse1)
        startRotationAnimation(ellipse2)
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )

        val finishButton = findViewById<Button>(R.id.finish)
        finishButton.setOnClickListener {
            val subjectName: String
            subjectName = subjectEditText.text.toString().trim().replace("\\s+".toRegex(), "")
            if(subjectName.length > 6) {
                val timerOnView = findViewById<View>(R.id.activity_timer_on)
                val snackbar = Snackbar.make(timerOnView, "6글자 이내로 작성해주세요.", Snackbar.LENGTH_LONG)
                snackbar.setAction("확인") {
                    snackbar.dismiss()
                }
                snackbar.show()
                return@setOnClickListener
            }
            if (stopWatchTextView.text == "00:00:00") {
                showMessage()
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                "00:00:00"
                return@setOnClickListener
            } else {
                if (subjectName.isNotBlank()) { // 빈 문자열이 아닌 경우에만 저장
                    val dbHelper = DBHelper(this)
                    dbHelper.insertSubject(subjectName, lastElapsedTime) // 수정된 부분
                } else {
                    Toast.makeText(this, "과목명을 입력하세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                finish()
            }
            stopStopwatch()
            // 과목 저장

            val resultIntent = Intent()
            setResult(RESULT_OK, resultIntent)
        }

    }
    private fun startRotationAnimation(view: View) {
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        rotation.duration = 2000 // 애니메이션의 기간 설정
        rotation.repeatCount = ObjectAnimator.INFINITE // 무한 반복 설정
        rotation.interpolator = LinearInterpolator() // 선형 보간 사용

        rotation.start()
    }
    private fun showMessage() {
        val stopWatchTextView = findViewById<TextView>(R.id.stopWatch)
        if (stopWatchTextView.text.toString() == "00:00:00") {
            val timerOnView = findViewById<View>(R.id.activity_timer_on)
            val snackbar = Snackbar.make(timerOnView, "공부 하시고 종료하세요", Snackbar.LENGTH_LONG)
            snackbar.setAction("확인") {
                snackbar.dismiss()
            }

            snackbar.show()

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

    override fun onBackPressed() {
//        super.onBackPressed()
    }

    // onPause 시 NFC 전방향 디스패치 비활성화
    override fun onPause() {
        super.onPause()
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.moveTaskToFront(taskId, 0)
        } catch (e: Exception) {
            // 예외 처리: ActivityManager 또는 moveTaskToFront에서 예외가 발생한 경우
            e.printStackTrace()
        }

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
                isNfcTextView?.text = "NFC 태그 완료"
                lockingTextView?.text = "딴 짓 방지 켜짐"
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                onBackPressed()

                Thread(Runnable {
                    try {
                        while (true) {
                            val request = byteArrayOfInts(0x00, 0x01, 0x02, 0x03);
                            val response = isoDep.transceive(request)
                            Thread.sleep(500)
                        }
                    } catch (ex: Exception) {
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
        nfcHandler.post(object : Runnable {
            override fun run() {
                if (isStopwatchRunning) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val hours:Int = (elapsedTime / (1000 * 60 * 60)).toInt()
                    val minutes:Int = ((elapsedTime / (1000 * 60)) % 60).toInt()
                    val seconds:Int = ((elapsedTime / 1000) % 60).toInt()
                    if (elapsedTime > 0) {
                        formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
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
                    isNfcTextView?.text = "NFC 미태그"
                    lockingTextView?.text = "딴 짓 방지 꺼짐"
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    "00:00:00"
                }

            }
        })
    }
}