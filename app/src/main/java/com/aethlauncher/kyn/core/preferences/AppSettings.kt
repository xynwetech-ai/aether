package com.aethlauncher.kyn.core.preferences

import com.aethlauncher.kyn.core.theme.RenderQuality
import com.aethlauncher.kyn.core.theme.ThemeMode
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.core.theme.WallpaperMode

enum class DrawerSort { ALPHABETICAL, RECENTLY_INSTALLED }

enum class GestureAction { NONE, APP_DRAWER, NOTIFICATIONS, SEARCH, SETTINGS }

data class AppSettings(
    val safeMode: Boolean = false,
    val themeStyle: ThemeStyle = ThemeStyle.LIQUID_GLASS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val wallpaperMode: WallpaperMode = WallpaperMode.PRESET,
    val wallpaperPreset: Int = 0,
    val wallpaperVersion: Int = 0,
    val renderQuality: RenderQuality = RenderQuality.AUTO,
    val animationsEnabled: Boolean = true,
    val gridColumns: Int = 4,
    val gridRows: Int = 6,
    val showLabels: Boolean = true,
    val iconScalePercent: Int = 100,
    val labelSizeSp: Int = 11,
    val defaultPage: Int = 0,
    val dockCount: Int = 4,
    val dockOpacityPercent: Int = 60,
    val showDockLabels: Boolean = false,
    val dockXPercent: Int = 50,
    val dockYPercent: Int = 0,
    val dockScalePercent: Int = 100,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val drawerColumns: Int = 4,
    val drawerSort: DrawerSort = DrawerSort.ALPHABETICAL,
    val swipeUpAction: GestureAction = GestureAction.APP_DRAWER,
    val swipeDownAction: GestureAction = GestureAction.NOTIFICATIONS,
    val doubleTapAction: GestureAction = GestureAction.NONE,
    val appLockEnabled: Boolean = false,
    val lockTimeoutSeconds: Int = 60,
    val pinHash: String = "",
    val pinSalt: String = ""
) {
    val hasPin: Boolean get() = pinHash.isNotEmpty() && pinSalt.isNotEmpty()

    companion object {
        const val MIN_COLUMNS = 3
        const val MAX_COLUMNS = 6
        const val MIN_ROWS = 3
        const val MAX_ROWS = 8
        const val MIN_DOCK = 3
        const val MAX_DOCK = 5
        const val MIN_OPACITY = 10
        const val MAX_OPACITY = 100
        const val MIN_SCALE = 70
        const val MAX_SCALE = 130
        const val MIN_LABEL_SP = 9
        const val MAX_LABEL_SP = 14
        const val MIN_DRAWER_COLUMNS = 3
        const val MAX_DRAWER_COLUMNS = 6

        /** Preset grid dalam format (kolom, baris). */
        val GRID_PRESETS = listOf(4 to 5, 4 to 6, 5 to 5, 5 to 6, 5 to 7)

        val LOCK_TIMEOUTS = listOf(0, 60, 300)
    }
}
