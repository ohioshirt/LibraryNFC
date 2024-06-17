package com.piotrekwitkowski.libraryreader

import android.nfc.Tag
import com.piotrekwitkowski.log.Log.i
import com.piotrekwitkowski.nfc.ByteUtils.concatenate
import com.piotrekwitkowski.nfc.ByteUtils.toHexString
import java.io.IOException

internal class IsoDep private constructor(private val mIsoDep: android.nfc.tech.IsoDep) {
    @Throws(IOException::class)
    fun connect() {
        i(TAG, "connect()")
        mIsoDep.connect()
    }

    @Throws(IOException::class)
    fun transceive(command: Byte, data: Byte): Response {
        return transceive(concatenate(command, data))
    }

    @Throws(IOException::class)
    fun transceive(command: Byte, data: ByteArray?): Response {
        return transceive(concatenate(command, data))
    }

    @Throws(IOException::class)
    fun transceive(data: ByteArray?): Response {
        i(TAG, "--> " + toHexString(data))
        val response = mIsoDep.transceive(data)
        i(TAG, "<-- " + toHexString(response))
        return Response(response)
    }

    @Throws(IOException::class)
    fun close() {
        i(TAG, "close()")
        mIsoDep.close()
    }

    val historicalBytes: ByteArray
        get() {
            i(TAG, "getHistoricalBytes()")
            return mIsoDep.historicalBytes
        }

    companion object {
        private const val TAG = "IsoDep"
        fun get(tag: Tag?): IsoDep {
            return IsoDep(android.nfc.tech.IsoDep.get(tag))
        }
    }
}
