package com.aethlauncher.kyn.core.recovery

import android.annotation.SuppressLint
import android.content.Context

/**
 * Penyimpanan state crash dan startup.
 *
 * Memakai SharedPreferences dengan commit() (sinkron) karena ditulis dari
 * uncaught exception handler: proses bisa mati sebelum apply() sempat selesai.
 */
@SuppressLint("ApplySharedPref")
class CrashHistoryRepository(
    context: Context,
    private val maxRecords: Int = RecoveryConfig().maxStoredRecords
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun getConfirmedCrashes(): List<CrashRecord> =
        decodeCrashes(prefs.getString(KEY_CRASHES, null))

    @Synchronized
    fun getUnfinishedStartups(): List<Long> =
        decodeTimes(prefs.getString(KEY_UNFINISHED, null))

    @Synchronized
    fun isStartupInProgress(): Boolean =
        prefs.getBoolean(KEY_STARTUP_IN_PROGRESS, false)

    /** Crash tercatat sudah menjelaskan startup yang gagal, jadi flag startup ikut dibersihkan. */
    @Synchronized
    fun addConfirmedCrash(record: CrashRecord) {
        val updated = (getConfirmedCrashes() + record).takeLast(maxRecords)
        prefs.edit()
            .putString(KEY_CRASHES, encodeCrashes(updated))
            .putBoolean(KEY_STARTUP_IN_PROGRESS, false)
            .commit()
    }

    @Synchronized
    fun addUnfinishedStartup(timestampMs: Long) {
        val updated = (getUnfinishedStartups() + timestampMs).takeLast(maxRecords)
        prefs.edit().putString(KEY_UNFINISHED, encodeTimes(updated)).commit()
    }

    @Synchronized
    fun setStartupInProgress(value: Boolean) {
        prefs.edit().putBoolean(KEY_STARTUP_IN_PROGRESS, value).commit()
    }

    @Synchronized
    fun clearHistory() {
        prefs.edit().remove(KEY_CRASHES).remove(KEY_UNFINISHED).commit()
    }

    private fun encodeCrashes(records: List<CrashRecord>): String =
        records.joinToString("\n") { "${it.timestampMs}|${sanitize(it.summary)}" }

    // Baris yang rusak dilewati supaya data korup tidak menggagalkan startup.
    private fun decodeCrashes(raw: String?): List<CrashRecord> {
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.split("\n").mapNotNull { line ->
            val separator = line.indexOf('|')
            if (separator <= 0) {
                null
            } else {
                line.substring(0, separator).toLongOrNull()
                    ?.let { CrashRecord(it, line.substring(separator + 1)) }
            }
        }
    }

    private fun encodeTimes(times: List<Long>): String = times.joinToString(",")

    private fun decodeTimes(raw: String?): List<Long> {
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.split(",").mapNotNull { it.toLongOrNull() }
    }

    private fun sanitize(text: String): String =
        text.replace('\n', ' ').replace('|', '/').take(MAX_SUMMARY_LENGTH)

    private companion object {
        const val PREFS_NAME = "aether_crash_state"
        const val KEY_CRASHES = "confirmed_crashes"
        const val KEY_UNFINISHED = "unfinished_startups"
        const val KEY_STARTUP_IN_PROGRESS = "startup_in_progress"
        const val MAX_SUMMARY_LENGTH = 200
    }
}
