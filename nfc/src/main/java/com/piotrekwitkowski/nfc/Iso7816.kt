package com.piotrekwitkowski.nfc

object Iso7816 {
    @JvmField
    val RESPONSE_SUCCESS: ByteArray = byteArrayOf(0x90.toByte(), 0x00.toByte())
    @JvmField
    val RESPONSE_INTERNAL_ERROR: ByteArray = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    private const val SELECT = 0xA4.toByte()

    //    private final static byte READ_BINARY = (byte) 0xB0;
    //    private final static byte UPDATE_BINARY = (byte) 0xD6;
    //    private final static byte READ_RECORDS = (byte) 0xB2;
    //    private final static byte APPEND_RECORD = (byte) 0xE2;
    //    private final static byte GET_CHALLENGE = (byte) 0x84;
    //    private final static byte INTERNAL_AUTHENTICATE = (byte) 0x88;
    //    private final static byte EXTERNAL_AUTHENTICATE = (byte) 0x82;
    @JvmStatic
    fun wrapApdu(command: ByteArray): ByteArray? {
        val apduRequiredPart = byteArrayOf(0x00.toByte(), SELECT, 0x04.toByte(), 0x00.toByte())
        if (command.size == 0) {
            return apduRequiredPart
        } else {
            val apduCommandPart = ByteUtils.concatenate(command.size.toByte(), command)
            return ByteUtils.concatenate(apduRequiredPart, apduCommandPart)
        }
    }
}
