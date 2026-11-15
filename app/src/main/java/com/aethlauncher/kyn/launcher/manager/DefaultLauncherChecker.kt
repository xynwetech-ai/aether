package com.aethlauncher.kyn.launcher.manager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/** Hanya membaca status. Aether tidak pernah memaksa dirinya jadi default launcher. */
object DefaultLauncherChecker {

    fun isDefaultHome(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager
            .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName == context.packageName
    }
}
