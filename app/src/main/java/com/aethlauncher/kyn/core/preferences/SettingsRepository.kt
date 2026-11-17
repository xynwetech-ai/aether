package com.aethlauncher.kyn.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aethlauncher.kyn.core.theme.RenderQuality
import com.aethlauncher.kyn.core.theme.ThemeMode
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.core.theme.WallpaperMode
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.aetherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aether_settings"
)

private object Keys {
    // Key SAFE_MODE sama dengan Milestone 1 agar pengaturan lama tetap terbaca.
    val SAFE_MODE = booleanPreferencesKey("safe_mode_enabled")
    val THEME_STYLE = stringPreferencesKey("theme_style")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    val WALLPAPER_MODE = stringPreferencesKey("wallpaper_mode")
    val WALLPAPER_PRESET = intPreferencesKey("wallpaper_preset")
    val WALLPAPER_VERSION = intPreferencesKey("wallpaper_version")
    val RENDER_QUALITY = stringPreferencesKey("render_quality")
    val ANIMATIONS = booleanPreferencesKey("animations_enabled")
    val GRID_COLUMNS = intPreferencesKey("grid_columns")
    val GRID_ROWS = intPreferencesKey("grid_rows")
    val SHOW_LABELS = booleanPreferencesKey("show_labels")
    val ICON_SCALE = intPreferencesKey("icon_scale_percent")
    val LABEL_SIZE = intPreferencesKey("label_size_sp")
    val DEFAULT_PAGE = intPreferencesKey("default_page")
    val DOCK_COUNT = intPreferencesKey("dock_count")
    val DOCK_OPACITY = intPreferencesKey("dock_opacity_percent")
    val SHOW_DOCK_LABELS = booleanPreferencesKey("show_dock_labels")
    val DOCK_X = intPreferencesKey("dock_x_percent")
    val DOCK_Y = intPreferencesKey("dock_y_percent")
    val DOCK_SCALE = intPreferencesKey("dock_scale_percent")
    val SHOW_CLOCK = booleanPreferencesKey("show_clock")
    val SHOW_DATE = booleanPreferencesKey("show_date")
    val DRAWER_COLUMNS = intPreferencesKey("drawer_columns")
    val DRAWER_SORT = stringPreferencesKey("drawer_sort")
    val SWIPE_UP = stringPreferencesKey("gesture_swipe_up")
    val SWIPE_DOWN = stringPreferencesKey("gesture_swipe_down")
    val DOUBLE_TAP = stringPreferencesKey("gesture_double_tap")
    val APP_LOCK = booleanPreferencesKey("app_lock_enabled")
    val LOCK_TIMEOUT = intPreferencesKey("lock_timeout_seconds")
    val PIN_HASH = stringPreferencesKey("pin_hash")
    val PIN_SALT = stringPreferencesKey("pin_salt")
}

class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.aetherDataStore

    // Data korup atau gagal dibaca jatuh ke default, bukan crash.
    val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs.toSettings() }

    val safeModeEnabled: Flow<Boolean> = settings.map { it.safeMode }

    suspend fun current(): AppSettings = settings.first()

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    suspend fun setSafeMode(enabled: Boolean) {
        update { it.copy(safeMode = enabled) }
    }

    /** Mengembalikan semua pengaturan ke default. */
    suspend fun reset() {
        dataStore.edit { it.clear() }
    }
}

private inline fun <reified T : Enum<T>> Preferences.readEnum(key: Preferences.Key<String>, default: T): T {
    val name = this[key]
    return enumValues<T>().firstOrNull { it.name == name } ?: default
}

