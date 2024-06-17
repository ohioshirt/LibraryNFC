package com.piotrekwitkowski.nfc

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Arrays

object ByteUtils {
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun toByteArray(s: String): ByteArray {
        val len = s.length
        require(len % 2 != 1) { "Hex string must have even number of characters" }
        val data = ByteArray(len / 2) // Allocate 1 byte per 2 hex characters
        var i = 0
        while (i < len) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = ((s[i].digitToIntOrNull(16) ?: -1 shl 4)
            + s[i + 1].digitToIntOrNull(16)!! ?: -1).toByte()
            i += 2
        }
        return data
    }

    @JvmStatic
    fun toHexString(bytes: ByteArray?): String {
        val hexArray = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F'
        )
        val hexChars = CharArray(bytes!!.size * 2) // Each byte has two hex characters (nibbles)
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt() and 0xFF // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v ushr 4] // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v and 0x0F] // Select hex character from lower nibble
        }
        return String(hexChars)
    }

    @JvmStatic
    fun concatenate(a: Byte, b: Byte): ByteArray {
        return byteArrayOf(a, b)
    }

    @JvmStatic
    fun concatenate(a: Byte, b: ByteArray?): ByteArray {
        return concatenate(byteArrayOf(a), b)
    }

    @JvmStatic
    fun concatenate(a: ByteArray?, b: ByteArray?): ByteArray {
        val c = ByteArray(a!!.size + b!!.size)
        System.arraycopy(a, 0, c, 0, a.size)
        System.arraycopy(b, 0, c, a.size, b.size)
        return c
    }

    @JvmStatic
    fun rotateOneLeft(a: ByteArray?): ByteArray {
        val rotated = ByteArray(a!!.size)
        if (a.size - 1 >= 0) System.arraycopy(a, 1, rotated, 0, a.size - 1)
        rotated[rotated.size - 1] = a[0]
        return rotated
    }

    fun first16Bytes(a: ByteArray?): ByteArray {
        return Arrays.copyOfRange(a, 0, 16)
    }

    @JvmStatic
    fun last16Bytes(a: ByteArray?): ByteArray {
        return Arrays.copyOfRange(a, a!!.size - 16, a.size)
    }

    @JvmStatic
    fun getRandomBytes(length: Int): ByteArray {
        val random = ByteArray(length)
        SecureRandom().nextBytes(random)
        return random
    }

    @JvmStatic
    fun first3Bytes(i: Int): ByteArray {
        return byteArrayOf(
            ((i) and 0xff).toByte(),
            ((i shr 8) and 0xff).toByte(),
            ((i shr 16) and 0xff).toByte(),  //                (byte)((i >> 24) & 0xff),
        )
    }

    fun threeBytesToInt(bytes: ByteArray): Int {
        val moreBytes = byteArrayOf(
            0x00.toByte(),
            bytes[2],
            bytes[1],
            bytes[0],
        )
        return ByteBuffer.wrap(moreBytes).getInt()
    }

    @JvmStatic
    fun trimEnd(bytes: ByteArray, i: Int): ByteArray {
        return Arrays.copyOfRange(bytes, 0, bytes.size - i)
    }
}
