package com.piotrekwitkowski.libraryreader

import android.content.Context
import android.nfc.Tag
import com.piotrekwitkowski.log.Log.i
import com.piotrekwitkowski.nfc.desfire.AESKey
import com.piotrekwitkowski.nfc.desfire.AID
import com.piotrekwitkowski.nfc.desfire.InvalidParameterException

internal class LibraryReader(private val context: Context) {
    @Throws(InvalidParameterException::class)
    fun processTag(tag: Tag?) {
        i(TAG, "processTag()")

        val LIBRARY_AID = AID("015548")
        val LIBRARY_KEY = AESKey("00000000000000000000000000000000")
        val LIBRARY_KEY_NUMBER = 0
        val FILE_NUMBER = 0
        val FILE_OFFSET = 10
        val FILE_LENGTH = 12
        val isoDep: IsoDep = IsoDep.Companion.get(tag)

        try {
            val studentId: StudentId = StudentId.Companion.getStudentId(this.context, isoDep)
            studentId.selectApplication(LIBRARY_AID)
            studentId.authenticateAES(LIBRARY_KEY, LIBRARY_KEY_NUMBER)
            val libraryId = studentId.readData(FILE_NUMBER, FILE_OFFSET, FILE_LENGTH)
            i(
                TAG, "libraryId: " + String(
                    libraryId!!
                )
            )

            studentId.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "LibraryReader"
    }
}
