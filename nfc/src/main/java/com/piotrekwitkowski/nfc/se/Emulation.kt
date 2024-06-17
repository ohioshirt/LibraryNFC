package com.piotrekwitkowski.nfc.se

import com.piotrekwitkowski.log.Log

class Emulation(private val secureElement: SecureElement) {
    fun getResponse(apdu: ByteArray): ByteArray? {
        Log.i(TAG, "getResponse()")
        return secureElement.processCommand(Command(apdu))
    }

    companion object {
        private const val TAG = "Emulation"
    }
}
