package com.piotrekwitkowski.nfc.se

import com.piotrekwitkowski.log.Log
import com.piotrekwitkowski.nfc.ByteUtils
import com.piotrekwitkowski.nfc.desfire.AESKey
import java.io.ByteArrayOutputStream
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Authentication(application: Application) {
    private val key: AESKey? = application.key0
    private val cipher: Cipher = Cipher.getInstance("AES/CBC/NoPadding")
    private val aes = SecretKeySpec(application.key0.key, "AES")

    private lateinit var randomBytes: ByteArray
    private lateinit var challenge: ByteArray

    @Throws(
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchKeyException::class
    )
    fun initiate(keyNumber: Byte): ByteArray {
        // 1. The reader asked for AES authentication for a specific key.
        if (keyNumber.toInt() != 0) {
            throw NoSuchKeyException()
        }

        // 2. The card creates a 16 byte random number (B) and encrypts it with the selected AES
        // key. The result is sent to the reader.
        this.randomBytes = ByteUtils.getRandomBytes(16)
        Log.i(TAG, "random bytes: " + ByteUtils.toHexString(randomBytes))
        val ivParam = IvParameterSpec(ByteArray(key!!.key!!.size))
        cipher.init(Cipher.ENCRYPT_MODE, aes, ivParam)
        this.challenge = cipher.doFinal(this.randomBytes)
        return challenge
    }

    @Throws(
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AuthenticationException::class
    )
    fun proceed(readerChallenge: ByteArray?): AuthenticationResponse {
        // 3. The reader receives the 16 bytes, and decrypts it using the AES key to get back the
        // original 16 byte random number (B). This is decrypted with an IV of all 00 bytes.
        // 4. The reader generates its own 16 byte random number (A).
        // 5. The reader rotates B one byte to the left.
        // 6. The reader concatenates A and the rotated B together to make a 32 byte value C.
        // 7. The reader encrypts the 32 byte value C with the AES key and sends D to the card. The
        // IV for encrypting this is the 16 bytes received from the card (i.e. before decryption).
        // 8. The card receives the 32 byte value D and decrypts it with the AES key.
        var ivParam = IvParameterSpec(this.challenge)
        cipher.init(Cipher.DECRYPT_MODE, aes, ivParam)
        val C = cipher.doFinal(readerChallenge)
        Log.i(TAG, "from Reader: " + ByteUtils.toHexString(C))

        // 9. The card checks the second 16 bytes of C match the original random number B (rotated one
        // byte left). If this fails the authentication has failed. If it matches, the card knows
        // the reader has the right key.
        if (!ByteUtils.last16Bytes(C).contentEquals(
                ByteUtils.rotateOneLeft(
                    this.randomBytes
                )
            )
        ) {
            throw AuthenticationException()
        }

        // 10. The card rotates the first 16 bytes (A) left by one byte.
        val A = ByteUtils.first16Bytes(C)
        val rotatedA = ByteUtils.rotateOneLeft(A)

        // 11. The card encrypts this rotated A using the AES key and sends it to the reader.
        // 12. The reader receives the 16 bytes and decrypts it. The IV for this is the last 16
        // bytes the reader sent to the card.
        ivParam = IvParameterSpec(ByteUtils.last16Bytes(readerChallenge))
        cipher.init(Cipher.ENCRYPT_MODE, aes, ivParam)
        val encryptedRotatedA = cipher.doFinal(rotatedA)

        // 13. The reader checks this matches the original A random number (rotated one byte left).
        // If this fails then the authentication fails. If it matches, the reader knows the card
        // has the AES key too.
        // 14. Finally both reader and card generate a 16 byte session key using the random numbers
        // they now know. This is done by concatenating the first 4 bytes of A, first 4 bytes of B,
        // last 4 bytes of A and last 4 bytes of B.
        val sessionKeyOutputStream = ByteArrayOutputStream()
        sessionKeyOutputStream.write(A, 0, 4)
        sessionKeyOutputStream.write(randomBytes, 0, 4)
        sessionKeyOutputStream.write(A, 12, 4)
        sessionKeyOutputStream.write(randomBytes, 12, 4)
        val sessionKey = sessionKeyOutputStream.toByteArray()

        return AuthenticationResponse(sessionKey, encryptedRotatedA)
    }

    companion object {
        private const val TAG = "ApplicationAuthentication"
    }
}
