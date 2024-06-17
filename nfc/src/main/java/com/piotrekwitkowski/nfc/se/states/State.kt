package com.piotrekwitkowski.nfc.se.states

import com.piotrekwitkowski.nfc.se.Command

abstract class State {
    abstract fun processCommand(c: Command): CommandResult
}
