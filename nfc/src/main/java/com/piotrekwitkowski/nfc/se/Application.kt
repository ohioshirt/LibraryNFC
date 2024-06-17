package com.piotrekwitkowski.nfc.se

import com.piotrekwitkowski.nfc.desfire.AESKey
import com.piotrekwitkowski.nfc.desfire.AID
import com.piotrekwitkowski.nfc.desfire.File

abstract class Application protected constructor(val aid: AID, val key0: AESKey, val file0: File)
