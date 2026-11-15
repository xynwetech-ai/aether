package com.aethlauncher.kyn.launcher.resolver

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.aethlauncher.kyn.data.models.ShortcutRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LaunchableApp(
    val label: String,
    val ref: ShortcutRef
)

/** Mengambil aplikasi yang punya aktivitas MAIN/LAUNCHER, di luar main thread. */
class LaunchableAppsResolver(context: Context) {

    private val appContext = context.applicationContext

    suspend fun queryAll(): List<LaunchableApp> = withContext(Dispatchers.Default) {
        val pm = appContext.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved: List<ResolveInfo> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                queryLegacy(pm, intent)
            }

        resolved
            .map { it.activityInfo }
            .filter { it.packageName != appContext.packageName }
            .map { info ->
                LaunchableApp(
                    label = info.loadLabel(pm).toString(),
                    ref = ShortcutRef(info.packageName, info.name)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    @Suppress("DEPRECATION")
    private fun queryLegacy(pm: PackageManager, intent: Intent): List<ResolveInfo> =
        pm.queryIntentActivities(intent, 0)
}
