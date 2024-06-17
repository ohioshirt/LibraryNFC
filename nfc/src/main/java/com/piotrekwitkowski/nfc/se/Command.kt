package com.piotrekwitkowski.nfc.se

import java.util.Arrays

class Command internal constructor(private val bytes: ByteArray) {
    val code: Byte
        get() = bytes[0]

    val data: ByteArray
        get() = Arrays.copyOfRange(bytes, 1, bytes.size)
}
