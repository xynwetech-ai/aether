package com.aethlauncher.kyn.core.utils

import android.util.Log

/**
 * Logger terpusat. Semua log aplikasi lewat sini supaya mudah
 * difilter dan tidak ada data sensitif yang bocor ke logcat.
 */
object AetherLog {
    private const val TAG = "Aether"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
