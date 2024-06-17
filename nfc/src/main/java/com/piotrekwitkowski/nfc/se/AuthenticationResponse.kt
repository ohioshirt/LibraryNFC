package com.piotrekwitkowski.nfc.se

class AuthenticationResponse internal constructor(
    val sessionKey: ByteArray,
    val encryptedRotatedA: ByteArray
)
