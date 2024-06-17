package com.piotrekwitkowski.nfc.desfire

object ResponseCodes {
    const val SUCCESS: Byte = 0x00.toByte()
    const val ILLEGAL_COMMAND: Byte = 0x1C.toByte()
    const val NO_SUCH_KEY: Byte = 0x40.toByte()
    const val LENGTH_ERROR: Byte = 0x7E.toByte()
    const val APPLICATION_NOT_FOUND: Byte = 0xA0.toByte()
    const val AUTHENTICATION_ERROR: Byte = 0xAE.toByte()
    const val ADDITIONAL_FRAME: Byte = 0xAF.toByte()
    const val BOUNDARY_ERROR: Byte = 0xBE.toByte()
    const val FILE_NOT_FOUND: Byte = 0xF0.toByte()
}
