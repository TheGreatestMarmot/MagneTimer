package com.example.MagnetTimer

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class HostApduService : HostApduService() {

    // HostApduService는 NFC의 HCE 기능 구현 클래스.

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        // "9000"을 바이트 배열로 변환하여 반환, "9000"은 APDU 응답에서 일반적으로 성공할때 보냄
        return "9000".toByteArray()
    }

    // HCE 서비스가 비활성화될 때 호출됨
    // NFC 리더기와의 연결이 끊어졌을 때 발생됨
    override fun onDeactivated(reason: Int) {
        // 로그를 출력
        Log.d("HCE", "Deactivated: $reason")
    }

    // 바이트 배열을 16진수 문자열로 변환
    private fun bytesToHex(bytes: ByteArray?): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes?.size?.times(2) ?: 0)
        for (j in bytes?.indices ?: return "") {
            val v: Int = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}
