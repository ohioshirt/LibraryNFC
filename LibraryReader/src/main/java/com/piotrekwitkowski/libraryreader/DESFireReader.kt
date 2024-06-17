package com.piotrekwitkowski.libraryreader

import com.piotrekwitkowski.log.Log.i
import com.piotrekwitkowski.nfc.ByteUtils.concatenate
import com.piotrekwitkowski.nfc.ByteUtils.first3Bytes
import com.piotrekwitkowski.nfc.ByteUtils.getRandomBytes
import com.piotrekwitkowski.nfc.ByteUtils.last16Bytes
import com.piotrekwitkowski.nfc.ByteUtils.rotateOneLeft
import com.piotrekwitkowski.nfc.ByteUtils.toHexString
import com.piotrekwitkowski.nfc.desfire.Commands
import com.piotrekwitkowski.nfc.desfire.ResponseCodes
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal object DESFireReader {
    private const val TAG = "DESFireReader"

    @Throws(IOException::class, DESFireReaderException::class)
    fun selectApplication(isoDep: IsoDep, aid: ByteArray?) {
        i(TAG, "selectApplication()")

        val response = isoDep.transceive(Commands.SELECT_APPLICATION, aid)
        if (response.responseCode != ResponseCodes.SUCCESS) {
            throw DESFireReaderException("selectApplication() failed. Response status: " + response.responseCode)
        }
    }

    @Throws(
        IOException::class,
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        DESFireReaderException::class
    )
    fun authenticateAES(isoDep: IsoDep, aesKey: ByteArray?, keyNumber: Byte): ByteArray {
        i(TAG, "authenticateAES()")

        // 1. The reader asked for AES authentication for a specific key.
        // 2. The card creates a 16 byte random number (B) and encrypts it with the selected AES
        // key. The result is sent to the reader.
        var response = isoDep.transceive(Commands.AUTHENTICATE_AES, keyNumber)
        var challenge = response.data
        i(TAG, "challenge: " + toHexString(challenge))

        // 3. The reader receives the 16 bytes, and decrypts it using the AES key to get back the
        // original 16 byte random number (B). This is decrypted with an IV of all 00 bytes.
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val aes: Key = SecretKeySpec(aesKey, "AES")
        var ivParam = IvParameterSpec(ByteArray(16))
        cipher.init(Cipher.DECRYPT_MODE, aes, ivParam)
        val B = cipher.doFinal(challenge)
        i(TAG, "cipheredData: " + toHexString(B))

        // 4. The reader generates its own 16 byte random number (A).
        val A = getRandomBytes(16)

        // 5. The reader rotates B one byte to the left.
        val rotatedB = rotateOneLeft(B)

        // 6. The reader concatenates A and the rotated B together to make a 32 byte value C.
        val C = concatenate(A, rotatedB)

        // 7. The reader encrypts the 32 byte value C with the AES key and sends D to the card. The
        // IV for encrypting this is the 16 bytes received from the card (i.e. before decryption).
        ivParam = IvParameterSpec(challenge)
        cipher.init(Cipher.ENCRYPT_MODE, aes, ivParam)
        val D = cipher.doFinal(C)
        val command = concatenate(Commands.ADDITIONAL_FRAME, D)
        response = isoDep.transceive(command)
        challenge = response.data

        // 8. The card receives the 32 byte value D and decrypts it with the AES key.
        // 9. The card checks the second 16 bytes match the original random number B (rotated one
        // byte left). If this fails the authentication has failed. If it matches, the card knows
        // the reader has the right key.
        if (response.responseCode != ResponseCodes.SUCCESS) {
            throw DESFireReaderException("authenticateAES failed")
        }

        // 10. The card rotates the first 16 bytes (A) left by one byte.
        // 11. The card encrypts this rotated A using the AES key and sends it to the reader.
        // 12. The reader receives the 16 bytes and decrypts it. The IV for this is the last 16
        // bytes the reader sent to the card.
        val last16Bytes = last16Bytes(command)
        ivParam = IvParameterSpec(last16Bytes)
        cipher.init(Cipher.DECRYPT_MODE, aes, ivParam)
        val E = cipher.doFinal(challenge)

        // 13. The reader checks this matches the original A random number (rotated one byte left).
        // If this fails then the authentication fails. If it matches, the reader knows the card
        // has the AES key too.
        if (!rotateOneLeft(A).contentEquals(E)) {
            throw DESFireReaderException("authenticateAES failed")
        }

        // 14. Finally both reader and card generate a 16 byte session key using the random numbers
        // they now know. This is done by concatenating the first 4 bytes of A, first 4 bytes of B,
        // last 4 bytes of A and last 4 bytes of B.
        val sessionKeyOutputStream = ByteArrayOutputStream()
        sessionKeyOutputStream.write(A, 0, 4)
        sessionKeyOutputStream.write(B, 0, 4)
        sessionKeyOutputStream.write(A, 12, 4)
        sessionKeyOutputStream.write(B, 12, 4)
        return sessionKeyOutputStream.toByteArray()
    }

    @Throws(IOException::class, DESFireReaderException::class)
    fun readData(isoDep: IsoDep, fileNumber: Int, offset: Int, length: Int): ByteArray? {
        i(TAG, "readData()")

        // TODO: check if file
        // TODO: check if offset and length smaller than 3 bytes, else throw Exception
        val offsetBytes = first3Bytes(offset)
        val lengthBytes = first3Bytes(length)

        val params = concatenate(offsetBytes, lengthBytes)
        val commandData = concatenate(fileNumber.toByte(), params)

        val response = isoDep.transceive(Commands.READ_DATA, commandData)
        if (response.responseCode == ResponseCodes.SUCCESS) {
            return response.data
        } else if (response.responseCode == ResponseCodes.BOUNDARY_ERROR) {
            throw DESFireReaderException("Boundary error!")
        } else {
            throw DESFireReaderException("readData failed. Response status: " + response.responseCode)
        }
    }
}
