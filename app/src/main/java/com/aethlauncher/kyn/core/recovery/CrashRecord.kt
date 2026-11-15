package com.aethlauncher.kyn.core.recovery

/** Crash yang berhasil tercatat oleh uncaught exception handler. Tanpa pesan exception. */
data class CrashRecord(
    val timestampMs: Long,
    val summary: String
)

data class CrashStatus(
    val confirmedCrashCount: Int,
    val unfinishedStartupCount: Int,
    val lastCrash: CrashRecord?
)
