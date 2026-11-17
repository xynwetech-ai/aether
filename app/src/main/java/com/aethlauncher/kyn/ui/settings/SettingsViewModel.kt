package com.aethlauncher.kyn.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aethlauncher.kyn.core.AppContainer
import com.aethlauncher.kyn.core.preferences.AppPrefs
import com.aethlauncher.kyn.core.preferences.AppPrefsCodec
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.preferences.CustomCategory
import com.aethlauncher.kyn.core.preferences.DrawerSort
import com.aethlauncher.kyn.core.preferences.GestureAction
import com.aethlauncher.kyn.core.recovery.CrashRecord
import com.aethlauncher.kyn.core.recovery.CrashStatus
import com.aethlauncher.kyn.core.recovery.EmergencyExitResult
import com.aethlauncher.kyn.core.security.PinHasher
import com.aethlauncher.kyn.core.theme.RenderQuality
import com.aethlauncher.kyn.core.theme.ThemeMode
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.core.theme.WallpaperMode
import com.aethlauncher.kyn.data.models.HomeLayout
import com.aethlauncher.kyn.domain.usecase.HomeLayoutOps
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val layout: HomeLayout = HomeLayout(),
    val crashStatus: CrashStatus = CrashStatus(0, 0, null),
    val appPrefs: AppPrefs = AppPrefs(),
    val apps: List<LaunchableApp> = emptyList()
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val crashStatus = MutableStateFlow(container.crashRecoveryManager.getStatus())

    private val base = combine(
        container.settingsRepository.settings,
        container.layoutRepository.layout,
        crashStatus
    ) { settings, layout, status -> Triple(settings, layout, status) }

    val uiState: StateFlow<SettingsUiState> = combine(
        base,
        container.appsRepository.prefs,
        container.appsStore.apps
    ) { triple, prefs, apps ->
        SettingsUiState(triple.first, triple.second, triple.third, prefs, apps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    val iconLoader: AppIconLoader get() = container.iconLoader

    // ---- tema & wallpaper ----
    fun setThemeStyle(style: ThemeStyle) = updateSettings { it.copy(themeStyle = style) }
    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }
    fun setDynamicColor(enabled: Boolean) = updateSettings { it.copy(dynamicColor = enabled) }

    fun setWallpaperPreset(index: Int) = updateSettings {
        it.copy(
            wallpaperMode = WallpaperMode.PRESET,
            wallpaperPreset = index,
            wallpaperVersion = it.wallpaperVersion + 1
        )
    }

    fun useSystemWallpaper() = updateSettings { it.copy(wallpaperMode = WallpaperMode.SYSTEM) }

    fun importWallpaper(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = container.wallpaperRepository.importFrom(uri)
            if (ok) useCustomWallpaper()
            onResult(ok)
        }
    }

    fun copySystemWallpaper(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = container.wallpaperRepository.copySystemWallpaper()
            if (ok) useCustomWallpaper()
            onResult(ok)
        }
    }

    private suspend fun useCustomWallpaper() {
        container.settingsRepository.update {
            it.copy(wallpaperMode = WallpaperMode.CUSTOM, wallpaperVersion = it.wallpaperVersion + 1)
        }
    }

    // ---- grid & home ----
    fun setGrid(columns: Int, rows: Int) =
        updateSettings { it.copy(gridColumns = columns, gridRows = rows) }

    fun setGridColumns(columns: Int) = updateSettings { it.copy(gridColumns = columns) }
    fun setGridRows(rows: Int) = updateSettings { it.copy(gridRows = rows) }
    fun setShowLabels(show: Boolean) = updateSettings { it.copy(showLabels = show) }
    fun setIconScale(percent: Int) = updateSettings { it.copy(iconScalePercent = percent) }
    fun setLabelSize(sp: Int) = updateSettings { it.copy(labelSizeSp = sp) }
    fun setShowClock(show: Boolean) = updateSettings { it.copy(showClock = show) }
    fun setShowDate(show: Boolean) = updateSettings { it.copy(showDate = show) }
    fun setDefaultPage(index: Int) = updateSettings { it.copy(defaultPage = index) }

    // ---- dock ----
    fun setDockCount(count: Int) = updateSettings { it.copy(dockCount = count) }
    fun setDockOpacity(percent: Int) = updateSettings { it.copy(dockOpacityPercent = percent) }
    fun setShowDockLabels(show: Boolean) = updateSettings { it.copy(showDockLabels = show) }
    fun setDockX(percent: Int) = updateSettings { it.copy(dockXPercent = percent) }
    fun setDockY(percent: Int) = updateSettings { it.copy(dockYPercent = percent) }
    fun setDockScale(percent: Int) = updateSettings { it.copy(dockScalePercent = percent) }
    fun resetDockGeometry() =
        updateSettings { it.copy(dockXPercent = 50, dockYPercent = 0, dockScalePercent = 100) }

    // ---- drawer ----
    fun setDrawerColumns(columns: Int) = updateSettings { it.copy(drawerColumns = columns) }
    fun setDrawerSort(sort: DrawerSort) = updateSettings { it.copy(drawerSort = sort) }

    fun addCategory(name: String) {
        val clean = AppPrefsCodec.cleanName(name)
        if (clean.isEmpty()) return
        updateApps {
            val id = "c" + System.currentTimeMillis().toString(36)
            it.copy(customCategories = it.customCategories + CustomCategory(id, clean))
        }
    }

    fun renameCategory(id: String, name: String) {
        val clean = AppPrefsCodec.cleanName(name)
        if (clean.isEmpty()) return
        updateApps { prefs ->
            prefs.copy(customCategories = prefs.customCategories.map { if (it.id == id) it.copy(name = clean) else it })
        }
    }

    fun deleteCategory(id: String) = updateApps { prefs ->
        prefs.copy(
            customCategories = prefs.customCategories.filter { it.id != id },
            categoryOverrides = prefs.categoryOverrides.filterValues { it != id }
        )
    }

    // ---- gestur ----
    fun setSwipeUp(action: GestureAction) = updateSettings { it.copy(swipeUpAction = action) }
    fun setSwipeDown(action: GestureAction) = updateSettings { it.copy(swipeDownAction = action) }
    fun setDoubleTap(action: GestureAction) = updateSettings { it.copy(doubleTapAction = action) }

    // ---- halaman ----
    fun addPage() {
        viewModelScope.launch { container.layoutRepository.update { HomeLayoutOps.addPage(it) } }
    }

    fun removePage(index: Int) {
        viewModelScope.launch {
            val before = container.layoutRepository.layout.first()
            val after = HomeLayoutOps.removePage(before, index)
            if (after == before) return@launch
            container.layoutRepository.update { after }
            container.settingsRepository.update {
                it.copy(
                    defaultPage = HomeLayoutOps.defaultPageAfterRemove(it.defaultPage, index, after.pages.size)
                )
            }
        }
    }

    fun movePage(index: Int, delta: Int) {
        viewModelScope.launch {
            val before = container.layoutRepository.layout.first()
            val after = HomeLayoutOps.movePage(before, index, delta)
            if (after == before) return@launch
            container.layoutRepository.update { after }
            container.settingsRepository.update {
                it.copy(defaultPage = HomeLayoutOps.defaultPageAfterMove(it.defaultPage, index, delta))
            }
        }
    }

    // ---- keamanan ----
    fun setSafeMode(enabled: Boolean) = updateSettings { it.copy(safeMode = enabled) }

    fun setAppLockEnabled(enabled: Boolean) = updateSettings { it.copy(appLockEnabled = enabled) }
    fun setLockTimeout(seconds: Int) = updateSettings { it.copy(lockTimeoutSeconds = seconds) }

    /** Hash PBKDF2 dihitung di luar main thread. PIN asli tidak pernah disimpan. */
    fun setPin(pin: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val salt = PinHasher.newSalt()
            val hash = PinHasher.hash(pin, salt)
            container.settingsRepository.update { it.copy(pinSalt = salt, pinHash = hash) }
        }
    }

    fun clearPin() = updateSettings { it.copy(pinHash = "", pinSalt = "", appLockEnabled = false) }

    fun setHidden(key: String, hidden: Boolean) = updateApps {
        it.copy(hidden = if (hidden) it.hidden + key else it.hidden - key)
    }

    fun setLocked(key: String, locked: Boolean) = updateApps {
        it.copy(locked = if (locked) it.locked + key else it.locked - key)
    }

    fun refreshStatus() {
        crashStatus.value = container.crashRecoveryManager.getStatus()
    }

    fun clearCrashHistory() {
        container.crashRecoveryManager.clearHistory()
        refreshStatus()
    }

    fun openEmergencyExit(context: Context): EmergencyExitResult =
        container.emergencyRecoveryController.open(context)

    /** Khusus debug: mencatat dua crash palsu agar layar pemulihan bisa diuji. */
    fun simulateCrashLoop() {
        val now = System.currentTimeMillis()
        container.crashHistory.addConfirmedCrash(CrashRecord(now - 1_000L, "Simulasi crash 1"))
        container.crashHistory.addConfirmedCrash(CrashRecord(now, "Simulasi crash 2"))
        refreshStatus()
    }

    // ---- performa & reset ----
    fun setRenderQuality(quality: RenderQuality) = updateSettings { it.copy(renderQuality = quality) }
    fun setAnimations(enabled: Boolean) = updateSettings { it.copy(animationsEnabled = enabled) }

    fun clearIconCache() {
        container.iconCache.clear()
    }

    /** Mengembalikan pengaturan, susunan Home, dan preferensi aplikasi ke default. */
    fun resetAll() {
        viewModelScope.launch {
            container.settingsRepository.reset()
            container.layoutRepository.reset()
            container.appsRepository.reset()
        }
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { container.settingsRepository.update(transform) }
    }

    private fun updateApps(transform: (AppPrefs) -> AppPrefs) {
        viewModelScope.launch { container.appsRepository.update(transform) }
    }
}
