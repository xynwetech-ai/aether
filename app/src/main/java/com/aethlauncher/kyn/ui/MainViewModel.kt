package com.aethlauncher.kyn.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aethlauncher.kyn.core.AppContainer
import com.aethlauncher.kyn.core.preferences.AppPrefs
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.recovery.EmergencyExitResult
import com.aethlauncher.kyn.core.recovery.RecoveryDecision
import com.aethlauncher.kyn.core.theme.RenderPolicy
import com.aethlauncher.kyn.core.theme.WallpaperLayers
import com.aethlauncher.kyn.core.theme.WallpaperMode
import com.aethlauncher.kyn.core.utils.AetherLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class WallpaperKey(
    val mode: WallpaperMode,
    val preset: Int,
    val version: Int,
    val blurWidth: Int,
    val blurRadius: Int
)

class MainViewModel(private val container: AppContainer) : ViewModel() {

    val recoveryState: StateFlow<RecoveryDecision> = container.crashRecoveryManager.state

    /** Null sampai pengaturan selesai dibaca dari DataStore. */
    val settings: StateFlow<AppSettings?> = container.settingsRepository.settings
        .map<AppSettings, AppSettings?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val appPrefs: StateFlow<AppPrefs> = container.appsRepository.prefs
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppPrefs())

    private val _wallpaper = MutableStateFlow<WallpaperLayers?>(null)
    val wallpaper: StateFlow<WallpaperLayers?> = _wallpaper.asStateFlow()

    init {
        // Wallpaper dimuat ulang hanya saat sumbernya atau kualitas blur berubah.
        viewModelScope.launch {
            container.settingsRepository.settings
                .map { s ->
                    val policy = RenderPolicy.resolve(
                        s.safeMode, s.renderQuality, s.animationsEnabled,
                        reducedMotion = false, lowRam = container.isLowRamDevice
                    )
                    WallpaperKey(s.wallpaperMode, s.wallpaperPreset, s.wallpaperVersion, policy.blurWidth, policy.blurRadius)
                }
                .distinctUntilChanged()
                .collectLatest { key ->
                    try {
                        _wallpaper.value = container.wallpaperRepository.load(
                            key.mode, key.preset, key.blurWidth, key.blurRadius
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        AetherLog.e("Gagal memuat wallpaper", e)
                        _wallpaper.value = null
                    }
                }
        }
    }

    fun onStartupCompleted() {
        container.crashRecoveryManager.markStartupCompleted()
    }

    fun enableSafeModeAndContinue() {
        viewModelScope.launch { container.settingsRepository.setSafeMode(true) }
        container.crashRecoveryManager.clearHistory()
    }

    fun continueNormally() {
        container.crashRecoveryManager.clearHistory()
    }

    fun setDockGeometry(x: Int, y: Int, scale: Int) {
        viewModelScope.launch {
            container.settingsRepository.update {
                it.copy(dockXPercent = x, dockYPercent = y, dockScalePercent = scale)
            }
        }
    }

    fun openEmergencyExit(context: Context): EmergencyExitResult =
        container.emergencyRecoveryController.open(context)
}
