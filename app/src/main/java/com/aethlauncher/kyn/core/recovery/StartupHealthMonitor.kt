package com.aethlauncher.kyn.core.recovery

/**
 * Memantau apakah startup sebelumnya selesai.
 *
 * Startup dianggap "berjalan" sejak Application.onCreate sampai UI pertama tampil.
 * Jika proses berikutnya menemukan flag masih menyala, startup sebelumnya tidak
 * selesai (crash tak tertangkap, ANR, native crash, atau proses dimatikan). Android
 * tidak memberi cara andal untuk membedakannya, jadi dicatat terpisah dari crash
 * yang terkonfirmasi.
 */
class StartupHealthMonitor(
    private val history: CrashHistoryRepository,
    private val clock: () -> Long = System::currentTimeMillis
) {
    /** Return true jika startup sebelumnya tidak selesai. */
    fun onProcessStart(): Boolean {
        val previousUnfinished = history.isStartupInProgress()
        if (previousUnfinished) {
            history.addUnfinishedStartup(clock())
        }
        history.setStartupInProgress(true)
        return previousUnfinished
    }

    fun onStartupCompleted() {
        history.setStartupInProgress(false)
    }
}
