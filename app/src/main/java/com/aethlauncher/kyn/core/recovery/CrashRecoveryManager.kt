package com.aethlauncher.kyn.core.recovery

import com.aethlauncher.kyn.core.utils.AetherLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Mencatat crash dan menentukan apakah layar pemulihan perlu ditampilkan.
 *
 * Handler hanya MENCATAT crash. Handler tidak membuka Activity apa pun dari proses
 * yang sedang mati; keputusan pemulihan diambil pada startup berikutnya.
 * Mekanisme ini tidak menjamin mendeteksi semua ANR, native crash, atau
 * penghentian proses oleh sistem.
 */
class CrashRecoveryManager(
    private val history: CrashHistoryRepository,
    private val healthMonitor: StartupHealthMonitor,
    private val config: RecoveryConfig = RecoveryConfig(),
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val _state = MutableStateFlow<RecoveryDecision>(RecoveryDecision.Normal)
    val state: StateFlow<RecoveryDecision> = _state.asStateFlow()

    /** Dipanggil sekali dari Application.onCreate. */
    fun onApplicationCreate() {
        installCrashHandler()
        healthMonitor.onProcessStart()
        _state.value = evaluate()
        AetherLog.i("Status pemulihan: ${_state.value}")
    }

    /** Dipanggil saat UI pertama sudah tampil. */
    fun markStartupCompleted() {
        healthMonitor.onStartupCompleted()
    }

    fun clearHistory() {
        history.clearHistory()
        _state.value = RecoveryDecision.Normal
    }

    fun getStatus(): CrashStatus {
        val crashes = history.getConfirmedCrashes()
        return CrashStatus(
            confirmedCrashCount = crashes.size,
            unfinishedStartupCount = history.getUnfinishedStartups().size,
            lastCrash = crashes.lastOrNull()
        )
    }

    private fun evaluate(): RecoveryDecision = RecoveryEvaluator.evaluate(
        confirmedCrashTimes = history.getConfirmedCrashes().map { it.timestampMs },
        unfinishedStartupTimes = history.getUnfinishedStartups(),
        nowMs = clock(),
        config = config
    )

    private fun installCrashHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                history.addConfirmedCrash(CrashRecord(clock(), summarize(throwable)))
            } catch (e: Throwable) {
                // Handler tidak boleh melempar error baru; kegagalan mencatat diabaikan.
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    // Hanya nama class dan lokasi frame teratas. Pesan exception sengaja tidak disimpan.
    private fun summarize(throwable: Throwable): String {
        val top = throwable.stackTrace.firstOrNull()
        val where = if (top != null) "${top.className}.${top.methodName}:${top.lineNumber}" else "unknown"
        return "${throwable.javaClass.simpleName} @ $where"
    }
}
