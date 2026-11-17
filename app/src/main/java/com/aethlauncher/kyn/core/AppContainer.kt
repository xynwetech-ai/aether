package com.aethlauncher.kyn.core

import android.app.ActivityManager
import android.content.Context
import com.aethlauncher.kyn.core.performance.IconCache
import com.aethlauncher.kyn.core.preferences.AppsRepository
import com.aethlauncher.kyn.core.preferences.SettingsRepository
import com.aethlauncher.kyn.core.recovery.CrashHistoryRepository
import com.aethlauncher.kyn.core.recovery.CrashRecoveryManager
import com.aethlauncher.kyn.core.recovery.EmergencyRecoveryController
import com.aethlauncher.kyn.core.recovery.StartupHealthMonitor
import com.aethlauncher.kyn.core.wallpaper.WallpaperRepository
import com.aethlauncher.kyn.data.repository.LayoutRepository
import com.aethlauncher.kyn.launcher.manager.PackageChangeObserver
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.AppsStore
import com.aethlauncher.kyn.launcher.resolver.LaunchableAppsResolver

/** Dependency container manual (tanpa library DI) untuk menjaga dependensi tetap minimal. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val crashHistory = CrashHistoryRepository(appContext)
    val startupHealthMonitor = StartupHealthMonitor(crashHistory)
    val crashRecoveryManager = CrashRecoveryManager(crashHistory, startupHealthMonitor)
    val emergencyRecoveryController = EmergencyRecoveryController()

    val settingsRepository = SettingsRepository(appContext)
    val layoutRepository = LayoutRepository(appContext)
    val appsRepository = AppsRepository(appContext)
    val wallpaperRepository = WallpaperRepository(appContext)

    val appsStore = AppsStore(LaunchableAppsResolver(appContext))
    val iconCache = IconCache()
    val iconLoader = AppIconLoader(appContext, iconCache)

    val isLowRamDevice: Boolean =
        (appContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.isLowRamDevice == true

    private val packageObserver = PackageChangeObserver(appContext) { appsStore.notifyPackagesChanged() }

    init {
        packageObserver.start()
    }
}
