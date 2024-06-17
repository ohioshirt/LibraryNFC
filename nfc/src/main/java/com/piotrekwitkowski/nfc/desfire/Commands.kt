package com.piotrekwitkowski.nfc.desfire

object Commands {
    const val SELECT_APPLICATION: Byte = 0x5A.toByte()
    const val AUTHENTICATE_AES: Byte = 0xAA.toByte()
    const val ADDITIONAL_FRAME: Byte = 0xAF.toByte()
    const val READ_DATA: Byte = 0xBD.toByte()
}
