package com.piotrekwitkowski.libraryreader

import java.util.Arrays

internal class Response(val bytes: ByteArray) {
    val responseCode: Byte
        get() = bytes[0]

    val data: ByteArray
        get() = Arrays.copyOfRange(bytes, 1, bytes.size)
}
