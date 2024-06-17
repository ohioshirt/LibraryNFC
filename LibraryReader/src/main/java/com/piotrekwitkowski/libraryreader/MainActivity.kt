package com.piotrekwitkowski.libraryreader

import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ReaderCallback
import android.nfc.Tag
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.piotrekwitkowski.log.Log.i
import com.piotrekwitkowski.log.Log.reset
import com.piotrekwitkowski.log.Log.setLogTextView
import com.piotrekwitkowski.nfc.desfire.InvalidParameterException

class MainActivity : AppCompatActivity(), ReaderCallback {
    private val libraryReader = LibraryReader(this)
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val logTextView = findViewById<TextView>(R.id.logTextView)
        setLogTextView(logTextView)
        reset(TAG, "onCreate()")
    }

    override fun onResume() {
        super.onResume()
        i(TAG, "onResume()")
        nfcAdapter!!.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
        i(TAG, "NFC adapter enabled. Waiting for a card...")
    }

    override fun onPause() {
        super.onPause()
        i(TAG, "onPause()")
        nfcAdapter!!.disableReaderMode(this)
        i(TAG, "NFC adapter disabled.")
    }

    override fun onTagDiscovered(tag: Tag) {
        reset(TAG, "onTagDiscovered()")
        try {
            libraryReader.processTag(tag)
        } catch (e: InvalidParameterException) {
            i(TAG, e.message!!)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
