package com.gamehub.app.utils

import android.util.Log

object AppLogger {

    private const val TAG = "GameHub"

    fun debug(message: String) {
        Log.d(TAG, message)
    }

    fun error(message: String, exception: Throwable? = null) {
        Log.e(TAG, message, exception)
    }
}