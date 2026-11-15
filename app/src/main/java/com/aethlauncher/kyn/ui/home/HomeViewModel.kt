package com.aethlauncher.kyn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aethlauncher.kyn.core.AppContainer
import com.aethlauncher.kyn.data.models.HomeLayout
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.domain.usecase.HomeLayoutOps
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    /** Null sampai layout selesai dibaca dari DataStore. */
    val layout: StateFlow<HomeLayout?> = container.layoutRepository.layout
        .map<HomeLayout, HomeLayout?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _apps = MutableStateFlow<List<LaunchableApp>>(emptyList())
    val apps: StateFlow<List<LaunchableApp>> = _apps.asStateFlow()

    private val _appsLoaded = MutableStateFlow(false)
    val appsLoaded: StateFlow<Boolean> = _appsLoaded.asStateFlow()

    val appsByKey: StateFlow<Map<String, LaunchableApp>> = _apps
        .map { list -> list.associateBy { it.ref.key } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val iconLoader: AppIconLoader get() = container.iconLoader

    /** Dipanggil setiap Home kembali aktif. Observer instal/hapus aplikasi menyusul di Milestone 3. */
    fun refreshApps() {
        viewModelScope.launch {
            _apps.value = container.appsResolver.queryAll()
            _appsLoaded.value = true
        }
    }

    fun addToPage(page: Int, app: LaunchableApp) =
        updateLayout { HomeLayoutOps.addShortcut(it, page, app.ref) }

    fun removeShortcut(page: Int, ref: ShortcutRef) =
        updateLayout { HomeLayoutOps.removeShortcut(it, page, ref) }

    fun moveShortcut(page: Int, ref: ShortcutRef, delta: Int) =
        updateLayout { HomeLayoutOps.moveShortcut(it, page, ref, delta) }

    fun addToDock(app: LaunchableApp) =
        updateLayout { HomeLayoutOps.addDock(it, app.ref) }

    fun removeFromDock(ref: ShortcutRef) =
        updateLayout { HomeLayoutOps.removeDock(it, ref) }

    fun moveDock(ref: ShortcutRef, delta: Int) =
        updateLayout { HomeLayoutOps.moveDock(it, ref, delta) }

    private fun updateLayout(transform: (HomeLayout) -> HomeLayout) {
        viewModelScope.launch { container.layoutRepository.update(transform) }
    }
}
