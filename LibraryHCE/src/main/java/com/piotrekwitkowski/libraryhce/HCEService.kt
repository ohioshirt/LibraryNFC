package com.piotrekwitkowski.libraryhce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import com.piotrekwitkowski.libraryhce.application.LibraryApplication
import com.piotrekwitkowski.log.Log.i
import com.piotrekwitkowski.log.Log.reset
import com.piotrekwitkowski.nfc.ByteUtils.toHexString
import com.piotrekwitkowski.nfc.Iso7816
import com.piotrekwitkowski.nfc.desfire.InvalidParameterException
import com.piotrekwitkowski.nfc.se.Application
import com.piotrekwitkowski.nfc.se.Emulation
import com.piotrekwitkowski.nfc.se.SecureElement

class HCEService : HostApduService() {
    private val notifications = NotificationService(this)

    override fun processCommandApdu(command: ByteArray, extras: Bundle): ByteArray {
        val response =
            if (firstInteraction) getFirstResponse(command) else getNextResponse(command)!!
        i(TAG, "--> " + toHexString(response))
        return response
    }

    private fun getFirstResponse(command: ByteArray): ByteArray {
        reset(TAG, "<-- " + toHexString(command))
        notifications.createNotificationChannel(this)
        notifications.show("<--" + toHexString(command))

        try {
            Companion.emulation = this.emulation
            firstInteraction = false
            return Iso7816.RESPONSE_SUCCESS
        } catch (e: InvalidParameterException) {
            return Iso7816.RESPONSE_INTERNAL_ERROR
        }
    }

    @get:Throws(InvalidParameterException::class)
    private val emulation: Emulation
        get() {
            val libraryApplication: Application = LibraryApplication()
            val applications = arrayOf(libraryApplication)
            val seWrapper = SecureElement(applications)
            return Emulation(seWrapper)
        }

    private fun getNextResponse(command: ByteArray): ByteArray? {
        i(TAG, "<-- " + toHexString(command))
        notifications.show("<--" + toHexString(command))
        return Companion.emulation!!.getResponse(command)
    }

    override fun onDeactivated(reason: Int) {
        i(TAG, "onDeactivated(). Reason: $reason")
        firstInteraction = true
    }

    companion object {
        private const val TAG = "HCEService"
        private var firstInteraction = true
        private var emulation: Emulation? = null
    }
}
