package com.gamehub.app.utils

import android.util.Log

/**
 * Central logging helper. Every message uses the "GameHub" tag, so filtering Logcat by that
 * word shows only our messages.
 *
 * android.util.Log throws inside plain JVM unit tests, so each call is wrapped: on a phone it
 * behaves exactly like Log, and in a unit test it quietly does nothing. That lets
 * ViewModels and repositories log freely and still be unit tested.
 */
object AppLogger {

    private const val TAG = "GameHub"

    fun debug(message: String) = safely { Log.d(TAG, message) }

    fun info(message: String) = safely { Log.i(TAG, message) }

    fun warn(message: String) = safely { Log.w(TAG, message) }

    fun error(message: String, exception: Throwable? = null) = safely { Log.e(TAG, message, exception) }

    private inline fun safely(block: () -> Unit) {
        try {
            block()
        } catch (ignored: RuntimeException) {
            // Running in a JVM unit test where android.util.Log is not available.
        }
    }
}