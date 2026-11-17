package com.aethlauncher.kyn.launcher.resolver

import com.aethlauncher.kyn.core.utils.AetherLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sumber tunggal daftar aplikasi launchable. Dimuat sekali, lalu diperbarui hanya saat
 * ada broadcast instal/hapus/update paket (dengan debounce), bukan saat recomposition.
 */
class AppsStore(private val resolver: LaunchableAppsResolver) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _apps = MutableStateFlow<List<LaunchableApp>>(emptyList())
    val apps: StateFlow<List<LaunchableApp>> = _apps.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private val changes = MutableStateFlow(0)

    init {
        scope.launch {
            changes.collectLatest { version ->
                if (version > 0) delay(300)
                refresh()
            }
        }
    }

    fun notifyPackagesChanged() {
        changes.update { it + 1 }
    }

    private suspend fun refresh() {
        try {
            _apps.value = resolver.queryAll()
            _loaded.value = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AetherLog.e("Gagal memuat daftar aplikasi", e)
        }
    }
}