private fun Preferences.toSettings(): AppSettings {
    val d = AppSettings()
    return AppSettings(
        safeMode = this[Keys.SAFE_MODE] ?: d.safeMode,
        themeStyle = readEnum(Keys.THEME_STYLE, d.themeStyle),
        themeMode = readEnum(Keys.THEME_MODE, d.themeMode),
        dynamicColor = this[Keys.DYNAMIC_COLOR] ?: d.dynamicColor,
        wallpaperMode = readEnum(Keys.WALLPAPER_MODE, d.wallpaperMode),
        wallpaperPreset = (this[Keys.WALLPAPER_PRESET] ?: d.wallpaperPreset).coerceAtLeast(0),
        wallpaperVersion = this[Keys.WALLPAPER_VERSION] ?: d.wallpaperVersion,
        renderQuality = readEnum(Keys.RENDER_QUALITY, d.renderQuality),
        animationsEnabled = this[Keys.ANIMATIONS] ?: d.animationsEnabled,
        gridColumns = (this[Keys.GRID_COLUMNS] ?: d.gridColumns)
            .coerceIn(AppSettings.MIN_COLUMNS, AppSettings.MAX_COLUMNS),
        gridRows = (this[Keys.GRID_ROWS] ?: d.gridRows)
            .coerceIn(AppSettings.MIN_ROWS, AppSettings.MAX_ROWS),
        showLabels = this[Keys.SHOW_LABELS] ?: d.showLabels,
        iconScalePercent = (this[Keys.ICON_SCALE] ?: d.iconScalePercent)
            .coerceIn(AppSettings.MIN_SCALE, AppSettings.MAX_SCALE),
        labelSizeSp = (this[Keys.LABEL_SIZE] ?: d.labelSizeSp)
            .coerceIn(AppSettings.MIN_LABEL_SP, AppSettings.MAX_LABEL_SP),
        defaultPage = (this[Keys.DEFAULT_PAGE] ?: d.defaultPage).coerceAtLeast(0),
        dockCount = (this[Keys.DOCK_COUNT] ?: d.dockCount)
            .coerceIn(AppSettings.MIN_DOCK, AppSettings.MAX_DOCK),
        dockOpacityPercent = (this[Keys.DOCK_OPACITY] ?: d.dockOpacityPercent)
            .coerceIn(AppSettings.MIN_OPACITY, AppSettings.MAX_OPACITY),
        showDockLabels = this[Keys.SHOW_DOCK_LABELS] ?: d.showDockLabels,
        dockXPercent = (this[Keys.DOCK_X] ?: d.dockXPercent).coerceIn(0, 100),
        dockYPercent = (this[Keys.DOCK_Y] ?: d.dockYPercent).coerceIn(0, 100),
        dockScalePercent = (this[Keys.DOCK_SCALE] ?: d.dockScalePercent)
            .coerceIn(AppSettings.MIN_SCALE, AppSettings.MAX_SCALE),
        showClock = this[Keys.SHOW_CLOCK] ?: d.showClock,
        showDate = this[Keys.SHOW_DATE] ?: d.showDate,
        drawerColumns = (this[Keys.DRAWER_COLUMNS] ?: d.drawerColumns)
            .coerceIn(AppSettings.MIN_DRAWER_COLUMNS, AppSettings.MAX_DRAWER_COLUMNS),
        drawerSort = readEnum(Keys.DRAWER_SORT, d.drawerSort),
        swipeUpAction = readEnum(Keys.SWIPE_UP, d.swipeUpAction),
        swipeDownAction = readEnum(Keys.SWIPE_DOWN, d.swipeDownAction),
        doubleTapAction = readEnum(Keys.DOUBLE_TAP, d.doubleTapAction),
        appLockEnabled = this[Keys.APP_LOCK] ?: d.appLockEnabled,
        lockTimeoutSeconds = (this[Keys.LOCK_TIMEOUT] ?: d.lockTimeoutSeconds).coerceAtLeast(0),
        pinHash = this[Keys.PIN_HASH] ?: d.pinHash,
        pinSalt = this[Keys.PIN_SALT] ?: d.pinSalt
    )
}

private fun MutablePreferences.write(s: AppSettings) {
    this[Keys.SAFE_MODE] = s.safeMode
    this[Keys.THEME_STYLE] = s.themeStyle.name
    this[Keys.THEME_MODE] = s.themeMode.name
    this[Keys.DYNAMIC_COLOR] = s.dynamicColor
    this[Keys.WALLPAPER_MODE] = s.wallpaperMode.name
    this[Keys.WALLPAPER_PRESET] = s.wallpaperPreset
    this[Keys.WALLPAPER_VERSION] = s.wallpaperVersion
    this[Keys.RENDER_QUALITY] = s.renderQuality.name
    this[Keys.ANIMATIONS] = s.animationsEnabled
    this[Keys.GRID_COLUMNS] = s.gridColumns.coerceIn(AppSettings.MIN_COLUMNS, AppSettings.MAX_COLUMNS)
    this[Keys.GRID_ROWS] = s.gridRows.coerceIn(AppSettings.MIN_ROWS, AppSettings.MAX_ROWS)
    this[Keys.SHOW_LABELS] = s.showLabels
    this[Keys.ICON_SCALE] = s.iconScalePercent.coerceIn(AppSettings.MIN_SCALE, AppSettings.MAX_SCALE)
    this[Keys.LABEL_SIZE] = s.labelSizeSp.coerceIn(AppSettings.MIN_LABEL_SP, AppSettings.MAX_LABEL_SP)
    this[Keys.DEFAULT_PAGE] = s.defaultPage.coerceAtLeast(0)
    this[Keys.DOCK_COUNT] = s.dockCount.coerceIn(AppSettings.MIN_DOCK, AppSettings.MAX_DOCK)
    this[Keys.DOCK_OPACITY] = s.dockOpacityPercent.coerceIn(AppSettings.MIN_OPACITY, AppSettings.MAX_OPACITY)
    this[Keys.SHOW_DOCK_LABELS] = s.showDockLabels
    this[Keys.DOCK_X] = s.dockXPercent.coerceIn(0, 100)
    this[Keys.DOCK_Y] = s.dockYPercent.coerceIn(0, 100)
    this[Keys.DOCK_SCALE] = s.dockScalePercent.coerceIn(AppSettings.MIN_SCALE, AppSettings.MAX_SCALE)
    this[Keys.SHOW_CLOCK] = s.showClock
    this[Keys.SHOW_DATE] = s.showDate
    this[Keys.DRAWER_COLUMNS] = s.drawerColumns.coerceIn(
        AppSettings.MIN_DRAWER_COLUMNS, AppSettings.MAX_DRAWER_COLUMNS
    )
    this[Keys.DRAWER_SORT] = s.drawerSort.name
    this[Keys.SWIPE_UP] = s.swipeUpAction.name
    this[Keys.SWIPE_DOWN] = s.swipeDownAction.name
    this[Keys.DOUBLE_TAP] = s.doubleTapAction.name
    this[Keys.APP_LOCK] = s.appLockEnabled
    this[Keys.LOCK_TIMEOUT] = s.lockTimeoutSeconds.coerceAtLeast(0)
    this[Keys.PIN_HASH] = s.pinHash
    this[Keys.PIN_SALT] = s.pinSalt
}
