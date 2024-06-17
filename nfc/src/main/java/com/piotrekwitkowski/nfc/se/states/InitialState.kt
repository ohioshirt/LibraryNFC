package com.piotrekwitkowski.nfc.se.states

import com.piotrekwitkowski.log.Log
import com.piotrekwitkowski.nfc.desfire.AID
import com.piotrekwitkowski.nfc.desfire.Commands
import com.piotrekwitkowski.nfc.desfire.InvalidParameterException
import com.piotrekwitkowski.nfc.desfire.ResponseCodes
import com.piotrekwitkowski.nfc.se.Application
import com.piotrekwitkowski.nfc.se.Command

class InitialState(private val applications: Array<Application>) : State() {
    override fun processCommand(command: Command): CommandResult {
        Log.i(TAG, "processCommand()")

        return if (command.code == Commands.SELECT_APPLICATION) {
            selectApplication(command.data)
        } else {
            CommandResult(
                this,
                ResponseCodes.ILLEGAL_COMMAND
            )
        }
    }

    private fun selectApplication(aid: ByteArray?): CommandResult {
        Log.i(TAG, "selectApplication()")

        try {
            val aidToSelect = AID(aid)
            return CommandResult(selectApplication(aidToSelect), ResponseCodes.SUCCESS)
        } catch (ex: InvalidParameterException) {
            return CommandResult(this, ResponseCodes.LENGTH_ERROR)
        } catch (ex: ApplicationNotFoundException) {
            return CommandResult(this, ResponseCodes.APPLICATION_NOT_FOUND)
        }
    }

    @Throws(ApplicationNotFoundException::class)
    private fun selectApplication(aidToSelect: AID): ApplicationSelectedState {
        Log.i(TAG, "selectApplication()")
        for (a in applications) {
            if (a.aid.equals(aidToSelect)) {
                return ApplicationSelectedState(a)
            }
        }
        throw ApplicationNotFoundException()
    }

    companion object {
        private const val TAG = "InitialState"
    }
}
