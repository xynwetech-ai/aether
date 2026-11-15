package com.aethlauncher.kyn.core.recovery

enum class RecoveryReason {
    REPEATED_CRASHES,
    SUSPICIOUS_STARTUP_LOOP
}

sealed interface RecoveryDecision {
    data object Normal : RecoveryDecision
    data class Required(val reason: RecoveryReason, val eventCount: Int) : RecoveryDecision
}

/** Logika murni (tanpa Android) supaya mudah diuji unit test. */
object RecoveryEvaluator {

    fun evaluate(
        confirmedCrashTimes: List<Long>,
        unfinishedStartupTimes: List<Long>,
        nowMs: Long,
        config: RecoveryConfig = RecoveryConfig()
    ): RecoveryDecision {
        val recentCrashes = confirmedCrashTimes.filter { isRecent(it, nowMs, config.lookbackMs) }
        if (hasBurst(recentCrashes, config.confirmedCrashThreshold, config.confirmedCrashWindowMs)) {
            return RecoveryDecision.Required(RecoveryReason.REPEATED_CRASHES, recentCrashes.size)
        }

        val recentUnfinished = unfinishedStartupTimes.filter { isRecent(it, nowMs, config.lookbackMs) }
        if (hasBurst(recentUnfinished, config.unfinishedStartupThreshold, config.unfinishedStartupWindowMs)) {
            return RecoveryDecision.Required(RecoveryReason.SUSPICIOUS_STARTUP_LOOP, recentUnfinished.size)
        }

        return RecoveryDecision.Normal
    }

    private fun isRecent(timeMs: Long, nowMs: Long, lookbackMs: Long): Boolean {
        val age = nowMs - timeMs
        return age in 0L..lookbackMs
    }

    /** True jika ada [threshold] kejadian yang semuanya jatuh dalam [windowMs]. */
    private fun hasBurst(times: List<Long>, threshold: Int, windowMs: Long): Boolean {
        if (threshold <= 0 || times.size < threshold) return false
        val sorted = times.sorted()
        for (i in 0..sorted.size - threshold) {
            if (sorted[i + threshold - 1] - sorted[i] <= windowMs) return true
        }
        return false
    }
}
