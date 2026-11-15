package com.aethlauncher.kyn.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aethlauncher.kyn.core.AppContainer
import com.aethlauncher.kyn.core.preferences.AppSettings
import com.aethlauncher.kyn.core.recovery.CrashRecord
import com.aethlauncher.kyn.core.recovery.CrashStatus
import com.aethlauncher.kyn.core.recovery.EmergencyExitResult
import com.aethlauncher.kyn.core.theme.ThemeMode
import com.aethlauncher.kyn.core.theme.ThemeStyle
import com.aethlauncher.kyn.data.models.HomeLayout
import com.aethlauncher.kyn.domain.usecase.HomeLayoutOps
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
    val crashStatus: CrashStatus = CrashStatus(0, 0, null)
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val crashStatus = MutableStateFlow(container.crashRecoveryManager.getStatus())

    val uiState: StateFlow<SettingsUiState> = combine(
        container.settingsRepository.settings,
        container.layoutRepository.layout,
        crashStatus
    ) { settings, layout, status -> SettingsUiState(settings, layout, status) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    // ---- tema ----
    fun setThemeStyle(style: ThemeStyle) = updateSettings { it.copy(themeStyle = style) }
    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }
    fun setDynamicColor(enabled: Boolean) = updateSettings { it.copy(dynamicColor = enabled) }

    // ---- grid & home ----
    fun setGrid(columns: Int, rows: Int) =
        updateSettings { it.copy(gridColumns = columns, gridRows = rows) }

    fun setGridColumns(columns: Int) = updateSettings { it.copy(gridColumns = columns) }
    fun setGridRows(rows: Int) = updateSettings { it.copy(gridRows = rows) }
    fun setShowLabels(show: Boolean) = updateSettings { it.copy(showLabels = show) }
    fun setShowClock(show: Boolean) = updateSettings { it.copy(showClock = show) }
    fun setShowDate(show: Boolean) = updateSettings { it.copy(showDate = show) }
    fun setDefaultPage(index: Int) = updateSettings { it.copy(defaultPage = index) }

    // ---- dock ----
    fun setDockCount(count: Int) = updateSettings { it.copy(dockCount = count) }
    fun setDockOpacity(percent: Int) = updateSettings { it.copy(dockOpacityPercent = percent) }
    fun setShowDockLabels(show: Boolean) = updateSettings { it.copy(showDockLabels = show) }

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
                    defaultPage = HomeLayoutOps.defaultPageAfterRemove(
                        it.defaultPage, index, after.pages.size
                    )
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

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { container.settingsRepository.update(transform) }
    }
}
