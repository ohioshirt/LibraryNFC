package com.piotrekwitkowski.nfc.desfire

import com.piotrekwitkowski.nfc.ByteUtils
import java.util.Arrays

open class File protected constructor(data: String) {
    private val data: ByteArray? = ByteUtils.toByteArray(data)

    @Throws(
        ArrayIndexOutOfBoundsException::class,
        IllegalArgumentException::class,
        NullPointerException::class
    )
    fun readData(offset: Int, length: Int): ByteArray {
        return if (length == 0) {
            Arrays.copyOfRange(data, offset, data!!.size)
        } else {
            Arrays.copyOfRange(data, offset, offset + length)
        }
    }
}
