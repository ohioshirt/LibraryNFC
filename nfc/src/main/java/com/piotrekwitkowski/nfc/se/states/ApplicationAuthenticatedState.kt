package com.piotrekwitkowski.nfc.se.states

import com.piotrekwitkowski.log.Log
import com.piotrekwitkowski.nfc.ByteUtils
import com.piotrekwitkowski.nfc.desfire.Commands
import com.piotrekwitkowski.nfc.desfire.File
import com.piotrekwitkowski.nfc.desfire.ResponseCodes
import com.piotrekwitkowski.nfc.se.Application
import com.piotrekwitkowski.nfc.se.Command

class ApplicationAuthenticatedState internal constructor(
    private val application: Application,
    private val sessionKey: ByteArray?
) : State() {
    override fun processCommand(command: Command): CommandResult {
        Log.i(TAG, "processCommand()")

        if (command.code == Commands.READ_DATA) {
            val commandData = command.data
            return if (commandData!!.size == 7) {
                readData(commandData)
            } else {
                CommandResult(
                    this,
                    ResponseCodes.LENGTH_ERROR
                )
            }
        } else {
            return CommandResult(this, ResponseCodes.ILLEGAL_COMMAND)
        }
    }

    private fun readData(commandData: ByteArray?): CommandResult {
        val fileNumber = commandData!![0]
        if (fileNumber.toInt() == 0) {
            val file = application.file0
            return readFile(file, commandData)
        } else {
            return CommandResult(this, ResponseCodes.FILE_NOT_FOUND)
        }
    }

    private fun readFile(file: File?, commandData: ByteArray?): CommandResult {
        val offsetBytes = byteArrayOf(commandData!![1], commandData[2], commandData[3])
        val lengthBytes = byteArrayOf(commandData[4], commandData[5], commandData[6])
        val offset = ByteUtils.threeBytesToInt(offsetBytes)
        val length = ByteUtils.threeBytesToInt(lengthBytes)

        try {
            var data = file!!.readData(offset, length)
            data = ByteUtils.concatenate(data, getCRC(data))
            return CommandResult(this, ByteUtils.concatenate(ResponseCodes.SUCCESS, data))
        } catch (e: Exception) {
            e.printStackTrace()
            return CommandResult(this, ResponseCodes.BOUNDARY_ERROR)
        }
    }

    private fun getCRC(data: ByteArray?): ByteArray {
        Log.i(
            TAG, "sessionKey: " + ByteUtils.toHexString(
                sessionKey
            )
        )
        Log.i(TAG, "generating CRC for: " + ByteUtils.toHexString(data))

        // TODO: implement CRC
        return ByteArray(8)
    }

    companion object {
        private const val TAG = "ApplicationAuthenticatedState"
    }
}

