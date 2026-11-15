package com.aethlauncher.kyn.core.recovery

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.aethlauncher.kyn.core.utils.AetherLog

enum class EmergencyRoute {
    HOME_SETTINGS,
    DEFAULT_APPS_SETTINGS,
    SYSTEM_SETTINGS
}

sealed interface EmergencyExitResult {
    data class Opened(val route: EmergencyRoute) : EmergencyExitResult
    data object Failed : EmergencyExitResult
}

/**
 * Emergency Exit: membuka layar sistem untuk memilih launcher lain.
 * Tidak butuh biometrik maupun jaringan, dan hanya berjalan lewat aksi pengguna.
 */
class EmergencyRecoveryController {

    fun open(context: Context): EmergencyExitResult {
        for ((route, action) in ROUTES) {
            if (tryStart(context, action)) {
                AetherLog.i("Emergency Exit dibuka lewat $route")
                return EmergencyExitResult.Opened(route)
            }
        }
        AetherLog.w("Emergency Exit: semua rute sistem gagal dibuka")
        return EmergencyExitResult.Failed
    }

    private fun tryStart(context: Context, action: String): Boolean = try {
        context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }

    companion object {
        const val MANUAL_INSTRUCTIONS =
            "Tidak bisa membuka pengaturan otomatis. Buka Pengaturan > Aplikasi > " +
                "Aplikasi default > Aplikasi layar utama (Home), lalu pilih launcher lain. " +
                "Nama menu bisa berbeda di tiap merek HP."

        private val ROUTES = listOf(
            EmergencyRoute.HOME_SETTINGS to Settings.ACTION_HOME_SETTINGS,
            EmergencyRoute.DEFAULT_APPS_SETTINGS to Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS,
            EmergencyRoute.SYSTEM_SETTINGS to Settings.ACTION_SETTINGS
        )
    }
}
