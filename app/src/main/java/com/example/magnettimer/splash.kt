package com.example.MagnetTimer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class splash : AppCompatActivity() {

    // 지연 시간 상수
    companion object {
        private const val DURATION: Long = 1500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 레이아웃 설정
        setContentView(R.layout.activity_splash)

        // 상태 표시줄 색상 설정
        window.statusBarColor = Color.parseColor("#050625")

        // 일정 시간 지난 후 메인 액티비티로 이동
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        }, DURATION)
    }

    // 뒤로가기 버튼 동작 무시
    override fun onBackPressed() {
        super.onBackPressed()
    }
}
