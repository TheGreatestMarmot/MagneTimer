package com.example.MagnetTimer

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class HostApduService : HostApduService() {

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        return "9000".toByteArray()
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "Deactivated: $reason")
    }

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
