package fivecc.tools.shortcut_helper.utils

import android.util.Log

const val TRACE_TAG = "Trace"

inline fun <T> trace(desc: String, block: () -> T): T {
    Log.d(TRACE_TAG, "on ${Thread.currentThread()}: $desc")
    val startTime = System.currentTimeMillis()
    try {
        return block()
    } finally {
        val time = System.currentTimeMillis() - startTime
        Log.d(TRACE_TAG, "execute done ($time): $desc")
    }
}