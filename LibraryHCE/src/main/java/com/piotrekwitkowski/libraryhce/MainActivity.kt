package com.piotrekwitkowski.libraryhce

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.piotrekwitkowski.log.Log.reset
import com.piotrekwitkowski.log.Log.setLogTextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val logTextView = findViewById<TextView>(R.id.logTextView)
        setLogTextView(logTextView)
        reset(TAG, "onCreate()")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
