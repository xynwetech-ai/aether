package com.aethlauncher.kyn.launcher.manager

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.aethlauncher.kyn.core.utils.AetherLog
import com.aethlauncher.kyn.data.models.ShortcutRef

object AppLauncher {

    /** Return false jika aplikasi tidak bisa dibuka (dihapus, dinonaktifkan, atau ditolak sistem). */
    fun launch(context: Context, ref: ShortcutRef): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(ref.packageName, ref.className))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            AetherLog.w("Aplikasi tidak ditemukan: ${ref.packageName}")
            false
        } catch (e: SecurityException) {
            AetherLog.w("Aplikasi menolak dibuka: ${ref.packageName}")
            false
        }
    }
}
