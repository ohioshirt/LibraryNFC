package com.piotrekwitkowski.log

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView

@SuppressLint("SetTextI18n")
object Log {
    @SuppressLint("StaticFieldLeak")
    private var logTextView: TextView? = null

    @JvmStatic
    fun setLogTextView(tv: TextView?) {
        logTextView = tv
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        Handler(Looper.getMainLooper()).post {
            logTextView!!.text = logTextView!!.text.toString() + format(tag, msg)
        }
    }

    @JvmStatic
    fun reset(tag: String, msg: String) {
        Log.i(tag, msg)
        Handler(Looper.getMainLooper()).post { logTextView!!.text = format(tag, msg) }
    }

    private fun format(tag: String, msg: String): String {
        return "$tag: $msg\n"
    }
}
