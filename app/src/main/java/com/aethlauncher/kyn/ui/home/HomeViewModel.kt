package com.aethlauncher.kyn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aethlauncher.kyn.core.AppContainer
import com.aethlauncher.kyn.data.models.HomeLayout
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.domain.usecase.HomeLayoutOps
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader
import com.aethlauncher.kyn.launcher.resolver.LaunchableApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    /** Null sampai layout selesai dibaca dari DataStore. */
    val layout: StateFlow<HomeLayout?> = container.layoutRepository.layout
        .map<HomeLayout, HomeLayout?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Aplikasi yang boleh tampil: yang disembunyikan tidak pernah muncul di Home maupun picker. */
    val apps: StateFlow<List<LaunchableApp>> = combine(
        container.appsStore.apps,
        container.appsRepository.prefs
    ) { all, prefs -> all.filter { it.ref.key !in prefs.hidden } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val appsLoaded: StateFlow<Boolean> = container.appsStore.loaded

    val appsByKey: StateFlow<Map<String, LaunchableApp>> = apps
        .map { list -> list.associateBy { it.ref.key } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val iconLoader: AppIconLoader get() = container.iconLoader

    fun addToPage(page: Int, app: LaunchableApp) =
        updateLayout { HomeLayoutOps.addShortcut(it, page, app.ref) }

    fun addToDock(app: LaunchableApp) =
        updateLayout { HomeLayoutOps.addDock(it, app.ref) }

    fun removeEverywhere(ref: ShortcutRef) =
        updateLayout { HomeLayoutOps.removeEverywhere(it, ref) }

    /**
     * Drop di halaman. [visibleIndex] adalah indeks sel di grid; diterjemahkan ke indeks
     * pada daftar tersimpan (yang bisa memuat aplikasi yang sudah tidak terpasang).
     */
    fun dropOnPage(ref: ShortcutRef, page: Int, visibleIndex: Int) {
        val byKey = appsByKey.value
        updateLayout { layout ->
            val stored = layout.pages.getOrNull(page).orEmpty().filter { it != ref }
            val visible = stored.filter { byKey.containsKey(it.key) }
            val index = visibleIndex.coerceIn(0, visible.size)
            val storedIndex = if (index < visible.size) stored.indexOf(visible[index]) else stored.size
            HomeLayoutOps.placeOnPage(layout, ref, page, storedIndex)
        }
    }

    fun dropOnDock(ref: ShortcutRef, visibleIndex: Int, capacity: Int) {
        val byKey = appsByKey.value
        updateLayout { layout ->
            val stored = layout.dock.filter { it != ref }
            val visible = stored.filter { byKey.containsKey(it.key) }
            val index = visibleIndex.coerceIn(0, visible.size)
            val storedIndex = if (index < visible.size) stored.indexOf(visible[index]) else stored.size
            HomeLayoutOps.placeOnDock(layout, ref, storedIndex, capacity, visible.size)
        }
    }

    fun hide(ref: ShortcutRef) {
        viewModelScope.launch {
            container.appsRepository.update { it.copy(hidden = it.hidden + ref.key) }
        }
    }

    private fun updateLayout(transform: (HomeLayout) -> HomeLayout) {
        viewModelScope.launch { container.layoutRepository.update(transform) }
    }
}
