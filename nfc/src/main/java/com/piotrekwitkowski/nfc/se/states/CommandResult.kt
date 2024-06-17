package com.piotrekwitkowski.nfc.se.states

class CommandResult {
    val state: State
    val response: ByteArray?

    internal constructor(state: State, responseCode: Byte) {
        this.state = state
        this.response = byteArrayOf(responseCode)
    }

    constructor(state: State, responseBytes: ByteArray?) {
        this.state = state
        this.response = responseBytes
    }
}
