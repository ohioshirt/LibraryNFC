package com.piotrekwitkowski.libraryreader

import android.content.Context
import com.piotrekwitkowski.log.Log.i
import com.piotrekwitkowski.nfc.ByteUtils.toByteArray
import com.piotrekwitkowski.nfc.Iso7816.wrapApdu
import java.io.IOException

internal object HCE {
    private const val TAG = "HCE"

    @Throws(IOException::class)
    fun selectAndroidApp(context: Context, isoDep: IsoDep): Response? {
        i(TAG, "selectAndroidApp()")

        val HCE_AID = context.getString(com.piotrekwitkowski.nfc.R.string.hce_aid)
        val commandApdu = wrapApdu(toByteArray(HCE_AID))
        return isoDep.transceive(commandApdu)
    }
}
