package com.piotrekwitkowski.nfc.desfire

import com.piotrekwitkowski.nfc.ByteUtils

open class AESKey(key: String) {
    @JvmField
    val key: ByteArray?

    init {
        if (key.length == 32) {
            this.key = ByteUtils.toByteArray(key)
        } else {
            throw InvalidParameterException("AES key length should be 32 chars")
        }
    }
}
