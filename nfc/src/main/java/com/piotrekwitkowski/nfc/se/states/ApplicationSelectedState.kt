package com.piotrekwitkowski.nfc.se.states

import com.piotrekwitkowski.log.Log
import com.piotrekwitkowski.nfc.ByteUtils
import com.piotrekwitkowski.nfc.desfire.Commands
import com.piotrekwitkowski.nfc.desfire.ResponseCodes
import com.piotrekwitkowski.nfc.se.Application
import com.piotrekwitkowski.nfc.se.Authentication
import com.piotrekwitkowski.nfc.se.AuthenticationException
import com.piotrekwitkowski.nfc.se.Command
import com.piotrekwitkowski.nfc.se.NoSuchKeyException

class ApplicationSelectedState internal constructor(private val application: Application) :
    State() {
    private var authenticationInProgress = false
    private var authentication: Authentication? = null

    override fun processCommand(command: Command): CommandResult {
        Log.i(TAG, "processCommand()")

        val commandCode = command.code
        val commandData = command.data

        return try {
            if (!authenticationInProgress && commandCode == Commands.AUTHENTICATE_AES) {
                CommandResult(
                    this,
                    ByteUtils.concatenate(
                        ResponseCodes.ADDITIONAL_FRAME,
                        initiateAESAuthentication(commandData)
                    )
                )
            } else if (authenticationInProgress && commandCode == Commands.ADDITIONAL_FRAME) {
                proceedAuthentication(commandData)
            } else {
                CommandResult(this, ResponseCodes.ILLEGAL_COMMAND)
            }
        } catch (e: AuthenticationException) {
            CommandResult(this, ResponseCodes.AUTHENTICATION_ERROR)
        } catch (e: CommandDataLengthException) {
            CommandResult(this, ResponseCodes.LENGTH_ERROR)
        } catch (e: NoSuchKeyException) {
            CommandResult(this, ResponseCodes.NO_SUCH_KEY)
        }
    }

    @Throws(
        AuthenticationException::class,
        CommandDataLengthException::class,
        NoSuchKeyException::class
    )
    private fun initiateAESAuthentication(commandData: ByteArray?): ByteArray? {
        if (commandData!!.size == 1) {
            val challenge = getChallenge(commandData[0])
            Log.i(TAG, "challenge: " + ByteUtils.toHexString(challenge))
            this.authenticationInProgress = true
            return challenge
        } else {
            throw CommandDataLengthException()
        }
    }

    @Throws(AuthenticationException::class, NoSuchKeyException::class)
    private fun getChallenge(keyNumber: Byte): ByteArray? {
        Log.i(TAG, "proceedAuthentication() $keyNumber")

        try {
            this.authentication = Authentication(this.application)
            return authentication!!.initiate(keyNumber)
        } catch (e: NoSuchKeyException) {
            throw e
        } catch (e: Exception) {
            throw AuthenticationException()
        }
    }

    @Throws(AuthenticationException::class, CommandDataLengthException::class)
    private fun proceedAuthentication(readerChallenge: ByteArray?): CommandResult {
        Log.i(TAG, "proceedAuthentication() " + readerChallenge!!.size)
        if (readerChallenge.size == 32) {
            try {
                val authenticationResponse =
                    authentication!!.proceed(readerChallenge)
                this.authenticationInProgress = false
                val response = ByteUtils.concatenate(
                    ResponseCodes.SUCCESS,
                    authenticationResponse.encryptedRotatedA
                )
                return CommandResult(
                    ApplicationAuthenticatedState(
                        this.application, authenticationResponse.sessionKey
                    ), response
                )
            } catch (e: Exception) {
                throw AuthenticationException()
            }
        } else {
            throw CommandDataLengthException()
        }
    }

    companion object {
        private const val TAG = "ApplicationSelectedState"
    }
}