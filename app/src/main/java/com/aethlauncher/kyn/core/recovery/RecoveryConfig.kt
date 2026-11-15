package com.aethlauncher.kyn.core.recovery

/** Ambang deteksi crash loop. Bisa diubah secara internal sesuai PRD 3.2. */
data class RecoveryConfig(
    val confirmedCrashThreshold: Int = 2,
    val confirmedCrashWindowMs: Long = 10_000L,
    val unfinishedStartupThreshold: Int = 3,
    val unfinishedStartupWindowMs: Long = 60_000L,
    val lookbackMs: Long = 300_000L,
    val maxStoredRecords: Int = 20
)
