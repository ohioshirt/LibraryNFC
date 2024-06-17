package com.piotrekwitkowski.nfc.desfire

import com.piotrekwitkowski.nfc.ByteUtils

open class AID {
    @JvmField
    val bytes: ByteArray?

    constructor(aid: String) {
        if (aid.length == 6) {
            this.bytes = ByteUtils.toByteArray(aid)
        } else {
            throw InvalidParameterException("AID length should be 6 chars")
        }
    }

    constructor(aid: ByteArray?) {
        if (aid!!.size == 3) {
            this.bytes = aid
        } else {
            throw InvalidParameterException("AID length should be 3 bytes")
        }
    }

    fun equals(aid: AID): Boolean {
        return bytes.contentEquals(aid.bytes)
    }
}
