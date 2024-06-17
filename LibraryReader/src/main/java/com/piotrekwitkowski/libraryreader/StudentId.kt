package com.piotrekwitkowski.libraryreader

import android.content.Context
import com.piotrekwitkowski.log.Log.i
import com.piotrekwitkowski.nfc.ByteUtils.toHexString
import com.piotrekwitkowski.nfc.ByteUtils.trimEnd
import com.piotrekwitkowski.nfc.Iso7816
import com.piotrekwitkowski.nfc.desfire.AESKey
import com.piotrekwitkowski.nfc.desfire.AID
import java.io.IOException

internal class StudentId private constructor(private val isoDep: IsoDep) {
    internal enum class idForm {
        PHYSICAL, HCE
    }

    @Throws(IOException::class)
    fun close() {
        isoDep.close()
    }

    @Throws(IOException::class, DESFireReaderException::class)
    fun selectApplication(aid: AID) {
        val applicationAid = aid.bytes
        DESFireReader.selectApplication(this.isoDep, applicationAid)
        i(TAG, "Application selected: " + toHexString(applicationAid))
    }

    @Throws(Exception::class)
    fun authenticateAES(key: AESKey, keyNumber: Int) {
        val sessionKey = DESFireReader.authenticateAES(this.isoDep, key.key, keyNumber.toByte())
        i(TAG, "Session key: " + toHexString(sessionKey))
    }

    @Throws(IOException::class, DESFireReaderException::class)
    fun readData(fileNumber: Int, offset: Int, length: Int): ByteArray {
        val response = DESFireReader.readData(this.isoDep, fileNumber, offset, length)
        // TODO: check CRC (last 8 bytes)
        val data = trimEnd(response!!, 8)
        i(TAG, "Data: " + toHexString(data))
        return data
    }

    companion object {
        private const val TAG = "StudentId"
        @Throws(Exception::class)
        fun getStudentId(context: Context, isoDep: IsoDep): StudentId {
            i(TAG, "getStudentId()")
            isoDep.connect()

            val idForm = getIdForm(isoDep)
            i(TAG, "ID form: $idForm")

            return if (idForm == StudentId.idForm.PHYSICAL) {
                StudentId(isoDep)
            } else if (idForm == StudentId.idForm.HCE) {
                val response = HCE.selectAndroidApp(context, isoDep)
                if (response?.bytes.contentEquals(Iso7816.RESPONSE_SUCCESS)) {
                    StudentId(isoDep)
                } else {
                    throw StudentIdException("HCE Mobile Application select was unsuccessful")
                }
            } else {
                throw StudentIdException("ID form not supported")
            }
        }

        @Throws(StudentIdException::class)
        private fun getIdForm(isoDep: IsoDep): idForm {
            i(TAG, "getIdForm()")

            val historicalBytes = isoDep.historicalBytes
            i(TAG, "historicalBytes: " + toHexString(historicalBytes))

            return if (historicalBytes.contentEquals(byteArrayOf(0x80.toByte()))) {
                idForm.PHYSICAL
            } else if (historicalBytes.contentEquals(byteArrayOf())) {
                idForm.HCE
            } else {
                throw StudentIdException("id form not recognized")
            }
        }
    }
}
