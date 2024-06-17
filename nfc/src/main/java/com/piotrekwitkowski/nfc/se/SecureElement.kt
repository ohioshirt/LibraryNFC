package com.piotrekwitkowski.nfc.se

import com.piotrekwitkowski.log.Log
import com.piotrekwitkowski.nfc.se.states.InitialState
import com.piotrekwitkowski.nfc.se.states.State

class SecureElement(applications: Array<Application>) {
    private var state: State?

    init {
        this.state = InitialState(applications)
    }

    fun processCommand(command: Command): ByteArray? {
        Log.i(TAG, "processCommand()")

        val result = state!!.processCommand(command)
        this.state = result.state
        return result.response
    }

    companion object {
        private const val TAG = "SoftwareSEWrapper"
    }
}
